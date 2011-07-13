package org.geoserver.bxml.atom;

import static org.geoserver.gss.internal.atom.GeoRSS.where;

import org.geoserver.bxml.AbstractDecoder;
import org.geoserver.bxml.gml_3_1.GMLChainDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class WhereDecoder extends AbstractDecoder<Object> {

    private Object value;

    public WhereDecoder() {
        super(where);
    }

    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        value = new GMLChainDecoder().decode(r);
    }

    @Override
    protected Object buildResult() {
        return value;
    }

}
