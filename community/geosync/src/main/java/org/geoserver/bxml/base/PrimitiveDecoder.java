package org.geoserver.bxml.base;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.BXMLDecoderUtil;
import org.geoserver.bxml.Decoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class PrimitiveDecoder<T> implements Decoder<T> {

    private final QName name;
    private final Class<T> type;

    public PrimitiveDecoder(final QName name, Class<T> type) {
        this.name = name;
        this.type = type;
    }
    
    @Override
    public T decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, null, name.getLocalPart());
        final EventType event = r.next();
        final PrimitiveValueDecoder<T> valueDecoder = new PrimitiveValueDecoder<T>(type);

        T value = null;
        if (event.isValue() && valueDecoder.canHandle(event)) {
            value = (T)valueDecoder.decode(r);
        }

        BXMLDecoderUtil.goToEnd(r, name);
        r.require(EventType.END_ELEMENT, null, name.getLocalPart());

        return value;
    }

    @Override
    public boolean canHandle(QName name) {
        return name.equals(name);
    }

    @Override
    public Set<QName> getTargets() {
        return Collections.singleton(name);
    }

}
