package org.geoserver.bxml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.geoserver.bxml.base.StringDecoder;
import org.gvsig.bxml.adapt.stax.XmlStreamWriterAdapter;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.gvsig.bxml.stream.EncodingOptions;

public class PrimitiveValuesDecoderTest extends BxmlTestSupport {

    public void testReadString() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        XMLStreamWriter staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
        BxmlStreamWriter w = new XmlStreamWriterAdapter(new EncodingOptions(), staxWriter);

        QName testElement = new QName("test");
        w.writeStartElement(testElement);
        String value = "ba d ef ghy";
        w.writeValue(value);
        w.writeEndElement();

        byte[] buf = out.toByteArray();
        String string = new String(buf);

        ByteArrayInputStream in = new ByteArrayInputStream(string.getBytes("UTF-8"));
        BxmlStreamReader r = getXmlReader(in);
        r.nextTag();
        String decodedValue = new StringDecoder(testElement).decode(r);
        assertEquals(value, decodedValue);
    }
}
