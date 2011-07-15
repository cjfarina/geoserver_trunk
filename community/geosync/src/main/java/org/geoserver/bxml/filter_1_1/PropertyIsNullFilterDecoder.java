package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.PropertyIsNull;

import org.geoserver.bxml.filter_1_1.expression.ExpressionChainDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;

public class PropertyIsNullFilterDecoder extends FilterLinkDecoder {

    protected Expression expresion = null;

    public PropertyIsNullFilterDecoder() {
        super(PropertyIsNull, new PropertyIsBetweenFilterDecoder());
    }

    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        expresion = new ExpressionChainDecoder().decode(r);
    }

    @Override
    protected Filter buildResult() {
        return ff.isNull(expresion);
    }

}
