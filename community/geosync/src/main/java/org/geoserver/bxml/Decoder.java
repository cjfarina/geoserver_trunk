package org.geoserver.bxml;

import org.gvsig.bxml.stream.BxmlStreamReader;

public interface Decoder<T> {

    public abstract T decode(BxmlStreamReader r) throws Exception;

}