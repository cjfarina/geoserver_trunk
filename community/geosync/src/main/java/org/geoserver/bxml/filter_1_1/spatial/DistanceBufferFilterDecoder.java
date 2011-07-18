package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.Beyond;
import static org.geotools.filter.v1_1.OGC.DWithin;

import javax.xml.namespace.QName;

import org.geoserver.bxml.filter_1_1.ListDecoder;
import org.geoserver.bxml.filter_1_1.expression.ExpressionDecoder;
import org.geoserver.bxml.gml_3_1.GMLChainDecoder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.v1_1.OGC;
import org.geotools.gml3.GML;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.Geometry;

public class DistanceBufferFilterDecoder extends ListDecoder<Filter> {

    protected Expression expression;

    protected Geometry geometry;

    protected Distance distance;
    
    protected static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    public DistanceBufferFilterDecoder() {
        add(DWithin);
        add(Beyond);
    }

    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if (DistanceFilterDecoder.Distance.equals(name)) {
            distance = new DistanceFilterDecoder().decode(r);
        } else if (name.getNamespaceURI().equals(OGC.NAMESPACE)) {
            expression = new ExpressionDecoder().decode(r);
        } else if (name.getNamespaceURI().equals(GML.NAMESPACE)) {
            geometry = (Geometry) new GMLChainDecoder().decode(r);
        }
    }
    
    @Override
    protected Filter buildResult() {
        
        if(DWithin.equals(name)){
            return ff.dwithin(expression, ff.literal(geometry), distance.getValue(),
                    distance.getUnits());
        }
        
        if(Beyond.equals(name)){
            return ff
            .beyond(expression, ff.literal(geometry), distance.getValue(), distance.getUnits());
        }
        
        throw new IllegalArgumentException(this.getClass().getName() + " can not decode " + name);
    }
}
