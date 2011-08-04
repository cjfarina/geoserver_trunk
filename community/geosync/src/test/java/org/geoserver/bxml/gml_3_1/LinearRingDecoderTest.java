package org.geoserver.bxml.gml_3_1;

import org.geoserver.bxml.BxmlTestSupport;
import org.geotools.referencing.CRS;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.LinearRing;

public class LinearRingDecoderTest extends BxmlTestSupport {

    public void testLinePos() throws Exception {
        BxmlStreamReader reader = getReader("linearRingPos");
        reader.nextTag();
        LinearRingDecoder lineDecoder = new LinearRingDecoder();

        LinearRing line = (LinearRing) lineDecoder.decode(reader);
        Object userData = line.getUserData();
        assertEquals(CRS.decode("urn:ogc:def:crs:EPSG::900913"), userData);
        assertNotNull(line);
        testLineString(new double[][] { { 5, 7 }, { 9, 10 }, { 11, 15 }, { 13, 17 }, { 5, 7 } }, line);
        reader.close();
    }

    public void testLinePosList() throws Exception {
        BxmlStreamReader reader = getReader("linearRingPosList");
        reader.nextTag();
        LinearRingDecoder lineDecoder = new LinearRingDecoder();

        LinearRing line = (LinearRing) lineDecoder.decode(reader);
        Object userData = line.getUserData();
        assertEquals(CRS.decode("urn:ogc:def:crs:EPSG::900913"), userData);
        assertNotNull(line);
        testLineString(new double[][] { { 15, 16 }, { 17, 18 }, { 19, 20 }, { 21, 22 }, { 15, 16 } }, line);
        reader.close();
    }

}
