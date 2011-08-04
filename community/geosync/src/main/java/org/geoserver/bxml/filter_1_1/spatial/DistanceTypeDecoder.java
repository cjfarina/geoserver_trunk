package org.geoserver.bxml.filter_1_1.spatial;

import javax.xml.namespace.QName;

import org.geoserver.bxml.filter_1_1.AbstractTypeDecoder;
import org.geotools.filter.v1_1.OGC;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class DistanceTypeDecoder extends AbstractTypeDecoder<Distance> {

    public static final QName Distance = new QName(OGC.NAMESPACE, "Distance");

    public DistanceTypeDecoder() {
        super(Distance);
    }

    @Override
    protected Distance decodeInternal(BxmlStreamReader r, QName name) throws Exception {
        StringBuilder sb = new StringBuilder();
        String units = r.getAttributeValue(null, "units");

        EventType event;

        while ((event = r.next()).isValue()) {
            String chunk = r.getStringValue();
            sb.append(chunk);
        }

        String value = sb.length() == 0 ? null : sb.toString();

        Distance distance = new Distance(Double.parseDouble(value), units);
        return distance;
    }

}
