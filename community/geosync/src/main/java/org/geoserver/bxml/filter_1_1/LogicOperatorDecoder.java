package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.And;
import static org.geotools.filter.v1_1.OGC.Not;
import static org.geotools.filter.v1_1.OGC.Or;

import java.util.ArrayList;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

public class LogicOperatorDecoder extends ListDecoder<Filter> {

    private final List<Filter> params = new ArrayList<Filter>();
    
    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());
    
    public LogicOperatorDecoder() {
        add(And);
        add(Or);
        add(Not);
    }
    
    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        params.add(new FilterDecoder().decode(r));
    }
    
    @Override
    protected Filter buildResult() {
        if(And.equals(name)){
            return ff.and(params);
        } else if(Or.equals(name)){
            return ff.or(params);
        } else if(Not.equals(name)){
            return ff.not(params.get(0));
        }
        return null;
    }
}
