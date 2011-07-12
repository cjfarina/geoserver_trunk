package org.geoserver.bxml.gml_3_1;


import static org.geotools.gml3.GML.LinearRing;
import static org.geotools.gml3.GML.posList;

import java.util.Map;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;
import javax.xml.namespace.QName;

public class LinearRingDecoder extends GMLLinkDecoder {

    private Coordinate[] coordinates;

    public LinearRingDecoder() {
        super(LinearRing);
    }
    
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();
        
        if(posList.equals(name)){
            //parseLinearRing(crsDimension, crs);
        }
        
    }

    protected void decodeAttributtes(final BxmlStreamReader r, Map<QName, String> attributes)
            throws Exception {
        super.decodeAttributtes(r, attributes);
    }
    
    @Override
    protected Geometry buildResult() {
        return gf.createLinearRing(coordinates);
    }

}
