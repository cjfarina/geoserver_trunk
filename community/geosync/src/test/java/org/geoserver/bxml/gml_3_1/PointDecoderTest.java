package org.geoserver.bxml.gml_3_1;

import java.io.ByteArrayInputStream;

import org.geoserver.bxml.BxmlTestSupport;
import org.geotools.referencing.CRS;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;

public class PointDecoderTest extends BxmlTestSupport {

    public void testPointCoord() throws Exception {

        String xyz = "<Point xmlns=\"http://www.opengis.net/gml\"><coord><X>1</X><Y>1</Y><Z>1</Z></coord></Point>";

        BxmlStreamReader reader = getXmlReader(new ByteArrayInputStream(xyz.getBytes()));
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

}
