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

import org.geoserver.bxml.filter_1_1.ListDecoder;
import org.geoserver.bxml.filter_1_1.expression.ExpressionDecoder;
import org.geoserver.bxml.gml_3_1.GMLChainDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.v1_1.OGC;
import org.geotools.gml3.GML;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Geometry;

public class BinarySpatialOperationDecoder extends ListDecoder<Filter> {

    protected String propertyName;

    protected Geometry geometry;

    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public BinarySpatialOperationDecoder() {
        add(Equals);
        add(Disjoint);
        add(Touches);
        add(Within);
        add(Overlaps);
        add(Crosses);
        add(Intersects);
        add(Contains);
    }

    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if (name.getNamespaceURI().equals(OGC.NAMESPACE)) {
            AttributeExpressionImpl expression = (AttributeExpressionImpl) new ExpressionDecoder()
                    .decode(r);
            propertyName = expression.getPropertyName();
        } else if (name.getNamespaceURI().equals(GML.NAMESPACE)) {
            geometry = (Geometry) new GMLChainDecoder().decode(r);
        }
    }

    @Override
    protected Filter buildResult() {
        if (Equals.equals(name)) {
            return ff.equals(ff.property(propertyName), ff.literal(geometry));
        }

        if (Disjoint.equals(name)) {
            return ff.disjoint(ff.property(propertyName), ff.literal(geometry));
        }
        
        if (Touches.equals(name)) {
            return ff.touches(ff.property(propertyName), ff.literal(geometry));
        }
        
        if (Within.equals(name)) {
            return ff.within(ff.property(propertyName), ff.literal(geometry));
        }
        
        if (Overlaps.equals(name)) {
            return ff.overlaps(ff.property(propertyName), ff.literal(geometry));
        }
        
        if (Crosses.equals(name)) {
            return ff.crosses(ff.property(propertyName), ff.literal(geometry));
        }
        
        if (Intersects.equals(name)) {
            return ff.intersects(ff.property(propertyName), ff.literal(geometry));
        }
        
        if (Contains.equals(name)) {
            return ff.contains(ff.property(propertyName), ff.literal(geometry));
        }

        throw new IllegalArgumentException(this.getClass().getName() + " can not decode " + name);
    }

}
