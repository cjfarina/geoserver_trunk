package org.geoserver.bxml.gml_3_1;

import static org.geotools.gml3.GML.Polygon;
import static org.geotools.gml3.GML.exterior;
import static org.geotools.gml3.GML.interior;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;

public class PolygonDecoder extends GMLLinkDecoder {

    private LinearRing shell;
    
    private List<LinearRing> holes = new ArrayList<LinearRing>();
    
    public PolygonDecoder() {
        super(Polygon, new LinearRingDecoder());
    }
    
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();
        
        LinearRingDecoder linearRingDecoder = new LinearRingDecoder();
        linearRingDecoder.setCrs(getCrs());
        linearRingDecoder.setDimension(getDimension());
        if(exterior.equals(name)){
            r.next();
            shell = (LinearRing)linearRingDecoder.decode(r);
        } else if(interior.equals(name)){
            r.next();
            holes.add((LinearRing)linearRingDecoder.decode(r));
        }
    }

    protected void decodeAttributtes(final BxmlStreamReader r, Map<QName, String> attributes)
            throws Exception {
        super.decodeAttributtes(r, attributes);
    }
    
    @Override
    protected Geometry buildResult() {
        LinearRing[] holesArray = new LinearRing[holes.size()];
        for (int i = 0; i < holesArray.length; i++) {
            holesArray[i] = holes.get(i);
        }
        Geometry geometry = gf.createPolygon(shell, holesArray);
        geometry.setUserData(getCrs());
        return geometry;
    }

}
