package org.geoserver.bxml.wfs_1_1;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.BXMLDecoderUtil;
import org.geoserver.bxml.Decoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class SimpleFeatureAttributeDecoder implements Decoder<Object> {

    private final String namespace;

    public SimpleFeatureAttributeDecoder(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public Object decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, null, null);
        // r.nextTag();
        Object value = BXMLDecoderUtil.readValue(r);
        r.require(EventType.END_ELEMENT, null, null);
        return value;
    }

    @Override
    public boolean canHandle(QName name) {
        return namespace.equals(name.getNamespaceURI());
    }

    @Override
    public Set<QName> getTargets() {
        return new HashSet<QName>();
    }

}
