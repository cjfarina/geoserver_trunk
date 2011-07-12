package org.geoserver.bxml.gml_3_1;

import static org.geotools.gml3.GML.Polygon;
import static org.geotools.gml3.GML.srsName;
import static org.geotools.gml3.GML.interior;
import static org.geotools.gml3.GML.exterior;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.smartcardio.ATR;
import javax.xml.namespace.QName;

import org.geotools.gml3.GML;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.geometry.coordinate.Polygon;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.gml3.GML;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;

public class PolygonDecoder extends GMLLinkDecoder {

    private LinearRing shell;
    
    private List<LinearRing> holes = new ArrayList<LinearRing>();
    
    public PolygonDecoder() {
        super(Polygon);
    }
    
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();
        
        if(exterior.equals(name)){
            r.next();
            shell = (LinearRing)new LinearRingDecoder().decode(r);
        }
        if(interior.equals(name)){
            r.next();
            holes.add((LinearRing)new LinearRingDecoder().decode(r));
        }
    }

    protected void decodeAttributtes(final BxmlStreamReader r, Map<QName, String> attributes)
            throws Exception {
        super.decodeAttributtes(r, attributes);
    }
    
    @Override
    protected Geometry buildResult() {
        Geometry geometry = gf.createPolygon(shell, (LinearRing[])holes.toArray());
        geometry.setUserData(crs);
        return geometry;
    }

}
