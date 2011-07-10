package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.PropertyIsNotEqualTo;

import org.opengis.filter.Filter;

public class PropertyIsNotEqualToFilterDecoder extends BinaryComparisonOpTypeFilterDecoder {

    public PropertyIsNotEqualToFilterDecoder() {
        super(PropertyIsNotEqualTo, new PropertyIsLessThanFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff.notEqual(expresions.get(0), expresions.get(1));
    }

}
