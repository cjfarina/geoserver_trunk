package org.geoserver.bxml.filter_1_1.spatial;

import javax.xml.namespace.QName;

import org.geoserver.bxml.filter_1_1.ExpressionChainDecoder;
import org.geoserver.bxml.filter_1_1.FilterLinkDecoder;
import org.geoserver.bxml.gml_3_1.GMLChainDecoder;
import org.geotools.filter.v1_1.OGC;
import org.geotools.gml3.GML;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.Geometry;

public abstract class DistanceBufferFilterDecoder extends FilterLinkDecoder {

    protected Expression expression;

    protected Geometry geometry;

    protected Distance distance;
    
    public DistanceBufferFilterDecoder(QName name) {
        super(name);
    }
    
    public DistanceBufferFilterDecoder(final QName name, final FilterLinkDecoder filterLink) {
        super(name, filterLink);
    }
    
    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if (DistanceFilterDecoder.Distance.equals(name)) {
            distance = new DistanceFilterDecoder().decode(r);
        } else if (name.getNamespaceURI().equals(OGC.NAMESPACE)) {
            expression = new ExpressionChainDecoder().decode(r);
        } else if (name.getNamespaceURI().equals(GML.NAMESPACE)) {
            geometry = (Geometry)new GMLChainDecoder().decode(r);
        }
    }
}
