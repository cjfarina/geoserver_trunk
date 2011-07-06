package org.geoserver.gss.internal.atom.decoders.filters;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.decoders.AbstractDecoder;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.v1_1.OGC;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;

import static org.geotools.filter.v1_1.OGC.FeatureId;

public class FilterChainDecoder extends AbstractDecoder<Filter> {

    private AndFilterDecoder filterLink;

    private Filter filter;

    public FilterChainDecoder() {
        super(OGC.Filter);
        filterLink = new AndFilterDecoder();
    }

    protected void decodeElement(final BxmlStreamReader r) throws IOException {
        QName name = r.getElementName();
        
        if(FeatureId.equals(name)){
            /*if(filter == null){
                FilterFactory ff = FilterFactoryFinder.createFilterFactory();
                filter = ff.createFidFilter();
            }
            ((Id)filter).accept(paramFilterVisitor, paramObject)*/
        } else {
            filter = filterLink.decodeFilter(r);
        }
    }

    @Override
    protected Filter buildResult() {
        return filter;
    }
}
