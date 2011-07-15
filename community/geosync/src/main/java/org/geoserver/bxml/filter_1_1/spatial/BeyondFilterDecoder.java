package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.Beyond;

import org.opengis.filter.Filter;

public class BeyondFilterDecoder extends DistanceBufferFilterDecoder {

    public BeyondFilterDecoder() {
        super(Beyond, new BBOXFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff
                .beyond(expression, ff.literal(geometry), distance.getValue(), distance.getUnits());
    }

}
