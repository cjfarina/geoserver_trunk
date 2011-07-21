package org.geoserver.bxml.filter_1_1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.geoserver.bxml.BxmlTestSupport;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.xml.Parser;
import org.gvsig.bxml.adapt.stax.XmlStreamWriterAdapter;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.gvsig.bxml.stream.EncodingOptions;
import org.opengis.filter.Filter;

public class FilterDecoderTest extends BxmlTestSupport {

    FilterDecoder2 decoder;

    public void setUp() {
        decoder = new FilterDecoder2();
    }

    public void testFilterDecoder() throws Exception {

        final Filter expected = ECQL
                .toFilter("a = 'a1' OR NOT(b='b1' AND c<'c1' AND d>'d1' AND e<='e1' AND f>='f1')");

        BxmlStreamReader reader = getXmlReader(expected);
        reader.nextTag();

        Filter f = decoder.decode(reader);
        assertNotNull(f);
        assertEquals(expected.toString(), f.toString());
    }

    public void testFilterDecoder2() throws Exception {

        Object expected = new Parser(new OGCConfiguration()).parse(getClass().getResourceAsStream(
                "filter.xml"));

        BxmlStreamReader reader = getXmlReader("filter.xml");
        reader.nextTag();

        Filter f = decoder.decode(reader);
        assertNotNull(f);
        assertEquals(expected.toString(), f.toString());
    }

    public void testNot() throws Exception {

        Filter expected = ECQL
                .toFilter("NOT(b='b1' AND c<'c1' AND d>'d1' AND e<='e1' AND f>='f1')");

        BxmlStreamReader reader = getXmlReader(expected);
        reader.nextTag();

        Filter f = decoder.decode(reader);
        assertNotNull(f);
        assertEquals(expected.toString(), f.toString());
    }

    public void testPropertyIsEqualTo() throws Exception {

        final Filter expected = ECQL.toFilter("address = 'Address1'");
        BxmlStreamReader reader = getXmlReader(expected);
        reader.nextTag();

        Filter f = decoder.decode(reader);
        assertNotNull(f);
        assertEquals(expected.toString(), f.toString());
    }

    public void testBinaryLogicComparison() throws Exception {

        Filter expected = ECQL
                .toFilter("'and' = 'andValue1' AND (or1 = 'orvalue1' OR or2 = 'orvalue2')");
        BxmlStreamReader reader = getXmlReader(expected);
        reader.nextTag();

        Filter decoded = decoder.decode(reader);
        assertNotNull(decoded);
        assertEquals(expected.toString(), decoded.toString());

    }

    private BxmlStreamReader getXmlReader(final Filter expected) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        XMLStreamWriter staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
        BxmlStreamWriter w = new XmlStreamWriterAdapter(new EncodingOptions(), staxWriter);
        new FilterEncoder().encode(expected, w);

        byte[] buf = out.toByteArray();
        String string = new String(buf);
        string = "<Filter xmlns=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\">"
                + string + "</Filter>";

        ByteArrayInputStream in = new ByteArrayInputStream(string.getBytes("UTF-8"));
        return getXmlReader(in);
    }

}
