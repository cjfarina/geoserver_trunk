package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.FeatureId;
import static org.geotools.filter.v1_1.OGC.Filter;

import java.io.IOException;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.Identifier;

public class FilterChainDecoder extends AbstractDecoder<Filter> {

    private AndFilterDecoder filterLink;

    private Filter filter;
    
    private Set<Identifier> identifiers;
    
    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public FilterChainDecoder(QName name) {
        super(Filter);
        filterLink = new AndFilterDecoder();
    }
    
    public Filter decodeFilter(final BxmlStreamReader r) throws IOException {
        if(filterLink != null) {
            return filterLink.decode(r);
        } else {
            return null;
        }
    }

    protected void decodeElement(final BxmlStreamReader r) throws IOException {
        QName name = r.getElementName();
        
        if(FeatureId.equals(name)){
            //TODO:Add identifiers here
        } else {
            filter = decodeFilter(r);
        }
    }

    @Override
    protected Filter buildResult() {
        return filter;
    }
}
