package org.geoserver.bxml.filter_1_1.expression;

import static org.geotools.filter.v1_1.OGC.Div;

import org.opengis.filter.expression.Expression;

public class DivExpressionDecoder extends ArithmeticOperator {

    public DivExpressionDecoder() {
        super(Div, new FunctionExpressionDecoder());
    }

    @Override
    protected Expression buildResult() {
        return ff.divide(expresions.get(0), expresions.get(1));
    }

}
