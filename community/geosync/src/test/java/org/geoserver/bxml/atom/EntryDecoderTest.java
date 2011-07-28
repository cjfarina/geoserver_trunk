package org.geoserver.bxml.atom;

import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.wfs.DeleteElementType;

import org.geoserver.bxml.BxmlTestSupport;
import org.geoserver.gss.internal.atom.CategoryImpl;
import org.geoserver.gss.internal.atom.ContentImpl;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.geoserver.gss.internal.atom.LinkImpl;
import org.geoserver.gss.internal.atom.PersonImpl;
import org.geotools.feature.type.DateUtil;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.LiteralExpressionImpl;
import org.geotools.filter.text.ecql.ECQL;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class EntryDecoderTest extends BxmlTestSupport {

    public void testDecodeEntry1() throws Exception {
        BxmlStreamReader reader = super.getXmlReader("entry1.xml");
        EntryDecoder decoder = new EntryDecoder();

        reader.nextTag();
        EntryImpl entry = decoder.decode(reader);
        assertNotNull(entry);

        assertEquals("212c812cde528ad912f6eda535c631e6f2e932c9", entry.getId());
        assertEquals("Insert of Feature building.19484", entry.getTitle());
        assertEquals("new building", entry.getSummary());
        assertEquals(new Date(DateUtil.parseDateTime("2011-07-07T22:47:06.507Z")),
                entry.getUpdated());
        assertEquals(1, entry.getAuthor().size());
        assertEquals("gabriel", entry.getAuthor().get(0).getName());
        assertEquals(1, entry.getContributor().size());
        assertEquals("geoserver", entry.getContributor().get(0).getName());
        reader.close();
    }

    public void testDecodeEntry2() throws Exception {
        BxmlStreamReader reader = super.getXmlReader("entry2.xml");
        EntryDecoder decoder = new EntryDecoder();

        reader.nextTag();
        EntryImpl entry = decoder.decode(reader);
        assertNotNull(entry);

        assertEquals("453e5a7ba8917ed3550e088d69ff1ac0aa6d1a4d", entry.getId());
        assertEquals("Delte of Feature planet_osm_point.100", entry.getTitle());

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
        final Filter expected = ECQL
                .toFilter("(( addresses = Adrees1 ) OR ( NOT (( depth BETWEEN 100 AND 200 ) AND ( street = Street1 )) ))");

        assertEquals(expected.toString(), filter.toString());
        Polygon where = (Polygon) entry.getWhere();
        LineString exteriorRing4 = where.getExteriorRing();
        testLineRing(exteriorRing4, new double[][] { { 10, 10 }, { 20, 20 }, { 30, 30 },
                { 40, 40 }, { 10, 10 } });

        reader.close();
    }

    public void testDecodeEntry3() throws Exception {
        BxmlStreamReader reader = super.getXmlReader("entry3.xml");
        EntryDecoder decoder = new EntryDecoder();

        reader.nextTag();
        EntryImpl entry = decoder.decode(reader);

        assertEquals("453e5a7ba8917ed3550e088d69ff1ac0aa6d1a4d", entry.getId());
        assertEquals("Delte of Feature planet_osm_point.100", entry.getTitle());
        assertEquals("Commit automatically accepted as it comes from a WFS transaction",
                entry.getSummary());
        assertEquals(new Date(DateUtil.parseDateTime("2012-05-28T18:27:15.466Z")),
                entry.getUpdated());

        assertEquals(2, entry.getAuthor().size());

        PersonImpl entryAuthor1 = entry.getAuthor().get(0);
        assertEquals("msanchez", entryAuthor1.getName());
        assertEquals("msanchez@example.com", entryAuthor1.getEmail());
        assertEquals("www.msanchez.org", entryAuthor1.getUri());

        PersonImpl entryAuthor2 = entry.getAuthor().get(1);
        assertEquals("pmolina", entryAuthor2.getName());
        assertEquals(null, entryAuthor2.getEmail());
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

        assertEquals(new Date(DateUtil.parseDateTime("2010-08-20T21:48:06.466Z")),
                entry.getPublished());

        assertEquals("This are the entry rights.", entry.getRights());
        assertEquals("Entry source.", entry.getSource());
        reader.close();
    }
}
