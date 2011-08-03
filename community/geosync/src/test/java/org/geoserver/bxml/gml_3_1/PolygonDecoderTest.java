package org.geoserver.bxml.gml_3_1;

import org.geoserver.bxml.BxmlTestSupport;
import org.geotools.referencing.CRS;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class PolygonDecoderTest extends BxmlTestSupport {

    public void testPolygon() throws Exception {
        BxmlStreamReader reader = getReader("polygon");
        reader.nextTag();
        PolygonDecoder decoder = new PolygonDecoder();

        Polygon polygon = (Polygon) decoder.decode(reader);
        Object userData = polygon.getUserData();
        assertEquals(CRS.decode("urn:ogc:def:crs:EPSG::900913"), userData);

        LineString exteriorRing = polygon.getExteriorRing();
        testLineString(exteriorRing, new double[][] { { 5, 5 }, { 7, 7 }, { 11, 11 }, { 13, 13 },
                { 5, 5 } });
        
        LineString interior1 = polygon.getInteriorRingN(0);
        testLineString(interior1, new double[][] { { 2, 2 }, { 4, 4}, { 6, 8 }, { 10, 12 },
                { 2, 2 } });
        LineString interior2 = polygon.getInteriorRingN(1);
        testLineString(interior2, new double[][] { { 18, 20 }, { 22, 24}, { 26, 28 }, { 29, 30 },
                { 18, 20 } });

        reader.close();
    }

}
