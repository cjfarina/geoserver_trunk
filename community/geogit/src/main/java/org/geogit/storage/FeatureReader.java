package org.geogit.storage;

import static org.geogit.storage.BLOBS.BIGDECIMAL;
import static org.geogit.storage.BLOBS.BIGINT;
import static org.geogit.storage.BLOBS.BOOLEAN;
import static org.geogit.storage.BLOBS.BOOLEAN_ARRAY;
import static org.geogit.storage.BLOBS.BYTE;
import static org.geogit.storage.BLOBS.BYTE_ARRAY;
import static org.geogit.storage.BLOBS.CHAR_ARRAY;
import static org.geogit.storage.BLOBS.CONVERTED_TO_STRING;
import static org.geogit.storage.BLOBS.DOUBLE;
import static org.geogit.storage.BLOBS.DOUBLE_ARRAY;
import static org.geogit.storage.BLOBS.FEATURE;
import static org.geogit.storage.BLOBS.FLOAT;
import static org.geogit.storage.BLOBS.FLOAT_ARRAY;
import static org.geogit.storage.BLOBS.GEOMETRY_WKB;
import static org.geogit.storage.BLOBS.INT;
import static org.geogit.storage.BLOBS.INT_ARRAY;
import static org.geogit.storage.BLOBS.LONG;
import static org.geogit.storage.BLOBS.LONG_ARRAY;
import static org.geogit.storage.BLOBS.NULL;
import static org.geogit.storage.BLOBS.STRING;
import static org.gvsig.bxml.stream.EventType.END_DOCUMENT;
import static org.gvsig.bxml.stream.EventType.END_ELEMENT;
import static org.gvsig.bxml.stream.EventType.START_ELEMENT;
import static org.gvsig.bxml.stream.EventType.VALUE_BOOL;
import static org.gvsig.bxml.stream.EventType.VALUE_BYTE;
import static org.gvsig.bxml.stream.EventType.VALUE_DOUBLE;
import static org.gvsig.bxml.stream.EventType.VALUE_FLOAT;
import static org.gvsig.bxml.stream.EventType.VALUE_INT;
import static org.gvsig.bxml.stream.EventType.VALUE_LONG;
import static org.gvsig.bxml.stream.EventType.VALUE_STRING;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.namespace.QName;

import org.geogit.api.ObjectId;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.referencing.CRS;
import org.geotools.util.Converters;
import org.gvsig.bxml.stream.BxmlInputFactory;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.google.common.base.Throwables;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.InStream;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

public class FeatureReader implements ObjectReader<Feature> {

    private final FeatureType featureType;

    private final String featureId;

    public FeatureReader(final FeatureType featureType, final String featureId) {
        this.featureType = featureType;
        this.featureId = featureId;
    }

    /**
     * @see org.geogit.storage.ObjectReader#read(org.geogit.api.ObjectId, java.io.InputStream)
     */
    public Feature read(final ObjectId id, final InputStream rawData) throws IOException {
        final BxmlInputFactory inputFactory = BLOBS.cachedInputFactory;
        final BxmlStreamReader reader = inputFactory.createScanner(rawData);

        EventType tag = reader.nextTag();
        reader.require(START_ELEMENT, FEATURE.getNamespaceURI(), FEATURE.getLocalPart());

        SimpleFeatureBuilder builder = new SimpleFeatureBuilder((SimpleFeatureType) featureType);
        int index = 0;
        while (!END_DOCUMENT.equals(tag)) {
            tag = reader.nextTag();
            if (END_ELEMENT.equals(tag) && FEATURE.equals(reader.getElementName())) {
                break;
            }
            Object attValue = readValue(reader);
            builder.set(index, attValue);
            index++;
        }

        SimpleFeature feature = builder.buildFeature(featureId);
        return feature;
    }

    private Object readValue(final BxmlStreamReader reader) throws IOException {
        reader.require(START_ELEMENT, null, null);
        final QName valueElemName = reader.getElementName();

        Object value = null;
        if (NULL.equals(valueElemName)) {
            reader.nextTag();
            value = null;
            reader.require(END_ELEMENT, valueElemName.getNamespaceURI(),
                    valueElemName.getLocalPart());
        } else if (STRING.equals(valueElemName)) {
            reader.next();
            reader.require(VALUE_STRING, null, null);
            value = reader.getStringValue();
            reader.nextTag();
            reader.require(END_ELEMENT, valueElemName.getNamespaceURI(),
                    valueElemName.getLocalPart());
        } else if (BOOLEAN.equals(valueElemName)) {
            reader.next();
            reader.require(VALUE_BOOL, null, null);
            value = Boolean.valueOf(reader.getBooleanValue());
            reader.nextTag();
            reader.require(END_ELEMENT, valueElemName.getNamespaceURI(),
                    valueElemName.getLocalPart());
        } else if (BYTE.equals(valueElemName)) {
            reader.next();
            reader.require(VALUE_BYTE, null, null);
            value = Byte.valueOf((byte) reader.getByteValue());
            reader.nextTag();
            reader.require(END_ELEMENT, valueElemName.getNamespaceURI(),
                    valueElemName.getLocalPart());
        } else if (DOUBLE.equals(valueElemName)) {
            reader.next();
            reader.require(VALUE_DOUBLE, null, null);
            value = Double.valueOf(reader.getDoubleValue());
            reader.nextTag();
            reader.require(END_ELEMENT, valueElemName.getNamespaceURI(),
                    valueElemName.getLocalPart());
        } else if (BIGDECIMAL.equals(valueElemName)) {
            reader.next();
            reader.require(VALUE_STRING, null, null);
            value = new BigDecimal(reader.getStringValue());
            reader.nextTag();
            reader.require(END_ELEMENT, valueElemName.getNamespaceURI(),
                    valueElemName.getLocalPart());
        } else if (FLOAT.equals(valueElemName)) {
            reader.next();
            reader.require(VALUE_FLOAT, null, null);
            value = Float.valueOf(reader.getFloatValue());
            reader.nextTag();
            reader.require(END_ELEMENT, valueElemName.getNamespaceURI(),
                    valueElemName.getLocalPart());
        } else if (INT.equals(valueElemName)) {
            reader.next();
            reader.require(VALUE_INT, null, null);
            value = Integer.valueOf(reader.getIntValue());
            reader.nextTag();
            reader.require(END_ELEMENT, valueElemName.getNamespaceURI(),
                    valueElemName.getLocalPart());
        } else if (BIGINT.equals(valueElemName)) {
            reader.next();
            reader.require(VALUE_BYTE, null, null);
            int size = reader.getValueCount();
            byte[] buf = new byte[size];
            reader.getValue(buf, 0, size);
            value = new BigInteger(buf);
            reader.nextTag();
            reader.require(END_ELEMENT, valueElemName.getNamespaceURI(),
                    valueElemName.getLocalPart());
        } else if (LONG.equals(valueElemName)) {
            reader.next();
            reader.require(VALUE_LONG, null, null);
            value = Long.valueOf(reader.getLongValue());
            reader.nextTag();
            reader.require(END_ELEMENT, valueElemName.getNamespaceURI(),
                    valueElemName.getLocalPart());
        } else if (BOOLEAN_ARRAY.equals(valueElemName)) {
            throw new UnsupportedOperationException("not implemented yet");
            // writer.writeStartElement(BOOLEAN_ARRAY);
            // boolean[] array = (boolean[]) value;
            // writer.writeValue(array, 0, array.length);
        } else if (BYTE_ARRAY.equals(valueElemName)) {
            throw new UnsupportedOperationException("not implemented yet");
            // writer.writeStartElement(BYTE_ARRAY);
            // byte[] array = (byte[]) value;
            // writer.writeValue(array, 0, array.length);
        } else if (CHAR_ARRAY.equals(valueElemName)) {
            throw new UnsupportedOperationException("not implemented yet");
            // writer.writeStartElement(CHAR_ARRAY);
            // char[] array = (char[]) value;
            // writer.writeValue(array, 0, array.length);
        } else if (DOUBLE_ARRAY.equals(valueElemName)) {
            throw new UnsupportedOperationException("not implemented yet");
            // writer.writeStartElement(DOUBLE_ARRAY);
            // double[] array = (double[]) value;
            // writer.writeValue(array, 0, array.length);
        } else if (FLOAT_ARRAY.equals(valueElemName)) {
            throw new UnsupportedOperationException("not implemented yet");
            // writer.writeStartElement(FLOAT_ARRAY);
            // float[] array = (float[]) value;
            // writer.writeValue(array, 0, array.length);
        } else if (INT_ARRAY.equals(valueElemName)) {
            throw new UnsupportedOperationException("not implemented yet");
            // writer.writeStartElement(INT_ARRAY);
            // int[] array = (int[]) value;
            // writer.writeValue(array, 0, array.length);
        } else if (LONG_ARRAY.equals(valueElemName)) {
            throw new UnsupportedOperationException("not implemented yet");
            // writer.writeStartElement(LONG_ARRAY);
            // long[] array = (long[]) value;
            // writer.writeValue(array, 0, array.length);
        } else if (GEOMETRY_WKB.equals(valueElemName)) {
            // String srs = reader.getAttributeValue(XMLConstants.NULL_NS_URI, "crs");
            String srs = reader.getAttributeValue(0);
            EventType event = reader.next();
            reader.require(EventType.VALUE_BYTE, null, null);

            WKBReader geomReader = new WKBReader();
            Geometry geometry;
            try {
                geometry = geomReader.read(new InStream() {

                    /**
                     * @see com.vividsolutions.jts.io.InStream#read(byte[])
                     */
                    public void read(byte[] buf) throws IOException {
                        final int requestedLength = buf.length;
                        int offset = 0;
                        int missing = requestedLength;
                        while (missing > 0) {
                            int valueCount = reader.getValueCount();
                            int valueReadCount = reader.getValueReadCount();
                            int remaining = valueCount - valueReadCount;
                            if (remaining >= missing) {
                                reader.getValue(buf, offset, missing);
                                return;
                            }
                            if (remaining > 0) {
                                reader.getValue(buf, offset, remaining);
                                offset += remaining;
                                missing -= remaining;
                            }
                            if (missing > 0) {
                                reader.next();
                            }
                        }
                    }
                });
            } catch (ParseException e) {
                throw (IOException) new IOException(e.getMessage()).initCause(e);
            }

            if (srs != null) {
                CoordinateReferenceSystem crs;
                try {
                    crs = CRS.decode(srs);
                    geometry.setUserData(crs);
                } catch (Exception e) {
                    Throwables.propagate(e);
                }
            }
            value = geometry;
            reader.nextTag();
            reader.require(END_ELEMENT, valueElemName.getNamespaceURI(),
                    valueElemName.getLocalPart());
        } else if (CONVERTED_TO_STRING.equals(valueElemName)) {
            final String valueClassName = reader.getAttributeValue(0);// CONVERTED_FROM_ATT
            Class<?> valueClass;
            try {
                valueClass = Class.forName(valueClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            reader.next();
            reader.require(VALUE_STRING, null, null);
            String serializedValue = reader.getStringValue();
            Object deserializedValue = Converters.convert(serializedValue, valueClass);
            value = deserializedValue;
            reader.nextTag();
            reader.require(END_ELEMENT, valueElemName.getNamespaceURI(),
                    valueElemName.getLocalPart());
        } else {
            throw new IllegalStateException("Unknown value element type: " + valueElemName);
        }

        return value;
    }
}
