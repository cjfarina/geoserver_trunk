package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.PropertyIsGreaterThan;

import org.opengis.filter.Filter;

public class PropertyIsGreaterThanFilterDecoder extends BinaryComparisonOpTypeFilterDecoder {

    public PropertyIsGreaterThanFilterDecoder() {
        super(PropertyIsGreaterThan, new PropertyIsLessThanOrEqualToFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff.greater(expresions.get(0), expresions.get(1));
    }

}
