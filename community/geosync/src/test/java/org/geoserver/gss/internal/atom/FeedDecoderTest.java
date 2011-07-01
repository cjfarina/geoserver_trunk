package org.geoserver.gss.internal.atom;

import java.io.InputStream;
import java.util.Calendar;

import junit.framework.TestCase;

import org.geoserver.gss.internal.atom.decoders.FeedDecoder;
import org.gvsig.bxml.stream.BxmlFactoryFinder;
import org.gvsig.bxml.stream.BxmlInputFactory;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class FeedDecoderTest extends TestCase {

    
    //TODO: Test wrong date format
    
    public void testFeedDecodeDelete() {
        try {

            final InputStream input = getClass().getResourceAsStream(
                    "/test-data/gss/1.0.0/examples/transactions/delete.bxml");
            BxmlStreamReader reader;
            BxmlInputFactory inputFactory = BxmlFactoryFinder.newInputFactory();
            inputFactory.setNamespaceAware(true);
            reader = inputFactory.createScanner(input);
            FeedDecoder feedDecoder = new FeedDecoder();
            FeedImpl feed = feedDecoder.decode(reader);

            assertEquals(new Long(50), feed.getMaxEntries());
            assertEquals(new Long(1), feed.getStartPosition());
            
            assertEquals("01cbd610bbf9e37714980377ffc6600dc3fef24e", feed.getId());
            assertEquals("This is a feed title.", feed.getTitle());
            assertEquals("This is a feed subtitle.", feed.getSubtitle());
            assertEquals("This is an icon.", feed.getIcon());
            assertEquals("This is the rights.", feed.getRights());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(feed.getUpdated());
            
            assertEquals(2011, calendar.get(Calendar.YEAR));
            assertEquals(04, calendar.get(Calendar.MONTH));
            assertEquals(31, calendar.get(Calendar.DAY_OF_MONTH));
            
            assertEquals(2, feed.getAuthor().size());
            PersonImpl feedAuthor1 = feed.getAuthor().get(0);
            assertEquals("cfarina", feedAuthor1.getName());
            assertEquals("cfarina@host.com", feedAuthor1.getEmail());
            assertEquals("www.geoserver.org", feedAuthor1.getUri());
            
            PersonImpl feedAuthor2 = feed.getAuthor().get(1);
            assertEquals("mperez", feedAuthor2.getName());
            assertEquals("mperez@host.com", feedAuthor2.getEmail());
            assertEquals("", feedAuthor2.getUri());

            assertEquals(1, feed.getContributor().size());
            PersonImpl feedContributor1 = feed.getContributor().get(0);
            assertEquals("fperez", feedContributor1.getName());
            assertEquals("fperez@host.com", feedContributor1.getEmail());
            assertEquals("", feedContributor1.getUri());
            
            assertEquals(2, feed.getCategory().size());
            CategoryImpl feedCategory1 = feed.getCategory().get(0);
            assertEquals("term1", feedCategory1.getTerm());
            assertEquals("scheme1", feedCategory1.getScheme());
            
            CategoryImpl feedCategory2 = feed.getCategory().get(1);
            assertEquals("term2", feedCategory2.getTerm());
            assertEquals("scheme2", feedCategory2.getScheme());
            
            GeneratorImpl generator = feed.getGenerator();
            assertNotNull(generator);
            assertEquals("Generator Value 1", generator.getValue());
            assertEquals("generatoruri.org", generator.getUri());
            assertEquals("v1.3", generator.getVersion());
            
            assertEquals(3, feed.getLink().size());
            LinkImpl link1 = feed.getLink().get(0);
            assertEquals("title1", link1.getTitle());
            assertEquals("text/html", link1.getType());
            assertEquals("en", link1.getHreflang());
            assertEquals("http://example.org/", link1.getHref());
            
            LinkImpl link2 = feed.getLink().get(1);
            assertEquals("title2", link2.getTitle());
            assertEquals("alternate", link2.getRel());
            assertEquals("text/html", link2.getType());
            assertEquals("es", link2.getHreflang());
            assertEquals("http://example2.org/", link2.getHref());
            
            LinkImpl link3 = feed.getLink().get(2);
            assertEquals("alternate", link3.getRel());
            assertEquals("text/html", link3.getType());
            assertEquals("en", link3.getHreflang());
            assertEquals("http://example3.org/", link3.getHref());
            assertEquals(new Long(25), link3.getLength());
            
            assertTrue(feed.getEntry().hasNext());

            EntryImpl entry = feed.getEntry().next();

            assertEquals("453e5a7ba8917ed3550e088d69ff1ac0aa6d1a4d", entry.getId());
            assertEquals("Delte of Feature planet_osm_point.100", entry.getTitle());
            assertEquals("Commit automatically accepted as it comes from a WFS transaction",
                    entry.getSummary());

            /*assertEquals(2011, entry.getUpdated().getYear());
            assertEquals(05, entry.getUpdated().getMonth());
            assertEquals(31, entry.getUpdated().getDay());

            assertEquals(1, entry.getAuthor().size());
            assertEquals("groldan", entry.getAuthor().get(0).getName());

            assertEquals(1, entry.getContributor().size());
            assertEquals("geoserver", entry.getContributor().get(0).getName());*/
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
