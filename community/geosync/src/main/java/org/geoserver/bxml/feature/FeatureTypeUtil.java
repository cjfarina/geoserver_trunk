package org.geoserver.bxml.feature;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;

/**
 * The Class FeatureTypeUtil.
 * 
 * @author cfarina
 */
public class FeatureTypeUtil {

    /** The Constant FEATURE_PREFIX. */
    private static final String FEATURE_PREFIX = "f:";

    /**
     * Builds the feature type name.
     * 
     * @param r
     *            the r
     * @param elementName
     *            the element name
     * @return the q name
     */
    public static QName buildFeatureTypeName(BxmlStreamReader r, QName elementName) {
        QName name = r.getElementName();

        String namespaceURI = r.getNamespaceURI("f");

        if (elementName.equals(name)) {
            if (r.getAttributeValue(null, "typeName") != null) {

                String value = r.getAttributeValue(null, "typeName");
                if (value.startsWith(FEATURE_PREFIX)) {
                    value = value.substring(2);
                }
                QName typeNameValue = new QName(namespaceURI, value);
                return typeNameValue;
            }
        }
        return null;
    }

    /**
     * Builds the feature name.
     * 
     * @param nameString
     *            the name string
     * @param namespaceURI
     *            the namespace uri
     * @return the q name
     */
    public static QName buildQName(String nameString, String namespaceURI) {
        QName qName = null;
        if (nameString.startsWith(FEATURE_PREFIX)) {
            qName = new QName(namespaceURI, nameString.substring(2));
        }
        return qName;
    }
}
