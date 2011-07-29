package org.geoserver.bxml.wfs_1_1;

import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.wfs.PropertyType;
import net.opengis.wfs.UpdateElementType;

import org.geoserver.bxml.BxmlTestSupport;
import org.geotools.filter.FidFilterImpl;
import org.geotools.referencing.CRS;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class UpdateDecoderTest extends BxmlTestSupport {

    public void testFeedDecodeUpdate() throws Exception {

        BxmlStreamReader reader = super.getXmlReader("update1.xml");
        reader.nextTag();
        UpdateElementTypeDecoder decoder = new UpdateElementTypeDecoder();
        UpdateElementType updateElement = (UpdateElementType) decoder.decode(reader);

        @SuppressWarnings("unchecked")
        final List<PropertyType> property = updateElement.getProperty();
        PropertyType property1 = property.get(0);
        assertEquals(new QName("http://opengeo.org/osm", "building"), property1.getName());
        assertEquals("false", property1.getValue());

        PropertyType property2 = property.get(1);

        Polygon polygon = (Polygon) property2.getValue();

        LineString exteriorRing = polygon.getExteriorRing();
        testLineRing(exteriorRing, new double[][] { { -8421981.58, 5074017.82 },
                { -8421975.14, 5074027.33 }, { -8421933.13, 5073998.82 },
                { -8421939.57, 5073990.31 }, { -8421981.58, 5074017.82 } });

        CoordinateReferenceSystem crs = (CoordinateReferenceSystem) exteriorRing.getUserData();
        assertNotNull(crs);
        Integer epsCode = CRS.lookupEpsgCode(crs, true);
        assertEquals(new Integer(900913), epsCode);

        assertNotNull(updateElement.getFilter());
        FidFilterImpl identifierFilter = (FidFilterImpl) updateElement.getFilter();
        int i = 100;
        for (Object id : identifierFilter.getIDs()) {
            assertEquals("planet_osm_polygon." + i, id);
            i++;
        }
        reader.close();
    }
}
