package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.PropertyIsLike;

import java.util.ArrayList;
import java.util.List;

import org.geoserver.bxml.AbstractDecoder;
import org.geoserver.bxml.filter_1_1.expression.ExpressionDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.LiteralExpressionImpl;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.springframework.util.Assert;

public class PropertyIsLikeFilterDecoder extends AbstractDecoder<Filter> {

    protected final List<Expression> expresions = new ArrayList<Expression>();

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());
    
    public PropertyIsLikeFilterDecoder() {
        super(PropertyIsLike);
    }

    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        expresions.add(new ExpressionDecoder().decode(r));
    }

    @Override
    protected Filter buildResult() {
        Object value = ((LiteralExpressionImpl) expresions.get(1)).getValue();
        Assert.notNull(value, "Literal in IsLike filte can't be null");
        return ff.like(expresions.get(0), value.toString());
    }

}
