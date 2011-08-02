package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.Beyond;
import static org.geotools.filter.v1_1.OGC.DWithin;

import javax.xml.namespace.QName;

import org.geoserver.bxml.filter_1_1.AbstractTypeDecoder;
import org.geoserver.bxml.filter_1_1.expression.ExpressionDecoder;
import org.geoserver.bxml.gml_3_1.GeometryDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.Geometry;

public class DistanceBufferFilterDecoder extends AbstractTypeDecoder<Filter> {

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public DistanceBufferFilterDecoder() {
        super(DWithin, Beyond);
    }

    @Override
    protected Filter decodeInternal(final BxmlStreamReader r, final QName name) throws Exception {
        r.nextTag();
        Expression expression = new ExpressionDecoder().decode(r);
        r.nextTag();
        Geometry geometry = new GeometryDecoder().decode(r);
        r.nextTag();
        Distance d = new DistanceTypeDecoder().decode(r);

        double distance = d.getValue();
        String units = d.getUnits();
        r.nextTag();
        if (DWithin.equals(name)) {
            return ff.dwithin(expression, ff.literal(geometry), distance, units);
        } else if (Beyond.equals(name)) {
            return ff.beyond(expression, ff.literal(geometry), distance, units);
        }
        throw new IllegalArgumentException(name.toString());
    }
}
