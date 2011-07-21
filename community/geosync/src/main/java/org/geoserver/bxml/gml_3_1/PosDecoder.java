package org.geoserver.bxml.gml_3_1;

import static org.geotools.gml3.GML.*;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;

public class PosDecoder implements Decoder<CoordinateSequence> {

    @Override
    public CoordinateSequence decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, pos.getNamespaceURI(), pos.getLocalPart());
        
        final String dimensionAtt = r.getAttributeValue(null, "dimension");
        
        double[] values = new DoubleListDecoder(pos).decode(r);
        
        r.require(EventType.END_ELEMENT, pos.getNamespaceURI(), pos.getLocalPart());
        
        int dimension = dimensionAtt == null? 2 : Integer.parseInt(dimensionAtt);
        return new PackedCoordinateSequence.Double(values, dimension);
    }

    @Override
    public boolean canHandle(QName name) {
        return pos.equals(name);
    }

    @Override
    public Set<QName> getTargets() {
        return Collections.singleton(pos);
    }

}
