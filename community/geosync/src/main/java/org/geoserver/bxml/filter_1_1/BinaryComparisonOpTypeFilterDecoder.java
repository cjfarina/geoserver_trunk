package org.geoserver.bxml.filter_1_1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.geoserver.bxml.filter_1_1.expression.ExpressionChainDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.expression.Expression;

public abstract class BinaryComparisonOpTypeFilterDecoder extends FilterLinkDecoder {

    protected final List<Expression> expresions = new ArrayList<Expression>();

    public BinaryComparisonOpTypeFilterDecoder(final QName name, final FilterLinkDecoder filterLink) {
        super(name, filterLink);
    }

    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        expresions.add(new ExpressionChainDecoder().decode(r));
    }
}
