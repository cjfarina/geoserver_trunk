package org.geoserver.bxml.gml_3_1;

import static org.geotools.gml3.GML.LinearRing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;

public class LinearRingDecoder extends GMLLinkDecoder {

    private List<Coordinate> coordinates = new ArrayList<Coordinate>();

    public LinearRingDecoder() {
        super(LinearRing);
    }

    @SuppressWarnings("unchecked")
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();
        CoordinatePostListParser coordinatePostListParser = new CoordinatePostListParser(name,
                getDimension());
        Object postList = coordinatePostListParser.decode(r);
        if (getCrs() == null) {
            setCrs(coordinatePostListParser.getCrs());
        }
        if (getDimension() == -1) {
            setDimension(coordinatePostListParser.getDimension());
        }
        coordinates.addAll((List<Coordinate>) postList);
    }

    protected void decodeAttributtes(final BxmlStreamReader r, Map<QName, String> attributes)
            throws Exception {
        super.decodeAttributtes(r, attributes);
    }

    @Override
    protected Geometry buildResult() {
        LinearRing linearRing = gf.createLinearRing(coordinates.toArray(new Coordinate[coordinates
                .size()]));
        linearRing.setUserData(getCrs());
        return linearRing;
    }

}
