package org.geogit.repository;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.RevTree;
import org.geogit.storage.ObjectInserter;
import org.geogit.storage.ObjectWriter;
import org.geogit.storage.RevTreeWriter;
import org.geotools.util.logging.Logging;
import org.springframework.util.Assert;

/**
 * The Index keeps track of the changes not yet committed to the repository.
 * <p>
 * Unlike other, similar tools you may have used, Git, I mean, GeoServer, does not commit changes
 * directly from the working tree into the repository. Instead, changes are first registered in
 * something called the index. Think of it as a way of "confirming" your changes, one by one, before
 * doing a commit (which records all your approved changes at once). Some find it helpful to call it
 * the "staging area", instead of the "index".
 * </p>
 * 
 * @author Gabriel Roldan
 * 
 */
public class Index {

    private static final Logger LOGGER = Logging.getLogger(Index.class);

    private Repository repository;

    private static final Comparator<List<String>> PATH_COMPARATOR = new Comparator<List<String>>() {
        public int compare(List<String> o1, List<String> o2) {
            int c = Integer.valueOf(o1.size()).compareTo(Integer.valueOf(o2.size()));
            if (c == 0) {
                for (int i = 0; i < o1.size(); i++) {
                    c = o1.get(i).compareTo(o2.get(i));
                    if (c != 0) {
                        return c;
                    }
                }
            }
            return c;
        }
    };

    // <parents, <featureId, blobId>>
    private TreeMap<List<String>, TreeMap<String, Entry>> staged;

    /*
     *
     */
    private static class Entry {
        /**
         * 
         * Status of an entry in the index. Aka, status of a feature
         * 
         */
        public static enum Status {
            /**
             * Content is new and not staged to be committed
             */
            NEW,
            /**
             * Content is new and staged to be committed
             */
            ADDED,
            /**
             * Content exists and was modified
             */
            MODIFIED,
            /**
             * Content was deleted
             */
            DELETED
        }

        private ObjectId blobId;

        private Status status;

        public Entry(ObjectId blobId, Status status) {
            this.blobId = blobId;
            this.status = status;
        }

        public String toString() {
            return "[" + status + ":" + blobId.toString() + "]";
        }
    }

    public Index(final Repository repository) {
        this.repository = repository;
        this.staged = new TreeMap<List<String>, TreeMap<String, Entry>>(PATH_COMPARATOR);
    }

    public void created(List<String> path) {
        // TODO Auto-generated method stub

    }

    /**
     * Deletes the object (tree or feature) addressed by {@code path}
     */
    public boolean deleted(String... path) {
        Assert.notNull(path);
        Assert.isTrue(path.length > 0);

        final String fid = path[2];
        final String typeName = path[1];
        final String nsuri = path[0];

        final ObjectId typeTreeId = repository.getChildTreeId(nsuri, typeName);
        if (typeTreeId == null) {
            return false;
        }
        RevTree tree = repository.getTree(typeTreeId);
        Ref child = tree.get(fid);
        if (child == null) {
            return false;
        }

        TreeMap<String, Entry> fidMap = getFidMap(nsuri, typeName);
        fidMap.put(fid, new Entry(child.getObjectId(), Entry.Status.DELETED));
        return true;
    }

    /**
     * @param object
     * @param path
     * @return
     * @throws Exception
     */
    public ObjectId inserted(final ObjectWriter<?> object, final String... path) throws Exception {
        Assert.notNull(object);
        Assert.notNull(path);
        Assert.noNullElements(path);

        final ObjectInserter objectInserter = repository.newObjectInserter();
        final ObjectId blobId = objectInserter.insert(object);
        final String featureId = path[path.length - 1];

        TreeMap<String, Entry> fidMap = getFidMap(path[0], path[1]);
        fidMap.put(featureId, new Entry(blobId, Entry.Status.NEW));

        return blobId;
    }

    /**
     * Marks an object rename (in practice, it's used to change the feature id of a Feature once it
     * was committed and the DataStore generated FID is obtained)
     * 
     * @param from
     *            old path to featureId
     * @param to
     *            new path to featureId
     */
    public void renamed(final List<String> from, final List<String> to) {
        TreeMap<String, Entry> oldMap = getFidMap(from.get(0), from.get(1));
        TreeMap<String, Entry> newMap = getFidMap(to.get(0), to.get(1));
        final String oldFid = from.get(2);
        final String newFid = to.get(2);

        Entry entry = oldMap.remove(oldFid);
        newMap.put(newFid, entry);
    }

    /**
     * Discards any staged change.
     * 
     * @REVISIT: should this be implemented through ResetOp (GeoGIT.reset()) instead?
     * @TODO: When we implement transaction management will be the time to discard any needed object
     *        inserted to the database too
     */
    public void reset() {
        this.staged.clear();
    }

    private synchronized TreeMap<String, Entry> getFidMap(final String nsuri, final String typeName) {
        final List<String> parents = Arrays.asList(nsuri, typeName);
        TreeMap<String, Entry> fidMap = staged.get(parents);

        if (fidMap == null) {
            fidMap = new TreeMap<String, Index.Entry>();
            staged.put(parents, fidMap);
        }
        return fidMap;
    }

    public ObjectId writeTree(final ObjectInserter objectInserter) throws Exception {
        if (staged.size() == 0) {
            return null;
        }

        final RevTree root;
        {
            RevTree currRoot = repository.getRootTree();
            root = currRoot == null ? repository.newTree() : currRoot;
        }

        Map<List<String>, RevTree> updates = new TreeMap<List<String>, RevTree>(PATH_COMPARATOR);
        final Set<List<String>> typeNames = new TreeSet<List<String>>(PATH_COMPARATOR);
        typeNames.addAll(staged.keySet());
        for (List<String> typeName : typeNames) {
            // TODO: make all this really n depth
            final String nsUri = typeName.get(0);
            final String localTypeName = typeName.get(1);

            RevTree typeNameTree = updates.get(typeName);
            if (typeNameTree == null) {
                typeNameTree = findOrCreateTypeNameTree(root, objectInserter, nsUri, localTypeName);
                updates.put(typeName, typeNameTree);
            }

            // update the tree with the leaf entries
            TreeMap<String, Entry> fidMap = staged.remove(typeName);
            TreeSet<String> fids = new TreeSet<String>(fidMap.keySet());
            for (String fid : fids) {
                Entry entry = fidMap.remove(fid);
                if (entry.status == Entry.Status.DELETED) {
                    typeNameTree.remove(fid);
                } else {
                    typeNameTree.put(new Ref(fid, entry.blobId, TYPE.BLOB));
                }
            }
        }

        Map<String, RevTree> nsTrees = new HashMap<String, RevTree>();

        for (Map.Entry<List<String>, RevTree> typeTreeEntry : updates.entrySet()) {

            final List<String> typePath = typeTreeEntry.getKey();
            final String nsUri = typePath.get(0);
            final String typeName = typePath.get(1);

            final RevTree typeNameTree = typeTreeEntry.getValue();

            final ObjectId newTypeTreeId = objectInserter.insert(new RevTreeWriter(typeNameTree));
            RevTree nsTree = nsTrees.get(nsUri);
            if (nsTree == null) {
                ObjectId nsTreeId = repository.getChildTreeId(root, nsUri);
                if (nsTreeId == null) {
                    nsTree = repository.newTree();
                } else {
                    nsTree = repository.getTree(nsTreeId);
                }
                nsTrees.put(nsUri, nsTree);
            }

            nsTree.put(new Ref(typeName, newTypeTreeId, TYPE.TREE));
        }

        for (Map.Entry<String, RevTree> nste : nsTrees.entrySet()) {
            String nsUri = nste.getKey();
            RevTree nsTree = nste.getValue();
            ObjectId nsTreeId = objectInserter.insert(new RevTreeWriter(nsTree));
            root.put(new Ref(nsUri, nsTreeId, TYPE.TREE));
        }

        final ObjectId newRootId = objectInserter.insert(new RevTreeWriter(root));

        return newRootId;
    }

    private RevTree findOrCreateTypeNameTree(RevTree root, ObjectInserter objectInserter,
            String nsUri, String typeName) throws Exception {

        ObjectId typeNameTreeId = repository.getChildTreeId(root, nsUri, typeName);
        RevTree typeNameTree;
        if (typeNameTreeId == null) {
            typeNameTree = repository.newTree();
        } else {
            typeNameTree = repository.getTree(typeNameTreeId);
        }
        return typeNameTree;
    }

}
