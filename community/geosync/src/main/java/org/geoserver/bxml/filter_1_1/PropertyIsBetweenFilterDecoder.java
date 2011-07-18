package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.PropertyIsBetween;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.geoserver.bxml.filter_1_1.expression.ExpressionDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.v1_1.OGC;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

public class PropertyIsBetweenFilterDecoder extends AbstractDecoder<Filter> {

    private final List<Expression> expresions = new ArrayList<Expression>();

    public static final QName LowerBoundary = new QName(OGC.NAMESPACE, "LowerBoundary");

    public static final QName UpperBoundary = new QName(OGC.NAMESPACE, "UpperBoundary");

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public PropertyIsBetweenFilterDecoder() {
        super(PropertyIsBetween);
    }

    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if (LowerBoundary.equals(name)) {
            expresions.add(new BoundaryFilterDecoder(LowerBoundary).decode(r));
        } else if (UpperBoundary.equals(name)) {
            expresions.add(new BoundaryFilterDecoder(UpperBoundary).decode(r));
        } else {
            expresions.add(new ExpressionDecoder().decode(r));
        }

    }

    @Override
    protected Filter buildResult() {
        return ff.between(expresions.get(0), expresions.get(1), expresions.get(2));
    }
}
