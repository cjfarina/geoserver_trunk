package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.Within;

import org.opengis.filter.Filter;

public class WithinFilterDecoder extends BinarySpatialOperationDecoder {

    public WithinFilterDecoder() {
        super(Within, new OverlapsFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff.within(ff.property(propertyName), ff.literal(geometry));
    }

}
