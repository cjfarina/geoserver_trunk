package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.Not;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

public class NotDecoder implements Decoder<Filter> {

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    @Override
    public Filter decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, Not.getNamespaceURI(), Not.getLocalPart());
        r.nextTag();

        AnyFilterDecoder anyFilter = new AnyFilterDecoder();
        Filter negated = anyFilter.decode(r);

        r.nextTag();

        r.require(EventType.END_ELEMENT, Not.getNamespaceURI(), Not.getLocalPart());

        return ff.not(negated);
    }

    @Override
    public boolean canHandle(QName name) {
        return Not.equals(name);
    }

    @Override
    public Set<QName> getTargets() {
        return Collections.singleton(Not);
    }
}
