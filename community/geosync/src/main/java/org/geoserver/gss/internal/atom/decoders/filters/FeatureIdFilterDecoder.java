package org.geoserver.gss.internal.atom.decoders.filters;

import java.io.IOException;

import org.geoserver.gss.internal.atom.decoders.AbstractDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import static org.geotools.filter.v1_1.OGC.FeatureId;

public class FeatureIdFilterDecoder extends FilterLinkDecoder {

    
    public FeatureIdFilterDecoder() {
        super(FeatureId, new AndFilterDecoder());
    }
    
    @Override
    protected Filter buildResult() {
        return null;
    }

}
