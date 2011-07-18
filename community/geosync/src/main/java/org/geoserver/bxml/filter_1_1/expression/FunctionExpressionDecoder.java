package org.geoserver.bxml.filter_1_1.expression;

import static org.geotools.filter.v1_1.OGC.Function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.v1_1.OGC;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

public class FunctionExpressionDecoder extends AbstractDecoder<Expression> {

    public static final QName name = new QName(OGC.NAMESPACE, "name");

    private final List<Expression> expresions = new ArrayList<Expression>();

    private String functionName = null;
    
    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public FunctionExpressionDecoder() {
        super(Function);
    }

    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        expresions.add(new ExpressionDecoder().decode(r));
    }

    @Override
    protected void decodeAttributtes(BxmlStreamReader r, Map<QName, String> attributes)
            throws IOException {
        functionName = attributes.get(name);
    }

    @Override
    protected Expression buildResult() {
        return ff.function(functionName, (Expression[]) expresions.toArray());
    }
}
