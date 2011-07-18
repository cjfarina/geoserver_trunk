package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.PropertyIsNull;

import org.geoserver.bxml.AbstractDecoder;
import org.geoserver.bxml.filter_1_1.expression.ExpressionDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

public class PropertyIsNullFilterDecoder extends AbstractDecoder<Filter> {

    protected Expression expresion = null;

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());
    
    public PropertyIsNullFilterDecoder() {
        super(PropertyIsNull);
    }

    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        expresion = new ExpressionDecoder().decode(r);
    }

    @Override
    protected Filter buildResult() {
        return ff.isNull(expresion);
    }

}
