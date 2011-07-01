package org.geoserver.gss.impl.query;

import java.util.Date;

import org.geoserver.gss.internal.atom.AbstractEncoder;
import org.geoserver.ows.KvpParser;
import org.geotools.feature.type.DateUtil;

/**
 * Parses a String into a {@code java.util.Date} using {@link DateUtil#parseDateTime(String)} as the
 * closest to <a href="http://tools.ietf.org/html/rfc3339">rfc3339</a> parser that I know of.
 * 
 * @author groldan
 * @see AbstractEncoder
 */
public class TimeStampKvpParser extends KvpParser {

    public TimeStampKvpParser(String key) {
        super(key, Date.class);
    }

    @Override
    public Object parse(String value) throws Exception {
        long dateTime = DateUtil.parseDateTime(value);
        return new Date(dateTime);
    }

}