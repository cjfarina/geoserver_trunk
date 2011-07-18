package org.geoserver.bxml.filter_1_1;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.geoserver.bxml.filter_1_1.expression.ExpressionDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.expression.Expression;

public class BoundaryFilterDecoder extends AbstractDecoder<Expression> {

    private Expression expression = null;

    public BoundaryFilterDecoder(final QName name) {
        super(name);
    }

    @Override
    protected void decodeElement(BxmlStreamReader r) throws Exception {
        expression = new ExpressionDecoder().decode(r);
    }

    @Override
    protected Expression buildResult() {
        return expression;
    }

}
