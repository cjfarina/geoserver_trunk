package org.geoserver.bxml.wfs_1_1;

import java.util.HashSet;

import javax.xml.namespace.QName;

import org.geoserver.bxml.BxmlElementIterator;
import org.gvsig.bxml.stream.BxmlStreamReader;

/**
 * The Class SimpleFeatureAttributeIterator extends from BxmlElementIterator in order to redefine
 * the template method isExpectedElement(), which, in the case of the parsing of a feature, only the
 * namespace URI must to be the same.
 * 
 * @author cfarina
 */
public class SimpleFeatureAttributeIterator extends BxmlElementIterator {

    /** The namespace. */
    private final String namespace;

    /**
     * Instantiates a new simple feature attribute iterator.
     * 
     * @param reader the reader
     * @param namespace the namespace
     */
    public SimpleFeatureAttributeIterator(final BxmlStreamReader reader, final String namespace) {
        super(reader, new HashSet<QName>());
        this.namespace = namespace;
    }

    /**
     * Checks if is expected element.
     * 
     * @param elementName the element name
     * @return true, if is expected element
     */
    protected boolean isExpectedElement(QName elementName) {
        return elementName.getNamespaceURI().equals(namespace);
    }

}
