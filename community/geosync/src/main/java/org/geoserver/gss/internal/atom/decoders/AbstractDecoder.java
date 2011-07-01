package org.geoserver.gss.internal.atom.decoders;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.AbstractEncoder;
import org.geotools.feature.type.DateUtil;
import org.geotools.util.logging.Logging;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public abstract class AbstractDecoder<T> {

    private static final Logger LOGGER = Logging.getLogger(AbstractEncoder.class);

    protected void decodeElement(final BxmlStreamReader r) throws IOException {
    }

    protected void decodeAttributtes(final BxmlStreamReader r, Map<QName, String> attributes)
            throws IOException {
    }

    protected abstract T buildResult();

    protected void setStringValue(String value) throws IOException {
    }

    public T decode(BxmlStreamReader r) throws IOException {
        EventType event;
        while ((event = r.next()) != EventType.END_DOCUMENT) {
            if (EventType.START_DOCUMENT == event) {
            }
            if (EventType.START_ELEMENT != event) {
                continue;
            }
            if (r.getAttributeCount() > 0) {
                decodeAttributtes(r, getAttributes(r));
            }
            decodeElement(r);
        }
        return buildResult();
    }

    public T decode(BxmlStreamReader r, QName name) throws IOException {
        r.require(EventType.START_ELEMENT, name.getNamespaceURI(), name.getLocalPart());

        EventType event;
        if (r.getAttributeCount() > 0) {
            decodeAttributtes(r, getAttributes(r));
        }
        while (true) {
            event = r.next();

            if (EventType.VALUE_STRING == event) {
                StringBuilder sb = new StringBuilder();
                sb.append(r.getStringValue());
                event = r.next();
                while (event.isValue()) {
                    sb.append(r.getStringValue());
                    event = r.next();
                }
                setStringValue(sb.toString());
            }

            if (event == EventType.END_ELEMENT && name.equals(r.getElementName())) {
                r.require(EventType.END_ELEMENT, name.getNamespaceURI(), name.getLocalPart());
                return buildResult();
            }

            if (EventType.START_ELEMENT != event) {
                continue;
            }
            decodeElement(r);
        }

    }

    /**
     * @param r must be possitioned at the START_ELEMENT event of the element who's value is to be
     *        read. When this method returns the reader is guaranteed to be possitioned at the
     *        END_ELEMENT event of the same element.
     * @return
     * @throws IOException
     */
    protected String readStringValue(final BxmlStreamReader r, final QName name) throws IOException {
        r.require(EventType.START_ELEMENT, name.getNamespaceURI(), name.getLocalPart());
        StringBuilder sb = new StringBuilder();
        while (r.next().isValue()) {
            sb.append(r.getStringValue());
        }
        r.require(EventType.END_ELEMENT, name.getNamespaceURI(), name.getLocalPart());
        return sb.toString();
    }

    protected Long readLongValue(final BxmlStreamReader r, final QName name) throws IOException {
        String stringValue = readStringValue(r, name);
        if (stringValue != null && stringValue.equals("")) {
            LOGGER.warning(name.getLocalPart() + " value is null.");
        } else {
            Long value = 0l;

            try {
                value = new Long(stringValue);
                return value;
            } catch (NumberFormatException e) {
                LOGGER.warning(name.getLocalPart() + " value must be numeric.");
            }
        }
        return null;
    }

    protected Date readDateValue(BxmlStreamReader r, final QName name) throws IOException {
        r.require(EventType.START_ELEMENT, name.getNamespaceURI(), name.getLocalPart());
        String dateString = readStringValue(r, name);
        Date date = null;
        try {
            date = DateUtil.deserializeDateTime(dateString);
        } catch (IllegalArgumentException e) {
            LOGGER.warning("incorrect value for " + name + ".");
        }
        r.require(EventType.END_ELEMENT, name.getNamespaceURI(), name.getLocalPart());
        return date;
    }

    protected Map<QName, String> getAttributes(BxmlStreamReader r) {
        final int attributeCount = r.getAttributeCount();
        Map<QName, String> map = new HashMap<QName, String>();
        for (int i = 0; i < attributeCount; i++) {
            QName attributeName = r.getAttributeName(i);
            String attributeValue = r.getAttributeValue(i);
            map.put(attributeName, attributeValue);
        }
        return map;
    }

    protected Long parseLongValue(String stringValue, String name) {
        if (stringValue == null || stringValue.equals("")) {
            LOGGER.warning(name + " value mustn't be null.");
        } else {
            Long value = 0l;

            try {
                value = new Long(stringValue);
                return value;
            } catch (NumberFormatException e) {
                LOGGER.warning(name + " value must be numeric.");
                return null;
            }
        }
        return null;
    }

}
