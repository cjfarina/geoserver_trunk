package org.geoserver.bxml.gml_3_1;

import static org.geotools.gml3.GML.*;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.CoordinateSequence;

public class CoordinatesDecoder implements Decoder<CoordinateSequence> {

    @Override
    public CoordinateSequence decode(BxmlStreamReader r) throws Exception {
        throw new UnsupportedOperationException("gml:coordinates parsing not yet implemented");
    }

    @Override
    public boolean canHandle(QName name) {
        return coordinates.equals(name);
    }

    @Override
    public Set<QName> getTargets() {
        return Collections.singleton(coordinates);
    }

}
