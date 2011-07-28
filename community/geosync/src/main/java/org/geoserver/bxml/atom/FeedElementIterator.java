package org.geoserver.bxml.atom;

import static org.geoserver.gss.internal.atom.Atom.entry;

import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.BxmlElementIterator;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class FeedElementIterator extends BxmlElementIterator {

    public FeedElementIterator(BxmlStreamReader reader, QName elemName) {
        super(reader, elemName);
    }

    public FeedElementIterator(final BxmlStreamReader reader, final Set<QName> siblingNames) {
        super(reader, siblingNames);
    }

    protected boolean finish(BxmlStreamReader reader) {
        QName name = reader.getElementName();
        return name.equals(entry);
    }

}
