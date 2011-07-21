package org.geoserver.bxml.filter_1_1.expression;

import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.ChoiceDecoder;
import org.geoserver.bxml.Decoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.opengis.filter.expression.Expression;
import org.springframework.util.Assert;

public class ExpressionDecoder implements Decoder<Expression> {

    private Decoder<Expression> chain;

    @SuppressWarnings("unchecked")
    public ExpressionDecoder() {
        this.chain = new ChoiceDecoder<Expression>(new ArithmeticOperatorDecoder(),
                new FunctionExpressionDecoder(), new LiteralExpressionDecoder(),
                new PropertyNameExpressionDecoder());
    }

    @Override
    public Expression decode(final BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, null, null);
        QName name = r.getElementName();
        Assert.isTrue(canHandle(name));

        Expression expression = chain.decode(r);
        r.require(EventType.END_ELEMENT, name.getNamespaceURI(), name.getLocalPart());
        return expression;
    }

    @Override
    public boolean canHandle(QName name) {
        return chain.canHandle(name);
    }

    @Override
    public Set<QName> getTargets() {
        return chain.getTargets();
    }
}
