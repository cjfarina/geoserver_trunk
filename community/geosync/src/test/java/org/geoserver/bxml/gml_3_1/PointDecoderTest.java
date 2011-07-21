package org.geoserver.bxml.gml_3_1;

import java.io.ByteArrayInputStream;

import org.geoserver.bxml.BxmlTestSupport;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.Geometry;

public class PointDecoderTest extends BxmlTestSupport {

    public void testPointCoord() throws Exception {

        String xyz = "<Point xmlns=\"http://www.opengis.net/gml\"><coord><X>1</X><Y>1</Y><Z>1</Z></coord></Point>";

        BxmlStreamReader reader = getXmlReader(new ByteArrayInputStream(xyz.getBytes()));
        reader.nextTag();

        PointDecoder pointDecoder = new PointDecoder();
        Geometry point = pointDecoder.decode(reader);
        assertNotNull(point);
    }

}
