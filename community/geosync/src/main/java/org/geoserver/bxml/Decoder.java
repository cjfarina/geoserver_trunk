package org.geoserver.bxml;

import java.util.Set;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;

public interface Decoder<T> {

    public abstract T decode(BxmlStreamReader r) throws Exception;

    public abstract boolean canHandle(QName name);

    public abstract Set<QName> getTargets();

}