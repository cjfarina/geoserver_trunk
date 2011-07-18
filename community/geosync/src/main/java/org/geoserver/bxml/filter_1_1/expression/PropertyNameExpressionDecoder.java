package org.geoserver.bxml.filter_1_1.expression;

import static org.geotools.filter.v1_1.OGC.PropertyName;

import org.geoserver.bxml.AbstractDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

public class PropertyNameExpressionDecoder extends AbstractDecoder<Expression> {

    private String property;

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public PropertyNameExpressionDecoder() {
        super(PropertyName);
    }

    @Override
    protected void setStringValue(String value) throws Exception {
        property = value;
    }

    @Override
    protected Expression buildResult() {
        return ff.property(property);
    }
}
