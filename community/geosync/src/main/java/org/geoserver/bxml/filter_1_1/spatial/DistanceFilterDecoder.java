package org.geoserver.bxml.filter_1_1.spatial;

import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.geoserver.gss.internal.atom.Atom;
import org.geotools.filter.v1_1.OGC;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class DistanceFilterDecoder extends AbstractDecoder<Distance> {

    public static final QName Distance = new QName(OGC.NAMESPACE, "Distance");

    private Distance distance = new Distance();

    public DistanceFilterDecoder() {
        super(Distance);
    }

    protected void decodeAttributtes(final BxmlStreamReader r, Map<QName, String> attributes)
            throws Exception {
        distance.setUnits(attributes.get(new QName(Atom.NAMESPACE, "units")));
    }

    protected void setStringValue(String value) throws Exception {
        distance.setValue(new Double(value));
    }

    @Override
    protected Distance buildResult() {
        return distance;
    }

}
