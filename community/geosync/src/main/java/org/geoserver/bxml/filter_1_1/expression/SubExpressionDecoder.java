package org.geoserver.bxml.filter_1_1.expression;

import static org.geotools.filter.v1_1.OGC.Sub;

import org.opengis.filter.expression.Expression;

public class SubExpressionDecoder extends ArithmeticOperator {

    public SubExpressionDecoder() {
        super(Sub, new MulExpressionDecoder());
    }

    @Override
    protected Expression buildResult() {
        return ff.subtract(expresions.get(0), expresions.get(1));
    }
}
