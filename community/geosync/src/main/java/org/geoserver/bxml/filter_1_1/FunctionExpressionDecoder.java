package org.geoserver.bxml.filter_1_1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.geotools.filter.v1_1.OGC;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.expression.Expression;

import static org.geotools.filter.v1_1.OGC.Function;
import static org.geotools.filter.v1_1.OGC.FunctionNameType;

public class FunctionExpressionDecoder extends ExpressionLinkDecoder {

    public static final QName name = new QName(OGC.NAMESPACE, "name");

    private final List<Expression> expresions = new ArrayList<Expression>();
    
    private String functionName = null;
    
    public FunctionExpressionDecoder() {
        super(Function, new FunctionExpressionDecoder());
    }
    
    @Override
    protected void decodeElement(final BxmlStreamReader r) throws IOException {
        expresions.add(new ExpressionChainDecoder().decode(r));
    }
    
    @Override
    protected void decodeAttributtes(BxmlStreamReader r, Map<QName, String> attributes)
            throws IOException {
        functionName = attributes.get(name);
    }
    
    @Override
    protected Expression buildResult() {
        return ff.function(functionName, (Expression[])expresions.toArray());
    }
}
