package org.geoserver.bxml.gml_3_1;

import org.gvsig.bxml.stream.BxmlStreamReader;

public class GMLChainDecoder {

    private PolygonDecoder link = new PolygonDecoder();
    
    public Object decode(final BxmlStreamReader r) throws Exception {
        return link.decode(r);
    }
}
