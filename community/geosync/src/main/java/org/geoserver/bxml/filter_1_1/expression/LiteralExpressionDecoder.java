package org.geoserver.bxml.filter_1_1.expression;

import static org.geotools.filter.v1_1.OGC.Literal;

import org.opengis.filter.expression.Expression;

public class LiteralExpressionDecoder extends ExpressionLinkDecoder {

    private String literalValue;

    public LiteralExpressionDecoder() {
        super(Literal, new PropertyNameExpressionDecoder());
    }

    @Override
    protected void setStringValue(String value) throws Exception {
        literalValue = value;
    }

    @Override
    protected Expression buildResult() {
        return ff.literal(literalValue);
    }

}