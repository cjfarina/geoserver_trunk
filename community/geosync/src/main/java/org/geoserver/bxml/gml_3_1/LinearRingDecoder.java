package org.geoserver.bxml.gml_3_1;

import static org.geotools.gml3.GML.LinearRing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class LinearRingDecoder extends GMLLinkDecoder {

    private List<Coordinate> coordinates = new ArrayList<Coordinate>();

    public LinearRingDecoder() {
        super(LinearRing);
    }

    @SuppressWarnings("unchecked")
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();
        Object postList = new CoordinatePostListParser(name, getDimension()).decode(r);
        coordinates.addAll((List<Coordinate>)postList);
    }

    protected void decodeAttributtes(final BxmlStreamReader r, Map<QName, String> attributes)
            throws Exception {
        super.decodeAttributtes(r, attributes);
    }

    @Override
    protected Geometry buildResult() {
        return gf.createLinearRing(coordinates.toArray(new Coordinate[coordinates.size()]));
    }

}
