package org.geoserver.bxml.feature;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.geoserver.bxml.gml_3_1.GMLChainDecoder;
import org.geotools.gml3.GML;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class PropertyValueDecoder extends AbstractDecoder<Object> {

    private Object value;

    public PropertyValueDecoder() {
        super(PropertyDecoder.Value);
    }

    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if (name.getNamespaceURI().equals(GML.NAMESPACE)) {
            value = new GMLChainDecoder().decode(r);
        } else {
            throw new IllegalArgumentException(name + "is not supported for WFS Property value");
        }
    }

    protected void setStringValue(String value) throws Exception {
        this.value = value;
    }

    @Override
    protected Object buildResult() {
        return value;
    }

}
