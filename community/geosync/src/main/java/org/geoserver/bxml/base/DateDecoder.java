package org.geoserver.bxml.base;

import java.io.IOException;
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
        r.require(EventType.START_ELEMENT, elemName.getNamespaceURI(), elemName.getLocalPart());
        r.next();
        Date value = readDateValue(r);

        r.require(EventType.END_ELEMENT, elemName.getNamespaceURI(), elemName.getLocalPart());
        return value;
    }

    public static Date readDateValue(BxmlStreamReader r) throws IOException {
        String dateString = StringDecoder.readStringValue(r);
        Date date = null;
        try {
            date = DateUtil.deserializeDateTime(dateString);
        } catch (IllegalArgumentException e) {
            Throwables.propagate(e);
        }
        return date;
    }

}
