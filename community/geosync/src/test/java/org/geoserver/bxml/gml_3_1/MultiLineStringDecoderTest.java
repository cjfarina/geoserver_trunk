package org.geoserver.bxml.gml_3_1;

import org.geoserver.bxml.BxmlTestSupport;
import org.geotools.referencing.CRS;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class MultiLineStringDecoderTest extends BxmlTestSupport {

    public void testMultiPointCoordinates() throws Exception {
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
}
