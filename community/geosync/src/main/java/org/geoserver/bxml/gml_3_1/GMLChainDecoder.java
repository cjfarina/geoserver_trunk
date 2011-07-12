package org.geoserver.bxml.gml_3_1;

import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.Geometry;

public class GMLChainDecoder {

    private PolygonDecoder link;
    
    public Geometry decode(final BxmlStreamReader r) throws Exception {
        return link.decode(r);
    }
}
