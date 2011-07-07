package org.geoserver.gss.internal.atom.decoders.filters;

import static org.geotools.filter.v1_1.OGC.PropertyIsBetween;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.decoders.expressions.ExpressionChainDecoder;
import org.geotools.filter.v1_1.OGC;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;

public class PropertyIsBetweenFilterDecoder extends FilterLinkDecoder {
    
    private final List<Expression> expresions = new ArrayList<Expression>();
    
    public static final QName LowerBoundary = new QName(OGC.NAMESPACE, "LowerBoundary");
    
    public static final QName UpperBoundary = new QName(OGC.NAMESPACE, "UpperBoundary");
    
    public PropertyIsBetweenFilterDecoder() {
        super(PropertyIsBetween);
    }
    
    @Override
    protected void decodeElement(final BxmlStreamReader r) throws IOException {
        QName name = r.getElementName();
        
        if(LowerBoundary.equals(name)){
            expresions.add(new BoundaryFilterDecoder(LowerBoundary).decode(r));
        } else if(UpperBoundary.equals(name)){
            expresions.add(new BoundaryFilterDecoder(UpperBoundary).decode(r));
        } else {
            expresions.add(new ExpressionChainDecoder().decode(r));
        }
        
    }

    @Override
    protected Filter buildResult() {
        return ff.between(expresions.get(0), expresions.get(1), expresions.get(2));
    }

}