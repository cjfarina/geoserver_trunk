package org.geoserver.bxml;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;

public interface Decoder<T> {

    public abstract T decode(BxmlStreamReader r) throws Exception;
    
    public abstract Boolean canHandle(QName name);

}