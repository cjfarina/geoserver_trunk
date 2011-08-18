package org.geoserver.bxml.base;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class StringDecoder extends SimpleDecoder<String> {

    public StringDecoder(QName elemName) {
        super(elemName);
    }

    @Override
    public String decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, null, elemName.getLocalPart());
        r.next();
        String value = null;
        if (r.getEventType().isValue()) {
            value = new StringValueDecoder().decode(r);
        }

        r.require(EventType.END_ELEMENT, null, elemName.getLocalPart());
        return value;
    }

}
