package org.geoserver.gss.internal.atom.decoders.filters;

import static org.geotools.filter.v1_1.OGC.PropertyIsEqualTo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geoserver.gss.internal.atom.decoders.expressions.ExpressionChainDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;

public class PropertyIsEqualToFilterDecoder extends FilterLinkDecoder {
    
    private final List<Expression> expresions = new ArrayList<Expression>();
    
    public PropertyIsEqualToFilterDecoder() {
        super(PropertyIsEqualTo, new PropertyIsBetweenFilterDecoder());
    }
    
    @Override
    protected void decodeElement(final BxmlStreamReader r) throws IOException {
        expresions.add(new ExpressionChainDecoder().decode(r));
    }

    @Override
    protected Filter buildResult() {
        return ff.equals(expresions.get(0), expresions.get(1));
    }

}