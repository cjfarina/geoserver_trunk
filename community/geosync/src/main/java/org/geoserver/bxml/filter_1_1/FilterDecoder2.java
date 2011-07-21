package org.geoserver.bxml.filter_1_1;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.geotools.filter.v1_1.OGC;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.opengis.filter.Filter;

public class FilterDecoder2 implements Decoder<Filter> {

    private Decoder<Filter> anyFilter;

    public FilterDecoder2() {
        this.anyFilter = new AnyFilterDecoder();
    }

    @Override
    public Filter decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, OGC.Filter.getNamespaceURI(), OGC.Filter.getLocalPart());

        r.nextTag();

        Filter filter = anyFilter.decode(r);

        r.nextTag();

        r.require(EventType.END_ELEMENT, OGC.Filter.getNamespaceURI(), OGC.Filter.getLocalPart());
        return filter;
    }

    @Override
    public boolean canHandle(QName name) {
        return OGC.Filter.equals(name);
    }

    @Override
    public Set<QName> getTargets() {
        return Collections.singleton(OGC.Filter);
    }
}
