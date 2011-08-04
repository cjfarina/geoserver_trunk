package org.geoserver.bxml.gml_3_1;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.Geometry;

public class GeometryMemberDecoder extends AbstractGeometryDecoder<Geometry> {

    private final Decoder<Geometry> memberDecoder;

    public GeometryMemberDecoder(QName name, Decoder<Geometry> memberDecoder) {
        super(name);
        this.memberDecoder = memberDecoder;
    }

    @Override
    protected Geometry decodeInternal(BxmlStreamReader r, QName name) throws Exception {
        r.nextTag();
        Geometry geometry = memberDecoder.decode(r);
        r.nextTag();
        return geometry;
    }

}
