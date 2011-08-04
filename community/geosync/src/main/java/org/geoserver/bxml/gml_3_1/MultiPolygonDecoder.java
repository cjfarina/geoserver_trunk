package org.geoserver.bxml.gml_3_1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.geoserver.bxml.SequenceDecoder;
import org.geotools.gml2.GML;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class MultiPolygonDecoder extends AbstractGeometryDecoder<Geometry> {

    public MultiPolygonDecoder() {
        super(GML.MultiPolygon);
    }

    @Override
    protected MultiPolygon decodeInternal(BxmlStreamReader r, QName name) throws Exception {
        SequenceDecoder<Geometry> seq = new SequenceDecoder<Geometry>(1, Integer.MAX_VALUE);
        seq.add(new GeometryMemberDecoder(GML.polygonMember, new PolygonDecoder()), 1, 1);

        r.nextTag();
        Iterator<Geometry> iterator = seq.decode(r);
        List<Polygon> polygons = new ArrayList<Polygon>();
        while (iterator.hasNext()) {
            polygons.add((Polygon) iterator.next());

        }
        MultiPolygon multiPolygon = new GeometryFactory().createMultiPolygon(polygons
                .toArray(new Polygon[polygons.size()]));
        multiPolygon.setUserData(getCrs());
        return multiPolygon;
    }

}
