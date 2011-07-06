package org.geoserver.gss.internal.atom.decoders.filters;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.decoders.AbstractDecoder;
import org.geoserver.gss.internal.atom.decoders.expressions.ExpressionChainDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.expression.Expression;

public class BoundaryFilterDecoder extends AbstractDecoder<Expression>{

    private Expression expression = null;
    
    public BoundaryFilterDecoder(final QName name) {
        super(name);
    }
    
    @Override
    protected void decodeElement(BxmlStreamReader r) throws IOException {
        expression = new ExpressionChainDecoder().decode(r);
    }

    @Override
    protected Expression buildResult() {
        return expression;
    }

}
