package org.geoserver.bxml.atom;

import java.util.Date;

import org.geoserver.bxml.BxmlTestSupport;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.geotools.feature.type.DateUtil;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class EntryDecoderTest extends BxmlTestSupport {

    public void testDecodeEntry() throws Exception {
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
    }
}
