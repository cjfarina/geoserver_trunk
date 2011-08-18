package org.geoserver.bxml.base;

import java.util.Arrays;
import java.util.List;

import org.geoserver.bxml.ValueDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.springframework.util.Assert;

public class PrimitiveValueDecoder<T> implements ValueDecoder<T> {

    private List<EventType> canHandle = Arrays.asList(EventType.VALUE_BOOL, EventType.VALUE_DOUBLE,
            EventType.VALUE_FLOAT, EventType.VALUE_BYTE, EventType.VALUE_INT, EventType.VALUE_LONG,
            EventType.VALUE_STRING);

    private final Class<T> type;

    public PrimitiveValueDecoder(Class<T> type) {
        this.type = type;
    }

    @Override
    public T decode(BxmlStreamReader r) throws Exception {
        final EventType event = r.getEventType();
        if (!event.isValue()) {
            throw new IllegalArgumentException("r.getEventType() must to be  value event.");
        }
        Assert.isTrue(canHandle(event));

        T value = null;
        value = new PrimitiveReader<T>().read(r, type, event);
        return value;
    }

    @Override
    public boolean canHandle(EventType type) {
        return canHandle.contains(type);
    }

}
