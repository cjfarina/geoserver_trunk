package org.geoserver.gss.internal.atom.decoders.filters;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.decoders.AbstractDecoder;
import org.geotools.filter.v1_1.OGC;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;

public class FilterChainDecoder extends AbstractDecoder<Filter> {

    private FeatureIdFilterDecoder filterLink;

    private Filter filter;

    public FilterChainDecoder() {
        super(OGC.Filter);
    }

    protected void decodeElement(final BxmlStreamReader r) throws IOException {
        filter = filterLink.decodeFilter(r);
    }

    @Override
    protected Filter buildResult() {
        return filter;
    }
}
