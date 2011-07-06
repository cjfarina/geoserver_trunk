package org.geoserver.gss.internal.atom.decoders.expressions;

import static org.geotools.filter.v1_1.OGC.Literal;

import java.io.IOException;

import org.opengis.filter.expression.Expression;

public class LiteralExpressionDecoder extends ExpressionLinkDecoder {

    private String literalValue;
    
    public LiteralExpressionDecoder() {
        super(Literal, new PropertyNameExpressionDecoder());
    }
    
    @Override
    protected void setStringValue(String value) throws IOException {
        literalValue = value;
    }

    @Override
    protected Expression buildResult() {
        return ff.literal(literalValue);
    }

}
