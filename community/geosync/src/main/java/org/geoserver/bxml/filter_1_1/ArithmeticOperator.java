package org.geoserver.bxml.filter_1_1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.expression.Expression;

public abstract class ArithmeticOperator extends ExpressionLinkDecoder {

    protected final List<Expression> expresions = new ArrayList<Expression>();
    
    public ArithmeticOperator(final QName name, final ExpressionLinkDecoder filterLink) {
        super(name, filterLink);
    }
    
    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        expresions.add(new ExpressionChainDecoder().decode(r));
    }
    
    public ArithmeticOperator(final QName name) {
        super(name);
    }
}
