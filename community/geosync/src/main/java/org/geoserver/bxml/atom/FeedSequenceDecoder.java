package org.geoserver.bxml.atom;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.BxmlElementIterator;
import org.geoserver.bxml.SequenceDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class FeedSequenceDecoder<T> extends SequenceDecoder<T> {

    public FeedSequenceDecoder(final int minOccurs, final int maxOccurs) {
        super(minOccurs, maxOccurs);
    }

    @Override
    protected BxmlElementIterator buildIterator(BxmlStreamReader r, Set<QName> sequenceNames) {
        return new FeedElementIterator(r, new HashSet<QName>(sequenceNames));
    }
}
