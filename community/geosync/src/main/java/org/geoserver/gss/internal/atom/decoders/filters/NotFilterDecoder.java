package org.geoserver.gss.internal.atom.decoders.filters;

import static org.geotools.filter.v1_1.OGC.Not;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;

public class NotFilterDecoder extends FilterLinkDecoder {
    
    private Filter param;
    
    public NotFilterDecoder() {
        super(Not, new PropertyIsEqualToFilterDecoder());
    }
    
    @Override
    protected void decodeElement(final BxmlStreamReader r) throws IOException {
        QName name = r.getElementName();
        param = new FilterChainDecoder(name).decodeFilter(r);
    }

    @Override
    protected Filter buildResult() {
        return ff.not(param);
    }
}
