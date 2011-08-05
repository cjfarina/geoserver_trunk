package org.geoserver.bxml.feature;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.geoserver.bxml.BXMLDecoderUtil;
import org.geoserver.bxml.base.SimpleDecoder;
import org.geoserver.bxml.base.StringDecoder;
import org.geoserver.bxml.gml_3_1.GeometryDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class PropertyValueDecoder extends SimpleDecoder<Object> {

    public PropertyValueDecoder() {
        super(new QName("http://www.opengis.net/wfs", "Value"));
    }

    @Override
    public Object decode(BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();
        r.require(EventType.START_ELEMENT, name.getNamespaceURI(), name.getLocalPart());

        Object value = BXMLDecoderUtil.readValue(r);

        r.require(EventType.END_ELEMENT, name.getNamespaceURI(), name.getLocalPart());
        return value;
    }

}
