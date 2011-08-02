package org.geoserver.bxml.filter_1_1.expression;

import static org.geotools.filter.v1_1.OGC.Function;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.SequenceDecoder;
import org.geoserver.bxml.filter_1_1.AbstractTypeDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.springframework.util.Assert;

import com.google.common.collect.Iterators;

public class FunctionExpressionDecoder extends AbstractTypeDecoder<Expression> {

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public FunctionExpressionDecoder() {
        super(Function);
    }
    
    @Override
    public Expression decodeInternal(final BxmlStreamReader r, final QName name) throws Exception {

        final String functionName = r.getAttributeValue(null, "name");
        Assert.notNull(functionName, "Attribute 'name' not found in element Function");
        r.nextTag();
        SequenceDecoder<Expression> seq = new SequenceDecoder<Expression>();
        seq.add(new ExpressionDecoder(), 0, Integer.MAX_VALUE);

        Expression[] expressions = Iterators.toArray(seq.decode(r), Expression.class);

        return ff.function(functionName, expressions);
    }

    @Override
    public boolean canHandle(final QName name) {
        return Function.equals(name);
    }

    @Override
    public Set<QName> getTargets() {
        return Collections.singleton(Function);
    }
}
