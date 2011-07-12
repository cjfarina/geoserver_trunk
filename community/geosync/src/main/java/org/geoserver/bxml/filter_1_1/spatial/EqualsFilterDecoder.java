package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.Equals;

import org.opengis.filter.Filter;

public class EqualsFilterDecoder extends BinarySpatialOperationDecoder {

    public EqualsFilterDecoder() {
        super(Equals, new DisjointFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff.equals(ff.property(propertyName), ff.literal(geometry));
    }
}
