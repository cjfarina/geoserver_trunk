package org.geoserver.bxml.base;

import java.util.Date;

import javax.xml.namespace.QName;

import org.geotools.feature.type.DateUtil;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

import com.google.common.base.Throwables;

public class DateDecoder extends SimpleDecoder<Date> {

    public DateDecoder(QName elemName) {
        super(elemName);
    }

    @Override
    public Date decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, null, elemName.getLocalPart());
        r.next();

        // if empty element
        if (isEndElement(r)) {
            return null;
        }
        Date value = readDateValue(r);

        r.require(EventType.END_ELEMENT, null, elemName.getLocalPart());
        return value;
    }

    public static Date readDateValue(BxmlStreamReader r) throws Exception {
        StringValueDecoder valueDecoder = new StringValueDecoder();
        Date date = null;
        if (r.getEventType().isValue()) {
            String dateString = valueDecoder.decode(r);
            try {
                date = DateUtil.deserializeDateTime(dateString);
            } catch (IllegalArgumentException e) {
                Throwables.propagate(e);
            }
        }
        return date;
    }

}
