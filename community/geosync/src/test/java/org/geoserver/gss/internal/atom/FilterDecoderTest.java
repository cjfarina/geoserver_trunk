package org.geoserver.gss.internal.atom;

import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;
import net.opengis.wfs.DeleteElementType;

import org.geoserver.bxml.atom.FeedDecoder;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.FidFilterImpl;
import org.geotools.filter.LiteralExpressionImpl;
import org.gvsig.bxml.stream.BxmlFactoryFinder;
import org.gvsig.bxml.stream.BxmlInputFactory;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.identity.Identifier;

public class FilterDecoderTest extends TestCase {

    public void testFilterDecoder(){
        try {

            final InputStream input = getClass().getResourceAsStream(
                    "/test-data/gss/1.0.0/examples/transactions/delete2.bxml");
            BxmlStreamReader reader;
            BxmlInputFactory inputFactory = BxmlFactoryFinder.newInputFactory();
            inputFactory.setNamespaceAware(true);
            reader = inputFactory.createScanner(input);
            FeedDecoder feedDecoder = new FeedDecoder();
            FeedImpl feed = feedDecoder.decode(reader);

            assertEquals(new Long(50), feed.getMaxEntries());
            assertNull(feed.getStartPosition());

            assertEquals("01cbd610bbf9e37714980377ffc6600dc3fef24e", feed.getId());
            EntryImpl entry = feed.getEntry().next();

            assertEquals("453e5a7ba8917ed3550e088d69ff1ac0aa6d1a4d", entry.getId());

            assertNotNull(entry.getContent());
            ContentImpl content1 = entry.getContent();

            DeleteElementType deleteElement = (DeleteElementType)content1.getValue();
            Filter filter = deleteElement.getFilter();
            assertNotNull(deleteElement.getFilter());
            FidFilterImpl identifierFilter = (FidFilterImpl)filter;
            
            int i = 0;
            for (Object id : identifierFilter.getIDs()) {
                assertEquals("planet_osm_point.10" + i, id);
                i++;
            }
            
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
