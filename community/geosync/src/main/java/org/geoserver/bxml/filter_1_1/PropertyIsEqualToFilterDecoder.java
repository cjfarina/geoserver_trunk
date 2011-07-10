package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.PropertyIsEqualTo;

import org.opengis.filter.Filter;

public class PropertyIsEqualToFilterDecoder extends BinaryComparisonOpTypeFilterDecoder {

    public PropertyIsEqualToFilterDecoder() {
        super(PropertyIsEqualTo, new PropertyIsNotEqualToFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff.equals(expresions.get(0), expresions.get(1));
    }

}
