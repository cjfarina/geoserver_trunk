package org.geoserver.bxml.base;

import java.io.IOException;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class PrimitiveReader<T> {


    @SuppressWarnings("rawtypes")
    public T read(BxmlStreamReader r, Class toType, EventType type) throws Exception {
        Object value = readValue(r, type);
        if(value == null){
            return null;
        }
        return convertToType(value, toType);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public T convertToType(Object value, Class type) {
        
        if(type.equals(Object.class)){
            return (T)value;
        }
        
        if(type.equals(String.class)){
            return (T)value.toString();
        }
        
        if(type.equals(Boolean.class)){
            return (T)new Boolean(value.toString());
        }
        
        if(type.equals(Double.class)){
            return (T)new Double(value.toString());
        }
        
        if(type.equals(Float.class)){
            return (T)new Float(value.toString());
        }
        
        if(type.equals(Byte.class)){
            return (T)new Byte(value.toString());
        }
        
        if(type.equals(Integer.class)){
            return (T)new Integer(value.toString());
        }
        
        if(type.equals(Long.class)){
            return (T)new Long(value.toString());
        }
        
        return null;
    }

    private Object readValue(BxmlStreamReader r, EventType type) throws IOException, Exception {
        Object value = null;
        switch (type) {
        case VALUE_BOOL:
            value = r.getBooleanValue();
            break;
        case VALUE_DOUBLE:
            value = r.getDoubleValue();
            break;
        case VALUE_FLOAT:
            value = r.getFloatValue();
            break;
        case VALUE_BYTE:
            value = r.getByteValue();
            break;
        case VALUE_INT:
            value = r.getIntValue();
            break;
        case VALUE_LONG:
            value = r.getLongValue();
            break;
        case VALUE_STRING:
            value = new StringValueDecoder().decode(r);
            break;
        }
        return value;
    }

}
