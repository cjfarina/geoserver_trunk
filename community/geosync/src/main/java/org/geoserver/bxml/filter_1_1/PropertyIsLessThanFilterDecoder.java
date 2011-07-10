package org.geoserver.bxml.filter_1_1;

import static org.geotools.filter.v1_1.OGC.PropertyIsLessThan;

import org.opengis.filter.Filter;

public class PropertyIsLessThanFilterDecoder extends BinaryComparisonOpTypeFilterDecoder {

    public PropertyIsLessThanFilterDecoder() {
        super(PropertyIsLessThan, new PropertyIsGreaterThanFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff.less(expresions.get(0), expresions.get(1));
    }

}
