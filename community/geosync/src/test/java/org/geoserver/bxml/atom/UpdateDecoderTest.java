package org.geoserver.bxml.atom;

import java.io.InputStream;
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.wfs.PropertyType;
import net.opengis.wfs.UpdateElementType;

import org.geoserver.bxml.atom.FeedDecoder;
import org.geoserver.gss.internal.atom.ContentImpl;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.geoserver.gss.internal.atom.FeedImpl;
import org.geotools.filter.FidFilterImpl;
import org.geotools.referencing.CRS;
import org.gvsig.bxml.stream.BxmlFactoryFinder;
import org.gvsig.bxml.stream.BxmlInputFactory;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

@Deprecated
public class UpdateDecoderTest extends BXMLDecoderTest {

    public void testFeedDecodeUpdate() throws Exception {

        final InputStream input = getClass().getResourceAsStream(
        "/test-data/gss/1.0.0/examples/transactions/update.bxml");
        BxmlStreamReader reader;
        BxmlInputFactory inputFactory = BxmlFactoryFinder.newInputFactory();
        inputFactory.setNamespaceAware(true);
        reader = inputFactory.createScanner(input);
        //BxmlStreamReader reader = super.getXmlReader("update.xml");
        reader.nextTag();
        FeedDecoder feedDecoder = new FeedDecoder();
        FeedImpl feed = feedDecoder.decode(reader);

        assertEquals(new Long(50), feed.getMaxEntries());

        assertEquals("1ea12197b04bc0990216f1bfea04fc1c05ba0aab", feed.getId());
        EntryImpl entry = feed.getEntry().next();

        assertEquals("bf5043bab0b7ed55358d4ef2909103d0d50ba276", entry.getId());

        assertNotNull(entry.getContent());
        ContentImpl content1 = entry.getContent();

        UpdateElementType updateElement = (UpdateElementType) content1.getValue();
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

        Polygon where = (Polygon) entry.getWhere();
        LineString exteriorRing2 = where.getExteriorRing();
        testLineRing(exteriorRing2, new double[][] { { 10.1, 10.5 }, { 21.5, 23.5 },
                { 26.5, 28.7 }, { 10.1, 10.5 } });

        reader.close();
    }
}
