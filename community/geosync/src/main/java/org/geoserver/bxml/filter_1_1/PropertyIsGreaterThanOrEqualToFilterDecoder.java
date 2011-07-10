package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.PropertyIsGreaterThanOrEqualTo;

import org.opengis.filter.Filter;

public class PropertyIsGreaterThanOrEqualToFilterDecoder extends BinaryComparisonOpTypeFilterDecoder {

    public PropertyIsGreaterThanOrEqualToFilterDecoder() {
        super(PropertyIsGreaterThanOrEqualTo, new PropertyIsLikeFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff.greaterOrEqual(expresions.get(0), expresions.get(1));
    }

}
