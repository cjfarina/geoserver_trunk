package org.geoserver.bxml.base;

import java.io.IOException;

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
        String value = readStringValue(r);

        r.require(EventType.END_ELEMENT, null, elemName.getLocalPart());
        return value;
    }

    public static String readStringValue(BxmlStreamReader r) throws IOException {
        EventType type;
        StringBuilder sb = null;
        while ((type = r.getEventType()).isValue()) {
            if (sb == null) {
                sb = new StringBuilder();
            }
            sb.append(r.getStringValue());
            r.next();
        }
        return sb == null ? null : sb.toString();
    }
}
