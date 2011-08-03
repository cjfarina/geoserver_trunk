package org.geoserver.bxml.gml_3_1;

import static org.geotools.gml3.GML.LineString;

import javax.xml.namespace.QName;

import org.geoserver.bxml.ChoiceDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class LineStringDecoder extends AbstractGeometryDecoder<Geometry> {

    private ChoiceDecoder<CoordinateSequence> choice;
    
    public LineStringDecoder() {
        super(LineString);
        choice = new ChoiceDecoder<CoordinateSequence>();
        choice.addOption(new PosDecoder());
        choice.addOption(new PosListDecoder());
        choice.addOption(new CoordinatesDecoder());
        choice.addOption(new CoordDecoder());
    }
    
    @Override
    public Geometry decodeInternal(final BxmlStreamReader r, final QName name) throws Exception {
        r.nextTag();

        CoordinateSequence coordinates = choice.decode(r);

        LineString lineString = new GeometryFactory().createLineString(coordinates);
        lineString.setUserData(getCrs());
        return lineString;
    }

}
