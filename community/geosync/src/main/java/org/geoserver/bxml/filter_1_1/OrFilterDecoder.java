package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.Or;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;

public class OrFilterDecoder extends FilterLinkDecoder {

    private List<Filter> params = new ArrayList<Filter>();

    public OrFilterDecoder() {
        super(Or, new NotFilterDecoder());
    }

    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();
        params.add(new FilterChainDecoder(name).decodeFilter(r));
    }

    @Override
    protected Filter buildResult() {
        return ff.or(params.get(0), params.get(1));
    }
}
