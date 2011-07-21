package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.Beyond;
import static org.geotools.filter.v1_1.OGC.DWithin;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.geoserver.bxml.SequenceDecoder;
import org.geoserver.bxml.filter_1_1.AbstractTypeDecoder;
import org.geoserver.bxml.filter_1_1.expression.ExpressionDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

public class DistanceBufferFilterDecoder extends AbstractTypeDecoder<Filter> {

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public DistanceBufferFilterDecoder() {
        super(DWithin, Beyond);
    }

    @Override
    protected Filter decodeInternal(final BxmlStreamReader r, final QName name) throws Exception {
        r.nextTag();
        SequenceDecoder<Expression> seq = new SequenceDecoder<Expression>();
        seq.add(new ExpressionDecoder(), 2, 2);

        Iterator<Expression> expressions = seq.decode(r);

        Expression geometry1 = expressions.next();
        Expression geometry2 = expressions.next();

        r.nextTag();
        Distance d = new DistanceTypeDecoder().decode(r);
        double distance = d.getValue();
        String units = d.getUnits();
        r.nextTag();
        if (DWithin.equals(name)) {
            return ff.dwithin(geometry1, geometry2, distance, units);
        } else if (Beyond.equals(name)) {
            return ff.beyond(geometry1, geometry2, distance, units);
        }
        throw new IllegalArgumentException(name.toString());
    }
}
