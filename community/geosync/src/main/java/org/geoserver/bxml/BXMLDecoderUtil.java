package org.geoserver.bxml;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.geoserver.bxml.atom.AbstractAtomEncoder;
import org.geoserver.bxml.base.StringDecoder;
import org.geoserver.bxml.gml_3_1.GeometryDecoder;
import org.geotools.util.logging.Logging;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class BXMLDecoderUtil {

    protected static final Logger LOGGER = Logging.getLogger(AbstractAtomEncoder.class);

    public static Long parseLongValue(String stringValue, String name) {
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

    public static void goToEnd(final BxmlStreamReader r, final QName name) throws IOException {
        while (!EventType.END_ELEMENT.equals(r.getEventType()) || !name.equals(r.getElementName())) {
            r.next();
        }
    }
    
    public static Object readValue(BxmlStreamReader r) throws IOException, Exception {
        EventType event = r.next();
        Object value = null;
        if (EventType.VALUE_STRING == event) {
            value = StringDecoder.readStringValue(r);
            event = r.getEventType();
        } else if (EventType.VALUE_BOOL == event) {
            value = Boolean.valueOf(r.getBooleanValue());
            event = r.getEventType();
        } else if (EventType.VALUE_BYTE== event) {
            value = r.getByteValue();
            event = r.getEventType();
        } else if (EventType.VALUE_DOUBLE == event) {
            value = r.getDoubleValue();
            event = r.getEventType();
        } else if (EventType.VALUE_FLOAT == event) {
            value = r.getFloatValue();
            event = r.getEventType();
        } else if (EventType.VALUE_INT == event) {
            value = r.getIntValue();
            event = r.getEventType();
        } else if (EventType.VALUE_LONG == event) {
            value = r.getLongValue();
            event = r.getEventType();
        } 
        
        if (EventType.START_ELEMENT == event) {
            value = new GeometryDecoder().decode(r);
            r.nextTag();
        }
        return value;
    }

}
