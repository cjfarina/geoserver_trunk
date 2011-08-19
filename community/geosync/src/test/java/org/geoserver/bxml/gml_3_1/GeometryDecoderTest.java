package org.geoserver.bxml.gml_3_1;

import java.io.ByteArrayInputStream;

import junit.framework.Test;

import org.geoserver.bxml.BxmlTestSupport;
import org.geotools.referencing.CRS;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryDecoderTest extends BxmlTestSupport {

    public void testLinearRingPos() throws Exception {
        BxmlStreamReader reader = getReader("linearRingPos");
        reader.nextTag();
        LinearRingDecoder lineDecoder = new LinearRingDecoder();

        LinearRing line = (LinearRing) lineDecoder.decode(reader);
        Object userData = line.getUserData();
        assertEquals(CRS.decode("urn:ogc:def:crs:EPSG::900913"), userData);
        assertNotNull(line);
        testLineString(new double[][] { { 5, 7 }, { 9, 10 }, { 11, 15 }, { 13, 17 }, { 5, 7 } },
                line);
        reader.close();
    }

    public void testLinearRingPosList() throws Exception {
        BxmlStreamReader reader = getReader("linearRingPosList");
        reader.nextTag();
        LinearRingDecoder lineDecoder = new LinearRingDecoder();

        LinearRing line = (LinearRing) lineDecoder.decode(reader);
        Object userData = line.getUserData();
        assertEquals(CRS.decode("urn:ogc:def:crs:EPSG::900913"), userData);
        assertNotNull(line);
        testLineString(
                new double[][] { { 15, 16 }, { 17, 18 }, { 19, 20 }, { 21, 22 }, { 15, 16 } }, line);
        reader.close();
    }

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

    public void testMultiLineStringCoordinates() throws Exception {
        BxmlStreamReader reader = getReader("multiLineStringCoordinates");
        reader.nextTag();
        MultiLineStringDecoder decoder = new MultiLineStringDecoder();

        MultiLineString multiLineString = (MultiLineString) decoder.decode(reader);
        Object userData = multiLineString.getUserData();
        assertEquals(CRS.decode("urn:ogc:def:crs:EPSG::900913"), userData);
        assertNotNull(multiLineString);

        testLineString(new double[][] { { 45.67, 88.56 }, { 55.56, 89.44 }, { 56.34, 32.98 } },
                (LineString) multiLineString.getGeometryN(0));
        testLineString(new double[][] { { 5.5, 7.9 }, { 5.6, 9.8 }, { 3.2, 3.8 } },
                (LineString) multiLineString.getGeometryN(1));
        testLineString(new double[][] { { 2.35, 2.15 }, { 3.26, 3.12 }, { 1.2, 3.21 } },
                (LineString) multiLineString.getGeometryN(2));
        testLineString(new double[][] { { 3.1, 2.15 }, { 21.1, 52.5 }, { 12.25, 32.5 } },
                (LineString) multiLineString.getGeometryN(3));
        reader.close();
    }

    public void testMultiPointCoordinates() throws Exception {
        BxmlStreamReader reader = getReader("multiPointCoordinate");
        reader.nextTag();
        MultiPointDecoder decoder = new MultiPointDecoder();

        MultiPoint multiPoint = (MultiPoint) decoder.decode(reader);
        Object userData = multiPoint.getUserData();
        assertEquals(CRS.decode("urn:ogc:def:crs:EPSG::900913"), userData);
        assertNotNull(multiPoint);
        testMultiPoint(new double[][] { { 2.079641, 45.001795 }, { 2.71833, 45.541131 },
                { 3.016384, 45.143725 }, { 0.930003, 45.001795 } }, multiPoint);
        reader.close();
    }

    public void testMultiPolygonCoordinates() throws Exception {
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

    public void testPointCoord() throws Exception {

        String xyz = "<Point xmlns=\"http://www.opengis.net/gml\"><coord><X>1</X><Y>1</Y><Z>1</Z></coord></Point>";

        BxmlStreamReader reader = getReader(new ByteArrayInputStream(xyz.getBytes()));
        reader.nextTag();

        PointDecoder pointDecoder = new PointDecoder();
        Geometry point = pointDecoder.decode(reader);
        assertNotNull(point);
        reader.close();
    }

    public void testPointCoord2() throws Exception {
        BxmlStreamReader reader = getReader("pointCoord");
        reader.nextTag();
        PointDecoder pointDecoder = new PointDecoder();
        Geometry point = pointDecoder.decode(reader);
        assertNotNull(point);
        assertEquals(1.0, point.getCoordinate().x);
        assertEquals(2.0, point.getCoordinate().y);
        assertEquals(3.0, point.getCoordinate().z);
        reader.close();
    }

    public void testPointPos() throws Exception {
        BxmlStreamReader reader = getReader("pointPos");
        reader.nextTag();
        PointDecoder pointDecoder = new PointDecoder();

        Geometry point = pointDecoder.decode(reader);
        Object userData = point.getUserData();
        assertEquals(CRS.decode("urn:ogc:def:crs:EPSG::900913"), userData);
        System.out.println(userData);
        assertNotNull(point);
        assertEquals(-5.33, point.getCoordinate().x);
        assertEquals(2.43, point.getCoordinate().y);
        assertEquals(9.556, point.getCoordinate().z);
        reader.close();
    }

    public void testPointCoordinate() throws Exception {
        BxmlStreamReader reader = getReader("pointCoordinate");
        reader.nextTag();
        PointDecoder pointDecoder = new PointDecoder();
        Geometry point = pointDecoder.decode(reader);
        assertNotNull(point);
        assertEquals(45.67, point.getCoordinate().x);
        assertEquals(88.56, point.getCoordinate().y);
        reader.close();
    }

    public void testPolygon() throws Exception {
        BxmlStreamReader reader = getReader("polygon");
        reader.nextTag();
        PolygonDecoder decoder = new PolygonDecoder();

        Polygon polygon = (Polygon) decoder.decode(reader);
        Object userData = polygon.getUserData();
        assertEquals(CRS.decode("urn:ogc:def:crs:EPSG::900913"), userData);

        LineString exteriorRing = polygon.getExteriorRing();
        testLineString(new double[][] { { 5, 5 }, { 7, 7 }, { 11, 11 }, { 13, 13 }, { 5, 5 } },
                exteriorRing);

        LineString interior1 = polygon.getInteriorRingN(0);
        testLineString(new double[][] { { 2, 2 }, { 4, 4 }, { 6, 8 }, { 10, 12 }, { 2, 2 } },
                interior1);
        LineString interior2 = polygon.getInteriorRingN(1);
        testLineString(
                new double[][] { { 18, 20 }, { 22, 24 }, { 26, 28 }, { 29, 30 }, { 18, 20 } },
                interior2);

        reader.close();
    }
}
