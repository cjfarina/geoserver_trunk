package org.geoserver.gss.internal.atom.decoders.expressions;

import java.io.IOException;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.expression.Expression;

public class ExpressionChainDecoder {

    private LiteralExpressionDecoder link = new LiteralExpressionDecoder();
    
    public Expression decode(final BxmlStreamReader r) throws IOException{
        return link.decode(r);
    }
}
