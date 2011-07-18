package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.PropertyIsEqualTo;
import static org.geotools.filter.v1_1.OGC.PropertyIsGreaterThan;
import static org.geotools.filter.v1_1.OGC.PropertyIsGreaterThanOrEqualTo;
import static org.geotools.filter.v1_1.OGC.PropertyIsLessThan;
import static org.geotools.filter.v1_1.OGC.PropertyIsLessThanOrEqualTo;
import static org.geotools.filter.v1_1.OGC.PropertyIsNotEqualTo;

import java.util.ArrayList;
import java.util.List;

import org.geoserver.bxml.filter_1_1.expression.ExpressionDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

public class BinaryComparisonOperatorDecoder extends ListDecoder<Filter> {

    protected final List<Expression> expresions = new ArrayList<Expression>();

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public BinaryComparisonOperatorDecoder() {
        add(PropertyIsEqualTo);
        add(PropertyIsNotEqualTo);
        add(PropertyIsLessThan);
        add(PropertyIsGreaterThan);
        add(PropertyIsLessThanOrEqualTo);
        add(PropertyIsGreaterThanOrEqualTo);
    }

    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        expresions.add(new ExpressionDecoder().decode(r));
    }

    @Override
    protected Filter buildResult() {
        if (PropertyIsEqualTo.equals(name)) {
            return ff.equals(expresions.get(0), expresions.get(1));
        }

        if (PropertyIsNotEqualTo.equals(name)) {
            return ff.notEqual(expresions.get(0), expresions.get(1));
        }

        if (PropertyIsLessThan.equals(name)) {
            return ff.less(expresions.get(0), expresions.get(1));
        }

        if (PropertyIsGreaterThan.equals(name)) {
            return ff.greater(expresions.get(0), expresions.get(1));
        }

        if (PropertyIsGreaterThanOrEqualTo.equals(name)) {
            return ff.greaterOrEqual(expresions.get(0), expresions.get(1));
        }

        if (PropertyIsLessThanOrEqualTo.equals(name)) {
            return ff.lessOrEqual(expresions.get(0), expresions.get(1));
        }

        throw new IllegalArgumentException(this.getClass().getName() + " can not decode " + name);
    }
}
