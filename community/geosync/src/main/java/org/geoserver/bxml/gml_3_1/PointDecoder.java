package org.geoserver.bxml.gml_3_1;

import javax.xml.namespace.QName;

import org.geoserver.bxml.BXMLDecoderUtil;
import org.geoserver.bxml.ChoiceDecoder;
import org.geotools.gml2.GML;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class PointDecoder extends AbstractGeometryDecoder<Geometry> {

    private ChoiceDecoder<CoordinateSequence> choice;

    public PointDecoder() {
        super(GML.Point);
        choice = new ChoiceDecoder<CoordinateSequence>();
        choice.addOption(new PosDecoder());
        choice.addOption(new CoordinatesDecoder());
        choice.addOption(new CoordDecoder());
    }

    @Override
    protected Geometry decodeInternal(final BxmlStreamReader r, final QName name) throws Exception {
        r.nextTag();

        CoordinateSequence coordinates = choice.decode(r);

        BXMLDecoderUtil.goToEnd(r, name);
        r.require(EventType.END_ELEMENT, name.getNamespaceURI(), name.getLocalPart());

        Point point = new GeometryFactory().createPoint(coordinates);
        point.setUserData(getCrs());
        return point;
    }

}
