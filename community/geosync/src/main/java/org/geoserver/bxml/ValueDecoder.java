package org.geoserver.bxml;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

/**
 * The Interface ValueDecoder.
 * 
 * @param <T> the generic type
 * 
 * @author cfarina
 */
public interface ValueDecoder<T> {

    /**
     * Decode.
     * 
     * @param r the r
     * @return the t
     * @throws Exception the exception
     */
    public abstract T decode(BxmlStreamReader r) throws Exception;

    /**
     * Can handle.
     * 
     * @param type the type
     * @return true, if successful
     */
    public abstract boolean canHandle(EventType type);
}
