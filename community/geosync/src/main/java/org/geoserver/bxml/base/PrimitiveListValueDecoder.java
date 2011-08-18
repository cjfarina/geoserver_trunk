package org.geoserver.bxml.base;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geoserver.bxml.ValueDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.springframework.util.Assert;

/**
 * The Class PrimitiveListValueDecoder.
 * 
 * @param <T> the generic type
 * 
 * @author cfarina
 */
public class PrimitiveListValueDecoder<T> implements ValueDecoder<T[]> {

    /** The can handle. */
    private List<EventType> canHandle = Arrays.asList(EventType.VALUE_BOOL, EventType.VALUE_DOUBLE,
            EventType.VALUE_FLOAT, EventType.VALUE_BYTE, EventType.VALUE_INT, EventType.VALUE_LONG,
            EventType.VALUE_STRING);

    /** The type. */
    private final Class<T> type;

    /**
     * Instantiates a new primitive list value decoder.
     * 
     * @param type the type
     */
    public PrimitiveListValueDecoder(Class<T> type) {
        this.type = type;
    }

    /**
     * Decode.
     * 
     * @param r the r
     * @return the t[]
     * @throws Exception the exception
     */
    @SuppressWarnings("unchecked")
    @Override
    public T[] decode(BxmlStreamReader r) throws Exception {
        StringBuilder sb = new StringBuilder();

        EventType eventType = r.getEventType();
        T[] values = (T[]) Array.newInstance(type, 0);
        int pos = 0;

        if (!eventType.isValue()) {
            throw new IllegalArgumentException("r.getEventType() must to be  value event.");
        }
        Assert.isTrue(canHandle(eventType));

        while (eventType.isValue()) {

            if (EventType.VALUE_STRING.equals(eventType)) {
                sb.append(new StringValueDecoder().decode(r));
            } else {
                if (sb.length() > 0) {
                    values = append(values, sb);
                    sb.setLength(0);
                }
                final int valueCount = r.getValueCount();
                values = expand(values, valueCount);
                for (int i = 0; i < valueCount; i++, pos++) {
                    values[pos] = new PrimitiveReader<T>().read(r, type, eventType);
                }
            }
            if (r.getEventType().isValue()) {
                eventType = r.next();
            } else {
                break;
            }

        }
        if (sb.length() > 0) {
            values = append(values, sb);
            sb.setLength(0);
        }
        return values;
    }

    /**
     * Append.
     * 
     * @param values the values
     * @param sb the sb
     * @return the t[]
     */
    private T[] append(T[] values, final StringBuilder sb) {
        final int len = sb.length();

        StringBuilder curr = new StringBuilder();

        List<String> strings = new ArrayList<String>();
        for (int i = 0; i < len; i++) {
            char c = sb.charAt(i);
            if (!Character.isWhitespace(c)) {
                curr.append(c);
            } else if (curr.length() > 0) {
                strings.add(curr.toString());
                curr.setLength(0);
            }
        }
        if (curr.length() > 0) {
            strings.add(curr.toString());
            curr.setLength(0);
        }
        final int count = strings.size();
        final int startIndex = values.length;
        values = expand(values, count);
        for (int i = 0; i < count; i++) {
            values[startIndex + i] = new PrimitiveReader<T>().convertToType(strings.get(i), type);
        }
        return values;
    }

    /**
     * Expand.
     * 
     * @param values the values
     * @param valueCount the value count
     * @return the t[]
     */
    @SuppressWarnings("unchecked")
    private T[] expand(T[] values, int valueCount) {
        int newLength = values.length + valueCount;
        T[] expanded = (T[]) Array.newInstance(type, newLength);
        System.arraycopy(values, 0, expanded, 0, values.length);
        return expanded;
    }

    /**
     * Can handle.
     * 
     * @param type the type
     * @return true, if successful
     */
    @Override
    public boolean canHandle(EventType type) {
        return canHandle.contains(type);
    }

}
