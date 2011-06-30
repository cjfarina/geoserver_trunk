package org.geogit.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.geogit.api.ObjectId;
import org.gvsig.bxml.stream.BxmlFactoryFinder;
import org.gvsig.bxml.stream.BxmlInputFactory;
import org.gvsig.bxml.stream.BxmlOutputFactory;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.gvsig.bxml.stream.EventType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTWriter;

public class BLOBS {
    private BLOBS() {
        //
    }

    public static final String NAMESPACE = XMLConstants.NULL_NS_URI;

    public static final QName CONVERTED_FROM_ATT = new QName(NAMESPACE, "from");

    public static final QName CONVERTED_TO_STRING = new QName(NAMESPACE, "to_string");

    public static final QName GEOMETRY_WKB = new QName(NAMESPACE, "wkb");

    public static final QName LONG_ARRAY = new QName(NAMESPACE, "a_long");

    public static final QName INT_ARRAY = new QName(NAMESPACE, "a_int");

    public static final QName FLOAT_ARRAY = new QName(NAMESPACE, "a_float");

    public static final QName DOUBLE_ARRAY = new QName(NAMESPACE, "a_double");

    public static final QName CHAR_ARRAY = new QName(NAMESPACE, "a_char");

    public static final QName BYTE_ARRAY = new QName(NAMESPACE, "a_byte");

    public static final QName BOOLEAN_ARRAY = new QName(NAMESPACE, "a_boolean");

    public static final QName LONG = new QName(NAMESPACE, "long");

    public static final QName INT = new QName(NAMESPACE, "int");

    public static final QName BIGINT = new QName(NAMESPACE, "bigint");

    public static final QName FLOAT = new QName(NAMESPACE, "float");

    public static final QName DOUBLE = new QName(NAMESPACE, "double");

    public static final QName BIGDECIMAL = new QName(NAMESPACE, "bigdec");

    public static final QName BYTE = new QName(NAMESPACE, "byte");

    public static final QName BOOLEAN = new QName(NAMESPACE, "boolean");

    public static final QName STRING = new QName(NAMESPACE, "string");

    public static final QName NULL = new QName(NAMESPACE, "null");

    public static final QName FEATURE = new QName(NAMESPACE, "feature");

    public static final QName COMMIT = new QName(NAMESPACE, "commit");

    public static final QName TREE = new QName(NAMESPACE, "tree");

    public static final QName BUCKET = new QName(NAMESPACE, "bucket");

    public static final QName ENTRY = new QName(NAMESPACE, "entry");

    public static final QName PARENT_IDS = new QName(NAMESPACE, "parentids");

    public static final QName OBJECT_ID = new QName(NAMESPACE, "objectid");

    public static final QName AUTHOR = new QName(NAMESPACE, "author");

    public static final QName COMMITTER = new QName(NAMESPACE, "committer");

    public static final QName MESSAGE = new QName(NAMESPACE, "message");

    public static final QName TIMESTAMP = new QName(NAMESPACE, "timestamp");

    public static final BxmlInputFactory cachedInputFactory = BxmlFactoryFinder.newInputFactory();

    public static final BxmlOutputFactory cachedOutputFactory = BxmlFactoryFinder
            .newOutputFactory();

    public static ObjectId parseObjectId(final BxmlStreamReader r) throws IOException {
        r.require(EventType.START_ELEMENT, null, null);
        QName name = r.getElementName();
        if (NULL.equals(name)) {
            r.nextTag();
            r.require(EventType.END_ELEMENT, NULL.getNamespaceURI(), NULL.getLocalPart());
            return null;
        }
        r.require(EventType.START_ELEMENT, OBJECT_ID.getNamespaceURI(), OBJECT_ID.getLocalPart());
        r.next();
        r.require(EventType.VALUE_BYTE, null, null);
        final int valueSize = r.getValueCount();
        byte[] raw = new byte[valueSize];
        r.getValue(raw, 0, valueSize);
        r.next();
        r.require(EventType.END_ELEMENT, OBJECT_ID.getNamespaceURI(), OBJECT_ID.getLocalPart());

        return new ObjectId(raw);
    }

    public static void writeObjectId(final BxmlStreamWriter w, final ObjectId id)
            throws IOException {
        if (id == null) {
            w.writeStartElement(NULL);
        } else {
            w.writeStartElement(OBJECT_ID);
            byte[] rawId = id.getRawValue();
            w.writeValue(rawId, 0, rawId.length);
        }
        w.writeEndElement();
    }

    public static void writeString(final BxmlStreamWriter w, final QName name, final String value)
            throws IOException {
        w.writeStartElement(name);
        {
            if (value == null) {
                w.writeStartElement(NULL);
            } else {
                w.writeStartElement(STRING);
                w.writeValue(value);
            }
            w.writeEndElement();
        }
        w.writeEndElement();
    }

    public static String parseString(final BxmlStreamReader r) throws IOException {
        r.require(EventType.START_ELEMENT, null, null);
        if (NULL.equals(r.getElementName())) {
            r.nextTag();
            r.require(EventType.END_ELEMENT, NULL.getNamespaceURI(), NULL.getLocalPart());
            return null;
        }
        r.require(EventType.START_ELEMENT, STRING.getNamespaceURI(), STRING.getLocalPart());
        r.next();
        r.require(EventType.VALUE_STRING, null, null);
        String value = r.getStringValue();
        r.next();
        r.require(EventType.END_ELEMENT, STRING.getNamespaceURI(), STRING.getLocalPart());
        return value;
    }

    public static long parseLong(final BxmlStreamReader r) throws IOException {
        r.require(EventType.START_ELEMENT, LONG.getNamespaceURI(), LONG.getLocalPart());
        r.next();
        r.require(EventType.VALUE_LONG, null, null);
        long longVal = r.getLongValue();
        r.next();
        r.require(EventType.END_ELEMENT, LONG.getNamespaceURI(), LONG.getLocalPart());

        return longVal;
    }

    public static void print(final byte[] rawBlob, final PrintStream out) throws IOException {
        print(new ByteArrayInputStream(rawBlob), out);
    }

    public static void print(final InputStream rawBlob, final PrintStream out) throws IOException {
        final BxmlInputFactory inputFactory = BLOBS.cachedInputFactory;
        final BxmlStreamReader reader = inputFactory.createScanner(rawBlob);

        EventType event;
        QName elementName = null;
        boolean hasvalue = false;
        boolean needsClosure = false;
        int ilevel = 0;
        while (!(event = reader.next()).equals(EventType.END_DOCUMENT)) {
            if (event == EventType.START_ELEMENT) {
                if (needsClosure) {
                    out.print(">");
                }
                hasvalue = false;
                needsClosure = true;
                elementName = reader.getElementName();
                if (ilevel > 0) {
                    out.print('\n');
                }
                for (int i = 0; i < ilevel; i++) {
                    out.print(' ');
                }
                ilevel++;
                out.print('<');
                out.print(elementName.getLocalPart());
                final int attributeCount = reader.getAttributeCount();
                for (int i = 0; i < attributeCount; i++) {
                    out.print(' ');
                    out.print(reader.getAttributeName(i));
                    out.print("=\"");
                    out.print(reader.getAttributeValue(i));
                    out.print('\"');
                }
            } else if (event.isValue()) {
                hasvalue = true;
                needsClosure = false;
                out.print('>');
                event = printer(elementName).print(reader, out);
            }

            if (event == EventType.END_ELEMENT) {
                ilevel--;
                if (needsClosure) {
                    out.print("/>");
                    needsClosure = false;
                    continue;
                }
                needsClosure = false;
                if (!hasvalue) {
                    out.print('\n');
                    for (int i = 0; i < ilevel; i++) {
                        out.print(' ');
                    }
                }
                out.print("</" + reader.getElementName().getLocalPart() + ">");
                hasvalue = false;
            }
        }
        out.print('\n');
        out.flush();
    }

    private static PrettyValuePrinter printer(QName name) {
        if (GEOMETRY_WKB.equals(name)) {
            return new WKBGeomPrinter();
        }
        if (OBJECT_ID.equals(name)) {
            return new ObjectIdPrinter();
        }
        return new PrettyValuePrinter();
    }

    private static class PrettyValuePrinter {
        public EventType print(BxmlStreamReader reader, PrintStream out) throws IOException {
            String stringValue = reader.getStringValue();
            out.print(stringValue);
            return reader.getEventType();
        }
    }

    private static class ObjectIdPrinter extends PrettyValuePrinter {
        @Override
        public EventType print(BxmlStreamReader reader, PrintStream out) throws IOException {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();

            EventType event = reader.getEventType();
            while (event.equals(EventType.VALUE_BYTE)) {
                final int chunkSize = reader.getValueCount();
                int byteValue;
                for (int i = 0; i < chunkSize; i++) {
                    byteValue = reader.getByteValue();
                    buf.write(byteValue);
                }
                event = reader.next();
            }
            byte[] rawId = buf.toByteArray();
            String strId = ObjectId.toString(rawId);
            out.print(strId);
            return reader.getEventType();
        }
    }

    private static class WKBGeomPrinter extends PrettyValuePrinter {
        @Override
        public EventType print(BxmlStreamReader reader, PrintStream out) throws IOException {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();

            EventType event = reader.getEventType();
            while (event.equals(EventType.VALUE_BYTE)) {
                final int chunkSize = reader.getValueCount();
                int byteValue;
                for (int i = 0; i < chunkSize; i++) {
                    byteValue = reader.getByteValue();
                    buf.write(byteValue);
                }
                event = reader.next();
            }

            Geometry read;
            try {
                read = new WKBReader().read(buf.toByteArray());
                OutputStreamWriter w = new OutputStreamWriter(out);
                new WKTWriter().write(read, w);
                w.flush();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return reader.getEventType();
        }
    }
}
