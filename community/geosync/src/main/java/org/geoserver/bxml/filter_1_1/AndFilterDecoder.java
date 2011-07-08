package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.And;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;

public class AndFilterDecoder extends FilterLinkDecoder {

    private List<Filter> params = new ArrayList<Filter>();
    
    public AndFilterDecoder() {
        super(And, new OrFilterDecoder());
    }
    
    @Override
    protected void decodeElement(final BxmlStreamReader r) throws IOException {
        QName name = r.getElementName();
        params.add(new FilterChainDecoder(name).decodeFilter(r));
    }

    @Override
    protected Filter buildResult() {
        return ff.and(params.get(0), params.get(1));
    }

}
