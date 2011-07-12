package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.Disjoint;

import org.opengis.filter.Filter;

public class DisjointFilterDecoder extends BinarySpatialOperationDecoder {

    public DisjointFilterDecoder() {
        super(Disjoint, new TouchesFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff.disjoint(ff.property(propertyName), ff.literal(geometry));
    }

}
