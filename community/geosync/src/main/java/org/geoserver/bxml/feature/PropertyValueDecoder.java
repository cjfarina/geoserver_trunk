package org.geoserver.bxml.feature;

import javax.xml.namespace.QName;

import org.geoserver.bxml.base.SimpleDecoder;
import org.geoserver.bxml.base.StringDecoder;
import org.geoserver.bxml.gml_3_1.GMLChainDecoder;
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
        Object value = null;

        EventType event = r.next();

        if (EventType.VALUE_STRING == event) {
            value = StringDecoder.readStringValue(r);
            event = r.getEventType();
        }

        if (EventType.START_ELEMENT == event) {
            value = new GMLChainDecoder().decode(r);
            r.nextTag();
        }

        r.require(EventType.END_ELEMENT, name.getNamespaceURI(), name.getLocalPart());
        return value;
    }

}
