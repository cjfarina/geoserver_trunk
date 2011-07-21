package org.geoserver.bxml.gml_3_1;

import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.Geometry;

public class LineStringDecoder implements Decoder<Geometry> {

    @Override
    public Geometry decode(BxmlStreamReader r) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canHandle(QName name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<QName> getTargets() {
        // TODO Auto-generated method stub
        return null;
    }

}
