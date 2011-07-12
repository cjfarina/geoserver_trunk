package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.Crosses;

import org.opengis.filter.Filter;

public class CrossesFilterDecoder extends BinarySpatialOperationDecoder {

    public CrossesFilterDecoder() {
        super(Crosses, new IntersectsFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff.crosses(ff.property(propertyName), ff.literal(geometry));
    }

}
