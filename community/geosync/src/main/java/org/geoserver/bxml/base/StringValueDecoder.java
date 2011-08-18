package org.geoserver.bxml.base;

import org.geoserver.bxml.ValueDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class StringValueDecoder implements ValueDecoder<String> {

    @Override
    public String decode(BxmlStreamReader r) throws Exception {
        EventType type;
        if (!r.getEventType().isValue()) {
            throw new IllegalArgumentException("r.getEventType() must to be  value event.");
        }
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

    @Override
    public boolean canHandle(EventType type) {
        return EventType.VALUE_STRING.equals(type);
    }

}
