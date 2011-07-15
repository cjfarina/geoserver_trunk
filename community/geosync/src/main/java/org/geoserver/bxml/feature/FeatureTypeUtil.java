package org.geoserver.bxml.feature;

import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.Atom;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class FeatureTypeUtil {

    private static final String FEATURE_PREFIX = "f:";

    public static QName buildFeatureTypeName(BxmlStreamReader r, Map<QName, String> attributes,
            QName elementName) {
        QName name = r.getElementName();

        String namespaceURI = r.getNamespaceURI("f");

        QName typeName = new QName(Atom.NAMESPACE, "typeName");

        if (elementName.equals(name)) {
            if (attributes.get(typeName) != null) {

                String value = attributes.get(typeName);
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
