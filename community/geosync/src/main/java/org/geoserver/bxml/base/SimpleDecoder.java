package org.geoserver.bxml.base;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public abstract class SimpleDecoder<T> implements Decoder<T> {

    protected final QName elemName;

    public SimpleDecoder(final QName elemName) {
        this.elemName = elemName;
    }

    @Override
    public boolean canHandle(QName name) {
        return elemName.equals(name);
    }

    @Override
    public Set<QName> getTargets() {
        return Collections.singleton(elemName);
    }
    
    protected boolean isEndElement(BxmlStreamReader r) {
        return r.getEventType() == EventType.END_ELEMENT && r.getElementName().equals(elemName);
    }

}
