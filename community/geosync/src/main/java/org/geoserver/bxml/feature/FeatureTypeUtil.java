package org.geoserver.bxml.feature;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;

public class FeatureTypeUtil {

    private static final String FEATURE_PREFIX = "f:";

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

    public static QName buildQName(String nameString, String namespaceURI) {
        QName qName = null;
        if (nameString.startsWith(FEATURE_PREFIX)) {
            qName = new QName(namespaceURI, nameString.substring(2));
        }
        return qName;
    }
}
