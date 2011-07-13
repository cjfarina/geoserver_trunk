package org.geoserver.gss.internal.atom;

import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import javax.xml.namespace.QName;

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

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class DeleteDecoderTest extends BXMLDecoderTest {

    public void testFeedDecodeDelete() throws Exception {

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

        Calendar feedUpdated = Calendar.getInstance();
        feedUpdated.clear();
        feedUpdated.setTime(feed.getUpdated());

        assertEquals(2011, feedUpdated.get(Calendar.YEAR));
        assertEquals(04, feedUpdated.get(Calendar.MONTH));
        assertEquals(31, feedUpdated.get(Calendar.DAY_OF_MONTH));
        assertEquals(21 + getTimeZone(), feedUpdated.get(Calendar.HOUR_OF_DAY));
        assertEquals(48, feedUpdated.get(Calendar.MINUTE));
        assertEquals(06, feedUpdated.get(Calendar.SECOND));

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

        Calendar entryUpdated = Calendar.getInstance();
        entryUpdated.setTime(entry.getUpdated());
        assertEquals(2012, entryUpdated.get(Calendar.YEAR));
        assertEquals(04, entryUpdated.get(Calendar.MONTH));
        assertEquals(28, entryUpdated.get(Calendar.DAY_OF_MONTH));
        assertEquals(18 + getTimeZone(), entryUpdated.get(Calendar.HOUR_OF_DAY));
        assertEquals(27, entryUpdated.get(Calendar.MINUTE));
        assertEquals(15, entryUpdated.get(Calendar.SECOND));

        assertEquals(2, entry.getAuthor().size());

        PersonImpl entryAuthor1 = entry.getAuthor().get(0);
        assertEquals("msanchez", entryAuthor1.getName());
        assertEquals("msanchez@example.com", entryAuthor1.getEmail());
        assertEquals("www.msanchez.org", entryAuthor1.getUri());

        PersonImpl entryAuthor2 = entry.getAuthor().get(1);
        assertEquals("pmolina", entryAuthor2.getName());
        assertEquals("", entryAuthor2.getEmail());
        assertEquals("www.pmolina.org", entryAuthor2.getUri());

        assertEquals(1, entry.getContributor().size());

        PersonImpl entryContributor1 = entry.getContributor().get(0);
        assertEquals("contributor1", entryContributor1.getName());

        assertEquals(3, entry.getCategory().size());

        CategoryImpl entryCategory1 = entry.getCategory().get(0);
        assertEquals("categoryTerm1", entryCategory1.getTerm());
        assertEquals("categoryScheme1", entryCategory1.getScheme());

        CategoryImpl entryCategory2 = entry.getCategory().get(1);
        assertEquals("categoryTerm2", entryCategory2.getTerm());
        assertEquals(null, entryCategory2.getScheme());

        CategoryImpl entryCategory3 = entry.getCategory().get(2);
        assertEquals(null, entryCategory3.getTerm());
        assertEquals("categoryScheme3", entryCategory3.getScheme());

        assertEquals(2, entry.getLink().size());

        LinkImpl entryLink1 = entry.getLink().get(0);
        assertEquals("Entry Title 1", entryLink1.getTitle());
        assertEquals("text/html", entryLink1.getType());
        assertEquals("en", entryLink1.getHreflang());
        assertEquals("http://entryexample.org/", entryLink1.getHref());

        LinkImpl entryLink2 = entry.getLink().get(1);
        assertEquals("Entry Title 3", entryLink2.getTitle());
        assertEquals("alternate", entryLink2.getRel());
        assertEquals("text/html", entryLink2.getType());
        assertEquals("es", entryLink2.getHreflang());
        assertEquals("http://entryexample2.org/", entryLink2.getHref());

        Calendar entryPublished = Calendar.getInstance();
        entryPublished.setTime(entry.getPublished());
        assertEquals(2010, entryPublished.get(Calendar.YEAR));
        assertEquals(07, entryPublished.get(Calendar.MONTH));
        assertEquals(20, entryPublished.get(Calendar.DAY_OF_MONTH));

        assertEquals("This are the entry rights.", entry.getRights());
        assertEquals("Entry source.", entry.getSource());

        assertNotNull(entry.getContent());
        ContentImpl content1 = entry.getContent();
        assertEquals("type1", content1.getType());
        assertEquals("source1", content1.getSrc());

        DeleteElementType deleteElement = (DeleteElementType) content1.getValue();
        QName deleteTypeName = deleteElement.getTypeName();
        assertEquals("http://opengeo.org/osm", deleteTypeName.getNamespaceURI());
        assertEquals("planet_osm_point", deleteTypeName.getLocalPart());
        Filter filter = deleteElement.getFilter();
        assertNotNull(deleteElement.getFilter());
        Or orFilter = (Or) filter;

        List<Filter> orFilterChildrens = orFilter.getChildren();
        assertEquals(2, orFilterChildrens.size());

        PropertyIsEqualTo propertyIsEqualTo1 = (PropertyIsEqualTo) orFilterChildrens.get(0);
        assertEquals("addresses",
                ((AttributeExpressionImpl) propertyIsEqualTo1.getExpression1()).getPropertyName());
        assertEquals("Adrees1",
                ((LiteralExpressionImpl) propertyIsEqualTo1.getExpression2()).getValue());

        Not notFilter = (Not) orFilterChildrens.get(1);
        And andFilter = (And) notFilter.getFilter();

        List<Filter> andFilterChildrens = andFilter.getChildren();
        assertEquals(2, andFilterChildrens.size());

        PropertyIsBetween propertyIsBetween1 = (PropertyIsBetween) andFilterChildrens.get(0);
        assertEquals("depth",
                ((AttributeExpressionImpl) propertyIsBetween1.getExpression()).getPropertyName());
        assertEquals("100",
                ((LiteralExpressionImpl) propertyIsBetween1.getLowerBoundary()).getValue());
        assertEquals("200",
                ((LiteralExpressionImpl) propertyIsBetween1.getUpperBoundary()).getValue());

        PropertyIsEqualTo propertyIsEqualTo2 = (PropertyIsEqualTo) andFilterChildrens.get(1);
        assertEquals("street",
                ((AttributeExpressionImpl) propertyIsEqualTo2.getExpression1()).getPropertyName());
        assertEquals("Street1",
                ((LiteralExpressionImpl) propertyIsEqualTo2.getExpression2()).getValue());

        Polygon where = (Polygon)entry.getWhere();
        LineString exteriorRing4 = where.getExteriorRing();
        testLineRing(exteriorRing4, new float[][] { { 10, 10 }, { 20, 20 }, { 30, 30 }, { 40, 40 },
                { 10, 10 } });
        reader.close();

    }

    public void testFeedDecoderDelete2() throws Exception {

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

        DeleteElementType deleteElement = (DeleteElementType) content1.getValue();
        Filter filter = deleteElement.getFilter();
        assertNotNull(deleteElement.getFilter());
        FidFilterImpl identifierFilter = (FidFilterImpl) filter;

        int i = 0;
        for (Object id : identifierFilter.getIDs()) {
            assertEquals("planet_osm_point.10" + i, id);
            i++;
        }

        reader.close();

    }

}
