package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.Touches;

import org.opengis.filter.Filter;

public class TouchesFilterDecoder extends BinarySpatialOperationDecoder {

    public TouchesFilterDecoder() {
        super(Touches, new WithinFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff.touches(ff.property(propertyName), ff.literal(geometry));
    }

}
