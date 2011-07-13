package org.geoserver.bxml.gml_3_1;

import static org.geotools.gml3.GML.Envelope;

import java.util.List;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.geotools.gml3.GML;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class EnvelopeDecoder extends AbstractDecoder<Object> {

    private static final QName lowerCorner = new QName(GML.NAMESPACE, "lowerCorner");

    private static final QName upperCorner = new QName(GML.NAMESPACE, "upperCorner");

    private Coordinate lowerCornerValues;

    private Coordinate uperCornerValues;

    public EnvelopeDecoder() {
        super(Envelope);
    }

    @SuppressWarnings("unchecked")
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();
        if (lowerCorner.equals(name)) {
            Object decode = new CoordinatePostListParser(name, 2).decode(r);
            lowerCornerValues = ((List<Coordinate>)decode).get(0);
        } else if (upperCorner.equals(name)) {
            Object decode = new CoordinatePostListParser(name, 2).decode(r);
            uperCornerValues = ((List<Coordinate>)decode).get(0);
        }
    }

    @Override
    protected Envelope buildResult() {
        Envelope envelope = new Envelope(lowerCornerValues.x, uperCornerValues.x, lowerCornerValues.y,
                uperCornerValues.y);
        return envelope;
    }
}
