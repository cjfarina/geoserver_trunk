package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.Equals;

import javax.xml.namespace.QName;

import org.geoserver.bxml.filter_1_1.ExpressionChainDecoder;
import org.geoserver.bxml.filter_1_1.FilterLinkDecoder;
import org.geoserver.bxml.gml_3_1.GMLChainDecoder;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.v1_1.OGC;
import org.geotools.gml3.GML;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.geometry.Geometry;

public class EqualsDecoderFilter extends FilterLinkDecoder {

    private String propertyName;
    
    private com.vividsolutions.jts.geom.Geometry geometry;
    
    public EqualsDecoderFilter() {
        super(Equals);
    }

    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if(name.getNamespaceURI().equals(OGC.NAMESPACE)){
            AttributeExpressionImpl expression = (AttributeExpressionImpl)new ExpressionChainDecoder().decode(r);
            propertyName = expression.getPropertyName();
        } else if(name.getNamespaceURI().equals(GML.NAMESPACE)){
            geometry = new GMLChainDecoder().decode(r);
        }
    }

    @Override
    protected Filter buildResult() {
        return ff.equals(ff.property(propertyName), ff.literal(geometry));
    }
}
