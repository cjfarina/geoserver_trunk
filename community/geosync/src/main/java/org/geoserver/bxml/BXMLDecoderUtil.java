package org.geoserver.bxml;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.geoserver.bxml.atom.AbstractAtomEncoder;
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

}
