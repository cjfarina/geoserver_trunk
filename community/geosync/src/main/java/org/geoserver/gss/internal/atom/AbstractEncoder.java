package org.geoserver.gss.internal.atom;

import static org.geoserver.gss.internal.atom.Atom.email;
import static org.geoserver.gss.internal.atom.Atom.name;
import static org.geoserver.gss.internal.atom.Atom.uri;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.geoserver.platform.GeoServerExtensions;
import org.geotools.feature.type.DateUtil;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.gvsig.bxml.geoserver.DefaultEncoderConfig;
import org.gvsig.bxml.geoserver.EncoderConfig;
import org.gvsig.bxml.geoserver.Gml3Encoder;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.util.Assert;

import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractEncoder {

    private static final Logger LOGGER = Logging.getLogger(AbstractEncoder.class);

    private Gml3Encoder gmlEncoder;

    protected Gml3Encoder getGmlEncoder() {
        if (gmlEncoder == null) {
            EncoderConfig config = GeoServerExtensions.bean(DefaultEncoderConfig.class);
            Assert.notNull(config);
            gmlEncoder = new Gml3Encoder(config);
        }
        return gmlEncoder;
    }

    protected void startElement(BxmlStreamWriter w, QName element) {
        try {
            w.writeStartElement(element.getNamespaceURI(), element.getLocalPart());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void endElement(BxmlStreamWriter w) {
        try {
            w.writeEndElement();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void element(BxmlStreamWriter w, QName element, boolean encodeIfNull, Date date) {
        element(w, element, encodeIfNull, date, false);
    }

    /**
     * Encodes a {@code java.util.Date} using {@link DateUtil#serializeDateTime(long, boolean)} as
     * the closest to <a href="http://tools.ietf.org/html/rfc3339">rfc3339</a> that I know of.
     */
    protected final void element(BxmlStreamWriter w, QName element, boolean encodeIfNull,
            Date date, boolean stringTable) {
        if (date != null) {
            String value = DateUtil.serializeDateTime(date.getTime(), true);
            element(w, element, encodeIfNull, value, stringTable);
        }
    }

    protected final void element(BxmlStreamWriter w, QName element, boolean encodeIfNull,
            String value) {
        element(w, element, encodeIfNull, value, false);
    }

    protected void element(BxmlStreamWriter w, QName element, boolean encodeIfNull, String value,
            boolean stringTable) {
        element(w, element, encodeIfNull, value, stringTable, false, (String[]) null);
    }

    protected final void element(BxmlStreamWriter w, QName element, boolean encodeIfNull,
            String value, String... attributes) {
        element(w, element, encodeIfNull, value, false, false, attributes);
    }

    /**
     * @param w
     * @param element
     * @param encodeIfNull
     * @param value
     * @param stringTable
     *            if {@code true} try using a string table reference to encode the value (use only
     *            for highly repetitive values)
     * @param stringTableAtts
     *            if {@code true} try using a string table reference to encode the value (use only
     *            for highly repetitive attributes)
     * @param attributes
     */
    protected void element(BxmlStreamWriter w, QName element, boolean encodeIfNull, String value,
            boolean stringTable, boolean stringTableAtts, String... attributes) {
        if (value == null && !encodeIfNull) {
            return;
        }
        try {
            w.writeStartElement(element.getNamespaceURI(), element.getLocalPart());
            attributes(w, stringTableAtts, attributes);
            if (value != null) {
                if (stringTable && w.supportsStringTableValues()) {
                    long stringTableReference = w.getStringTableReference(value);
                    w.writeStringTableValue(stringTableReference);
                } else {
                    w.writeValue(value);
                }
            }
            w.writeEndElement();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void attributes(BxmlStreamWriter w, String... attributes) {
        attributes(w, false, attributes);
    }

    protected void attributes(BxmlStreamWriter w, boolean stringTable, String... attributes) {

        if (attributes != null && attributes.length > 0) {
            Assert.isTrue(attributes.length % 2 == 0,
                    "Didn't get an even set of attributes key/value pairs");
            try {
                long[] valueRefs = null;
                if (w.supportsStringTableValues()) {
                    valueRefs = new long[attributes.length];
                    for (int i = 0; i < attributes.length; i += 2) {
                        long valueRef = w.getStringTableReference(attributes[i + 1]);
                        valueRefs[i] = valueRef;
                    }
                }
                for (int i = 0; i < attributes.length; i += 2) {
                    w.writeStartAttribute("", attributes[i]);
                    if (w.supportsStringTableValues()) {
                        w.writeStringTableValue(valueRefs[i]);
                    } else {
                        w.writeValue(attributes[i + 1]);
                    }
                }
                w.writeEndAttributes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void person(BxmlStreamWriter w, PersonImpl person, QName personElem) {
        try {
            w.writeStartElement(personElem.getNamespaceURI(), personElem.getLocalPart());
            {
                element(w, name, false, person.getName(), true);
                element(w, uri, false, person.getUri(), true);
                element(w, email, false, person.getEmail(), true);
            }
            w.writeEndElement();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected final CoordinateReferenceSystem guessCRS(final Geometry geometry) {

        final Object userData = geometry.getUserData();
        CoordinateReferenceSystem crs = null;
        if (userData instanceof CoordinateReferenceSystem) {
            crs = (CoordinateReferenceSystem) userData;
        } else if (userData instanceof String) {
            String mayBeACrsId = (String) userData;
            try {
                crs = CRS.decode(mayBeACrsId);
            } catch (Exception e) {
                // looks like it wasn't
                crs = null;
            }
        }
        if (crs == null && geometry.getSRID() > 0) {
            // may it have been set as a srid property?
            try {
                CRS.decode("urn:x-ogc:def:crs:EPSG:" + geometry.getSRID());
            } catch (Exception e) {
                // bad luck
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Geometry has srid property = " + geometry.getSRID()
                            + " but it coudln't be parsed to a CoordinateReferenceSystem", e);
                }
            }
        }
        if (crs == null) {
            // fallback to default value
            LOGGER.fine("Geometry contains no CRS information, defaulting to urn:x-ogc:def:crs:EPSG:4326");
            try {
                CRS.decode("urn:x-ogc:def:crs:EPSG:4326");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return crs;
    }

}
