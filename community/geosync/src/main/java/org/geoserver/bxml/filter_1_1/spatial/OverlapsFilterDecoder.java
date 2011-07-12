package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.Overlaps;

import org.opengis.filter.Filter;

public class OverlapsFilterDecoder extends BinarySpatialOperationDecoder {

    public OverlapsFilterDecoder() {
        super(Overlaps, new CrossesFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff.overlaps(ff.property(propertyName), ff.literal(geometry));
    }

}
