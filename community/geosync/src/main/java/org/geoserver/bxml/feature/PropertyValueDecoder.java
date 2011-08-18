package org.geoserver.bxml.feature;

import javax.xml.namespace.QName;

import org.geoserver.bxml.BXMLDecoderUtil;
import org.geoserver.bxml.base.GenericValueDecoder;
import org.geoserver.bxml.base.SimpleDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class PropertyValueDecoder extends SimpleDecoder<Object> {

    public PropertyValueDecoder() {
        super(new QName("http://www.opengis.net/wfs", "Value"));
    }

    @Override
    public Object decode(BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();
        r.require(EventType.START_ELEMENT, null, name.getLocalPart());

        Object value = new GenericValueDecoder().decode(r);

        r.require(EventType.END_ELEMENT, null, name.getLocalPart());
        return value;
    }

}
