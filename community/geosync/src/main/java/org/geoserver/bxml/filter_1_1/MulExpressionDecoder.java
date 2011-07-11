package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.Mul;

import org.opengis.filter.expression.Expression;

public class MulExpressionDecoder extends ArithmeticOperator {
    
    public MulExpressionDecoder() {
        super(Mul, new DivExpressionDecoder());
    }
    
    @Override
    protected Expression buildResult() {
        return ff.multiply(expresions.get(0), expresions.get(1));
    }

}
