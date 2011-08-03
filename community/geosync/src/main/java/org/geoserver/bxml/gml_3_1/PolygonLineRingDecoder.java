package org.geoserver.bxml.gml_3_1;

import javax.xml.namespace.QName;

import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

public class PolygonLineRingDecoder extends AbstractGeometryDecoder<Geometry> {

    public PolygonLineRingDecoder(CoordinateReferenceSystem coordinateReferenceSystem,
            int dimension, QName elemName) {
        super(coordinateReferenceSystem, dimension, elemName);
    }

    @Override
    public Geometry decodeInternal(BxmlStreamReader r, QName name) throws Exception {
        r.nextTag();
        return new LinearRingDecoder().decode(r);
    }

}
