package org.geoserver.bxml.gml_3_1;

import org.geoserver.bxml.BxmlTestSupport;
import org.geotools.referencing.CRS;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.MultiPoint;

public class MultiPointDecoderTest extends BxmlTestSupport {

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

}
