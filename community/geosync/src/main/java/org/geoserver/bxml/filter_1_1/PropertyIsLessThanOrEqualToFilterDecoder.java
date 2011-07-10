package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.PropertyIsLessThanOrEqualTo;

import org.opengis.filter.Filter;

public class PropertyIsLessThanOrEqualToFilterDecoder extends BinaryComparisonOpTypeFilterDecoder {

    public PropertyIsLessThanOrEqualToFilterDecoder() {
        super(PropertyIsLessThanOrEqualTo, new PropertyIsGreaterThanOrEqualToFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff.lessOrEqual(expresions.get(0), expresions.get(1));
    }

}
