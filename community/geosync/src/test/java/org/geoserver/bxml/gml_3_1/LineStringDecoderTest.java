package org.geoserver.bxml.gml_3_1;

import org.geoserver.bxml.BxmlTestSupport;
import org.geotools.referencing.CRS;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.LineString;

public class LineStringDecoderTest extends BxmlTestSupport {

    public void testLinePos() throws Exception {
        BxmlStreamReader reader = getReader("lineStringPos");
        reader.nextTag();
        LineStringDecoder lineDecoder = new LineStringDecoder();

        LineString line = (LineString) lineDecoder.decode(reader);
        Object userData = line.getUserData();
        assertEquals(CRS.decode("urn:ogc:def:crs:EPSG::900913"), userData);
        assertNotNull(line);
        testLineString(new double[][] { { 5, 7 }, { 9, 10 }, { 11, 15 }, { 13, 17 } }, line);
        reader.close();
    }

    public void testLinePosList() throws Exception {
        BxmlStreamReader reader = getReader("lineStringPosList");
        reader.nextTag();
        LineStringDecoder lineDecoder = new LineStringDecoder();

        LineString line = (LineString) lineDecoder.decode(reader);
        Object userData = line.getUserData();
        assertEquals(CRS.decode("urn:ogc:def:crs:EPSG::900913"), userData);
        assertNotNull(line);
        testLineString(new double[][] { { 15, 16 }, { 17, 18 }, { 19, 20 }, { 21, 22 } }, line);
        reader.close();
    }

    public void testLinePosList2() throws Exception {
        BxmlStreamReader reader = getReader("lineStringPosList2");
        reader.nextTag();
        LineStringDecoder lineDecoder = new LineStringDecoder();

        LineString line = (LineString) lineDecoder.decode(reader);
        Object userData = line.getUserData();
        assertEquals(CRS.decode("urn:ogc:def:crs:EPSG::900913"), userData);
        assertNotNull(line);
        testLineString(new double[][] { { 15, 16, 17 }, { 18, 19, 20 }, { 21, 22, 23 } }, line);
        reader.close();
    }

    public void testLineStringCoordinate() throws Exception {
        BxmlStreamReader reader = getReader("lineStringCoordinate");
        reader.nextTag();
        LineStringDecoder lineDecoder = new LineStringDecoder();

        LineString line = (LineString) lineDecoder.decode(reader);
        Object userData = line.getUserData();
        assertEquals(CRS.decode("urn:ogc:def:crs:EPSG::900913"), userData);
        assertNotNull(line);
        testLineString(new double[][] { { 45.67, 88.56 }, { 55.56, 89.44 }, { 56.34, 32.98 } },
                line);
        reader.close();
    }

    public void testLineStringCoordinate2() throws Exception {
        BxmlStreamReader reader = getReader("lineStringCoordinate2");
        reader.nextTag();
        LineStringDecoder lineDecoder = new LineStringDecoder();

        LineString line = (LineString) lineDecoder.decode(reader);
        Object userData = line.getUserData();
        assertEquals(CRS.decode("urn:ogc:def:crs:EPSG::900913"), userData);
        assertNotNull(line);
        testLineString(new double[][] { { 45.67, 88.56 }, { 55.56, 89.44 }, { 56.34, 32.98 } },
                line);
        reader.close();
    }
}
