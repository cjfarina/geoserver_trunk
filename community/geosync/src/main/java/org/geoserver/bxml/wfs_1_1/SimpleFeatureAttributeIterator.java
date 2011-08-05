package org.geoserver.bxml.wfs_1_1;

import java.util.HashSet;

import javax.xml.namespace.QName;

import org.geoserver.bxml.BxmlElementIterator;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class SimpleFeatureAttributeIterator extends BxmlElementIterator {

    private final String namespace;

    public SimpleFeatureAttributeIterator(final BxmlStreamReader reader, final String namespace) {
        super(reader, new HashSet<QName>());
        this.namespace = namespace;
    }

    protected boolean isExpectedElement(QName elementName) {
        return elementName.getNamespaceURI().equals(namespace);
    }

}
