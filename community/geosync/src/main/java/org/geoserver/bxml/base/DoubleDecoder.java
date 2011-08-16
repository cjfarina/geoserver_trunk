package org.geoserver.bxml.base;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.springframework.util.Assert;

public class DoubleDecoder implements Decoder<Double> {

    private final QName name;

    public DoubleDecoder(final QName name) {
        this.name = name;
    }

    @Override
    public Double decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, null, name.getLocalPart());
        final EventType event = r.next();
        
        Double d;
        switch (event) {
        case VALUE_DOUBLE:
            d = Double.valueOf(r.getDoubleValue());
            break;
        case VALUE_FLOAT:
            d = Double.valueOf(r.getFloatValue());
            break;
        default:
            Assert.isTrue(event.isValue());
            String stringValue = r.getStringValue();
            d = Double.valueOf(stringValue);
            break;
        }
        r.nextTag();
        r.require(EventType.END_ELEMENT, null, name.getLocalPart());

        return d;
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
