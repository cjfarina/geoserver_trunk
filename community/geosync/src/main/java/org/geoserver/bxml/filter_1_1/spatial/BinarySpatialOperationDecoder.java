package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.Contains;
import static org.geotools.filter.v1_1.OGC.Crosses;
import static org.geotools.filter.v1_1.OGC.Disjoint;
import static org.geotools.filter.v1_1.OGC.Equals;
import static org.geotools.filter.v1_1.OGC.Intersects;
import static org.geotools.filter.v1_1.OGC.Overlaps;
import static org.geotools.filter.v1_1.OGC.Touches;
import static org.geotools.filter.v1_1.OGC.Within;

import javax.xml.namespace.QName;

import org.geoserver.bxml.SequenceDecoder;
import org.geoserver.bxml.filter_1_1.AbstractTypeDecoder;
import org.geoserver.bxml.filter_1_1.expression.ExpressionDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

import com.google.common.collect.Iterators;

public class BinarySpatialOperationDecoder extends AbstractTypeDecoder<Filter> {

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public BinarySpatialOperationDecoder() {
        super(Equals, Disjoint, Touches, Within, Overlaps, Crosses, Intersects, Contains);
    }

    @Override
    protected Filter decodeInternal(final BxmlStreamReader r, final QName name) throws Exception {

        SequenceDecoder<Expression> seq = new SequenceDecoder<Expression>();
        seq.add(new ExpressionDecoder(), 2, 2);

        r.nextTag();
        Expression[] expressions = Iterators.toArray(seq.decode(r), Expression.class);
        r.require(EventType.END_ELEMENT, name.getNamespaceURI(), name.getLocalPart());
        // r.nextTag();

        Filter f;
        if (Equals.equals(name)) {
            f = ff.equal(expressions[0], expressions[1]);
        } else if (Disjoint.equals(name)) {
            f = ff.disjoint(expressions[0], expressions[1]);
        } else if (Touches.equals(name)) {
            f = ff.touches(expressions[0], expressions[1]);
        } else if (Within.equals(name)) {
            f = ff.within(expressions[0], expressions[1]);
        } else if (Overlaps.equals(name)) {
            f = ff.overlaps(expressions[0], expressions[1]);
        } else if (Crosses.equals(name)) {
            f = ff.crosses(expressions[0], expressions[1]);
        } else if (Intersects.equals(name)) {
            f = ff.intersects(expressions[0], expressions[1]);
        } else if (Contains.equals(name)) {
            f = ff.contains(expressions[0], expressions[1]);
        } else {
            throw new IllegalArgumentException(this.getClass().getName() + " can not decode "
                    + name);
        }
        return f;
    }

}
