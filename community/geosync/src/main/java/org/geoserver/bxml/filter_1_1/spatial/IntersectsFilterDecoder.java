package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.Intersects;

import org.opengis.filter.Filter;

public class IntersectsFilterDecoder extends BinarySpatialOperationDecoder {

    public IntersectsFilterDecoder() {
        super(Intersects, new ContainsFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff.intersects(ff.property(propertyName), ff.literal(geometry));
    }

}
