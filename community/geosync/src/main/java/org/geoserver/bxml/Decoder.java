package org.geoserver.bxml;

import java.util.Set;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;

// TODO: Auto-generated Javadoc
/**
 * The Interface Decoder.
 * 
 * @param <T> the generic type
 */
public interface Decoder<T> {

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
     * @param name the name
     * @return true, if successful
     */
    public abstract boolean canHandle(QName name);

    /**
     * Gets the targets.
     * 
     * @return the targets
     */
    public abstract Set<QName> getTargets();

}