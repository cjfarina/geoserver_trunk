package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.DWithin;

import org.opengis.filter.Filter;

public class DWithinFilterDecoder extends DistanceBufferFilterDecoder {

    public DWithinFilterDecoder() {
        super(DWithin, new BeyondFilterDecoder());
    }

    @Override
    protected Filter buildResult() {
        return ff.dwithin(expression, ff.literal(geometry), distance.getValue(),
                distance.getUnits());
    }

}
