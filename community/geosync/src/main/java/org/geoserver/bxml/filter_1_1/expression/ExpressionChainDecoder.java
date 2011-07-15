package org.geoserver.bxml.filter_1_1.expression;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.expression.Expression;

public class ExpressionChainDecoder {

    private LiteralExpressionDecoder link = new LiteralExpressionDecoder();

    public Expression decode(final BxmlStreamReader r) throws Exception {
        return link.decode(r);
    }
}
