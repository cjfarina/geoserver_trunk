package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.Add;

import org.opengis.filter.expression.Expression;

public class AddExpressionDecoder extends ArithmeticOperator {
    
    public AddExpressionDecoder() {
        super(Add, new SubExpressionDecoder());
    }
    
    @Override
    protected Expression buildResult() {
        return ff.add(expresions.get(0), expresions.get(1));
    }


}
