package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.Contains;

import org.opengis.filter.Filter;

public class ContainsFilterDecoder extends BinarySpatialOperationDecoder {

    public ContainsFilterDecoder() {
        super(Contains, new DWithinFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff.contains(ff.property(propertyName), ff.literal(geometry));
    }

}
