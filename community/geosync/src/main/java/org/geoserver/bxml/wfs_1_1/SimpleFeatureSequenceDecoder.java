package org.geoserver.bxml.wfs_1_1;

import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.BxmlElementIterator;
import org.geoserver.bxml.SequenceDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class SimpleFeatureSequenceDecoder<T> extends SequenceDecoder<T> {

    private final String namespace;

    public SimpleFeatureSequenceDecoder(final String namespace, final int minOccurs,
            final int maxOccurs) {
        super(minOccurs, maxOccurs);
        this.namespace = namespace;
    }

    @Override
    protected BxmlElementIterator buildIterator(BxmlStreamReader r, Set<QName> sequenceNames) {
        return new SimpleFeatureAttributeIterator(r, namespace);
    }

}