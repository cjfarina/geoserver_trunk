package org.geoserver.gss.internal.atom;

import java.io.InputStream;
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.wfs.PropertyType;
import net.opengis.wfs.UpdateElementType;

import org.geoserver.bxml.atom.FeedDecoder;
import org.gvsig.bxml.stream.BxmlFactoryFinder;
import org.gvsig.bxml.stream.BxmlInputFactory;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class UpdateDecoderTest extends BXMLDecoderTest {

    public void testFeedDecodeUpdate() throws Exception {

        final InputStream input = getClass().getResourceAsStream(
                "/test-data/gss/1.0.0/examples/transactions/update.bxml");
        BxmlStreamReader reader;
        BxmlInputFactory inputFactory = BxmlFactoryFinder.newInputFactory();
        inputFactory.setNamespaceAware(true);
        reader = inputFactory.createScanner(input);
        FeedDecoder feedDecoder = new FeedDecoder();
        FeedImpl feed = feedDecoder.decode(reader);

        assertEquals(new Long(50), feed.getMaxEntries());

        assertEquals("1ea12197b04bc0990216f1bfea04fc1c05ba0aab", feed.getId());
        EntryImpl entry = feed.getEntry().next();

        assertEquals("bf5043bab0b7ed55358d4ef2909103d0d50ba276", entry.getId());

        assertNotNull(entry.getContent());
        ContentImpl content1 = entry.getContent();

        UpdateElementType updateElement = (UpdateElementType) content1.getValue();
        final List<PropertyType> property = updateElement.getProperty();
        PropertyType property1 = property.get(0);
        assertEquals(new QName("http://opengeo.org/osm", "building"), property1.getName());
        assertEquals("false", property1.getValue());
        
        updateElement.getFilter();
        reader.close();
    }
}
