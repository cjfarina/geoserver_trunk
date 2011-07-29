package org.geoserver.bxml.filter_1_1;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.geoserver.bxml.SequenceDecoder;
import org.geoserver.bxml.base.SimpleDecoder;
import org.geoserver.bxml.filter_1_1.expression.ExpressionDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.springframework.util.Assert;

import com.google.common.collect.Iterators;

import static org.geotools.filter.v1_1.OGC.PropertyIsBetween;


public class PropertyIsBetweenFilterDecoder extends SimpleDecoder<Filter> {

    private SequenceDecoder<Expression> seq;

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public PropertyIsBetweenFilterDecoder() {
        super(PropertyIsBetween);
        seq = new SequenceDecoder<Expression>(3, 3);
        seq.add(new ExpressionDecoder(), 1, 1);
        seq.add(new BoundaryFilterDecoder(), 2, 2);
    }

    @Override
    public Filter decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, null, null);
        final QName name = r.getElementName();
        Assert.isTrue(canHandle(name));

        r.nextTag();

        final Iterator<Expression> exprIterator = seq.decode(r);
        final Expression[] expressions = Iterators.toArray(exprIterator, Expression.class);

        Assert.isTrue(expressions.length == 3);

        r.require(EventType.END_ELEMENT, name.getNamespaceURI(), name.getLocalPart());
        
        return ff.between(expressions[0], expressions[1], expressions[2]);
        
    }

}
