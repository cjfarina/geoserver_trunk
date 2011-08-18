package org.geoserver.bxml.base;

import org.geoserver.bxml.ValueDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class PrimitivesValueDecoder implements ValueDecoder<Object> {

    final private StringValueDecoder stringDecoder = new StringValueDecoder();

    final private PrimitiveValueDecoder<Object> primitivesValueDecoder = new PrimitiveValueDecoder<Object>(
            Object.class);

    final private PrimitiveListValueDecoder<Object> primitivesListValueDecoder = new PrimitiveListValueDecoder<Object>(
            Object.class);

    @Override
    public Object decode(BxmlStreamReader r) throws Exception {
        EventType type = r.getEventType();

        if (!type.isValue()) {
            throw new IllegalArgumentException("r.getEventType() must to be  value event.");
        }

        Object value = null;

        if (EventType.VALUE_STRING == type) {
            value = stringDecoder.decode(r);
        } else if (r.getValueCount() > 1) {
            value = primitivesListValueDecoder.decode(r);
        } else {
            value = primitivesValueDecoder.decode(r);
        }
        return value;
    }

    @Override
    public boolean canHandle(EventType type) {
        return true;
    }

}
