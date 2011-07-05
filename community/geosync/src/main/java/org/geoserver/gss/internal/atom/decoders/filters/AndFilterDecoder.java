package org.geoserver.gss.internal.atom.decoders.filters;

import org.geoserver.gss.internal.atom.decoders.AbstractDecoder;
import org.opengis.filter.Filter;

import static org.geotools.filter.v1_1.OGC.And;

import static org.geotools.filter.v1_1.OGC.Filter;

public class AndFilterDecoder extends FilterLinkDecoder {

    public AndFilterDecoder() {
        super(And);
    }
    @Override
    protected Filter buildResult() {
        // TODO Auto-generated method stub
        return null;
    }

}
