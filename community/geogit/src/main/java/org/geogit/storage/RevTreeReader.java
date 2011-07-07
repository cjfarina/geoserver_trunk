package org.geogit.storage;

import static org.geogit.storage.BLOBS.BUCKET;
import static org.geogit.storage.BLOBS.ENTRY;
import static org.geogit.storage.BLOBS.TREE;

import java.io.IOException;
import java.io.InputStream;

import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.RevTree;
import org.gvsig.bxml.stream.BxmlInputFactory;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class RevTreeReader implements ObjectReader<RevTree> {

    private ObjectDatabase objectDb;

    private int order;

    public RevTreeReader(ObjectDatabase objectDb) {
        this(objectDb, 0);
    }

    public RevTreeReader(ObjectDatabase objectDb, int order) {
        this.objectDb = objectDb;
        this.order = order;
    }

    /**
     * @throws IOException
     * @see org.geogit.storage.ObjectReader#read(org.geogit.api.ObjectId, java.io.InputStream)
     */
    public RevSHA1Tree read(final ObjectId id, final InputStream rawData) throws IOException {
        final BxmlInputFactory inputFactory = BLOBS.cachedInputFactory;
        final BxmlStreamReader r = inputFactory.createScanner(rawData);
        r.nextTag();
        try {
            r.require(EventType.START_ELEMENT, TREE.getNamespaceURI(), TREE.getLocalPart());
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        EventType event;
        RevSHA1Tree tree = new RevSHA1Tree(objectDb, order);
        while ((event = r.next()) != EventType.END_DOCUMENT) {
            if (EventType.START_ELEMENT.equals(event)) {
                if (ENTRY.equals(r.getElementName())) {
                    Ref entryRef = parseEntry(r);
                    tree.put(entryRef);
                } else if (TREE.equals(r.getElementName())) {
                    parseAndSetSubTree(r, tree);
                }
            }
        }
        return tree;
    }

    private void parseAndSetSubTree(BxmlStreamReader r, RevSHA1Tree tree) throws IOException {
        int bucket;
        ObjectId subtreeId;

        r.require(EventType.START_ELEMENT, TREE.getNamespaceURI(), TREE.getLocalPart());
        r.nextTag();
        r.require(EventType.START_ELEMENT, BUCKET.getNamespaceURI(), BUCKET.getLocalPart());
        r.next();
        r.require(EventType.VALUE_INT, null, null);
        bucket = r.getIntValue();
        r.nextTag();
        r.require(EventType.END_ELEMENT, BUCKET.getNamespaceURI(), BUCKET.getLocalPart());

        r.nextTag();
        subtreeId = BLOBS.parseObjectId(r);
        r.nextTag();
        r.require(EventType.END_ELEMENT, TREE.getNamespaceURI(), TREE.getLocalPart());

        tree.put(Integer.valueOf(bucket), subtreeId);
    }

    private Ref parseEntry(BxmlStreamReader r) throws IOException {
        String childName;
        ObjectId childObjectId;

        r.require(EventType.START_ELEMENT, ENTRY.getNamespaceURI(), ENTRY.getLocalPart());
        int typeCode = Integer.parseInt(r.getAttributeValue(0));
        r.nextTag();
        childName = BLOBS.parseString(r);
        r.nextTag();
        childObjectId = BLOBS.parseObjectId(r);
        r.nextTag();
        r.require(EventType.END_ELEMENT, ENTRY.getNamespaceURI(), ENTRY.getLocalPart());

        TYPE type = TYPE.valueOf(typeCode);
        return new Ref(childName, childObjectId, type);
    }
}