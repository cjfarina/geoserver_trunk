package org.geoserver.bxml.filter_1_1.spatial;

import javax.xml.namespace.QName;

import org.geoserver.bxml.filter_1_1.ExpressionChainDecoder;
import org.geoserver.bxml.filter_1_1.FilterLinkDecoder;
import org.geoserver.bxml.gml_3_1.GMLChainDecoder;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.v1_1.OGC;
import org.geotools.gml3.GML;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.Geometry;

public abstract class BinarySpatialOperationDecoder extends FilterLinkDecoder {

    protected String propertyName;

    protected Geometry geometry;

    public BinarySpatialOperationDecoder(QName name) {
        super(name);
    }
    
    public BinarySpatialOperationDecoder(final QName name, final FilterLinkDecoder filterLink) {
        super(name, filterLink);
    }

    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if (name.getNamespaceURI().equals(OGC.NAMESPACE)) {
            AttributeExpressionImpl expression = (AttributeExpressionImpl) new ExpressionChainDecoder()
                    .decode(r);
            propertyName = expression.getPropertyName();
        } else if (name.getNamespaceURI().equals(GML.NAMESPACE)) {
            geometry = new GMLChainDecoder().decode(r);
        }
    }

}
