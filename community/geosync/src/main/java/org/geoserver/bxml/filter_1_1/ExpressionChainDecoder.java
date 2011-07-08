package org.geoserver.bxml.filter_1_1;

import java.io.IOException;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.expression.Expression;

public class ExpressionChainDecoder {

    private LiteralExpressionDecoder link = new LiteralExpressionDecoder();
    
    public Expression decode(final BxmlStreamReader r) throws IOException{
        return link.decode(r);
    }
}
