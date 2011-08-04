package org.geoserver.bxml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.geoserver.bxml.base.DateDecoder;
import org.geoserver.bxml.base.DoubleDecoder;
import org.geoserver.bxml.base.StringDecoder;
import org.geotools.feature.type.DateUtil;
import org.gvsig.bxml.adapt.stax.XmlStreamWriterAdapter;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.gvsig.bxml.stream.EncodingOptions;

public class PrimitiveValuesDecoderTest extends BxmlTestSupport {

    public void testReadString() throws Exception {
        QName testElement = new QName("test");
        testDecodeValue(testElement, "ba d ef ghy", new StringDecoder(testElement));
    }

    public void testReadNumber() throws Exception {
        QName testElement = new QName("test");
        testDecodeValue(testElement, "25.0", 25.0, new DoubleDecoder(testElement));
        testDecodeValue(testElement, new Double(26.2), new DoubleDecoder(testElement));
        testDecodeValue(testElement, new Double(5445.542), new DoubleDecoder(testElement));
        testDecodeValue(testElement, new Float(32.21), 32.21, new DoubleDecoder(testElement));
        testDecodeValue(testElement, new Float(125.15), 125.15, new DoubleDecoder(testElement));
    }

    public void testReadDate() throws Exception {
        QName testElement = new QName("test");
        testDecodeValue(testElement, "2011-07-07T22:47:06.507Z",
                new Date(DateUtil.parseDateTime("2011-07-07T22:47:06.507Z")), new DateDecoder(
                        testElement));
    }

    private void testDecodeValue(QName testElement, Object value, Decoder decoder) throws Exception {
        testDecodeValue(testElement, value, value, decoder);
    }

    private void testDecodeValue(QName testElement, Object value, Object expected, Decoder decoder)
            throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        XMLStreamWriter staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
        BxmlStreamWriter w = new XmlStreamWriterAdapter(new EncodingOptions(), staxWriter);

        w.writeStartElement(testElement);
        if (value instanceof String) {
            w.writeValue((String) value);
        } else if (value instanceof Double) {
            w.writeValue((Double) value);
        } else if (value instanceof Float) {
            w.writeValue((Float) value);
        }

        w.writeEndElement();

        byte[] buf = out.toByteArray();
        String string = new String(buf);

        ByteArrayInputStream in = new ByteArrayInputStream(string.getBytes("UTF-8"));
        BxmlStreamReader r = getXmlReader(in);
        r.nextTag();
        Object decodedValue = decoder.decode(r);
        assertEquals(expected, decodedValue);
    }

}
