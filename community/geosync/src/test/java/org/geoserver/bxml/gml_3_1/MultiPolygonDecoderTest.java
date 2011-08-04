package org.geoserver.bxml.gml_3_1;

import org.geoserver.bxml.BxmlTestSupport;
import org.geotools.referencing.CRS;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class MultiPolygonDecoderTest extends BxmlTestSupport {

    public void testMultiPointCoordinates() throws Exception {
        BxmlStreamReader reader = getReader("multiPolygonPos");
        reader.nextTag();
        MultiPolygonDecoder decoder = new MultiPolygonDecoder();

        MultiPolygon multiPolygon = (MultiPolygon) decoder.decode(reader);
        Object userData = multiPolygon.getUserData();
        assertEquals(CRS.decode("urn:ogc:def:crs:EPSG::900913"), userData);
        assertNotNull(multiPolygon);

        testLineString(new double[][] { { 0, 0 }, { 1, 0 }, { 1, 1 }, { 0, 1 }, { 0, 0 } },
                (LinearRing) ((Polygon) multiPolygon.getGeometryN(0)).getExteriorRing());

        testLineString(new double[][] { { 2, 3 }, { 9, 4 }, { 3, 5 }, { 2, 1 }, { 2, 3 } },
                (LinearRing) ((Polygon) multiPolygon.getGeometryN(1)).getExteriorRing());

        testLineString(new double[][] { { 1, 3 }, { 2, 2 }, { 4, 4 }, { 5, 7 }, { 1, 3 } },
                (LinearRing) ((Polygon) multiPolygon.getGeometryN(2)).getExteriorRing());

        reader.close();

    }

}
