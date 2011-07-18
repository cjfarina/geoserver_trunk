package org.geoserver.bxml.filter_1_1.expression;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.geoserver.bxml.filter_1_1.ChoiceDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.expression.Expression;

public class ExpressionDecoder implements Decoder<Expression> {

    private Decoder<Expression> chain;

    @SuppressWarnings("unchecked")
    public ExpressionDecoder() {
        this.chain = new ChoiceDecoder<Expression>(new ArithmeticOperatorDecoder(),
                new FunctionExpressionDecoder(), new LiteralExpressionDecoder(),
                new PropertyNameExpressionDecoder());
    }

    @Override
    public Expression decode(BxmlStreamReader r) throws Exception {
        return chain.decode(r);
    }

    @Override
    public Boolean canHandle(QName name) {
        return chain.canHandle(name);
    }
}
