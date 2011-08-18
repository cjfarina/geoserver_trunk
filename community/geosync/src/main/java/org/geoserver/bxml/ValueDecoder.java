package org.geoserver.bxml;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public interface ValueDecoder<T> {

    public abstract T decode(BxmlStreamReader r) throws Exception;
    
    public abstract boolean canHandle(EventType type);
}
