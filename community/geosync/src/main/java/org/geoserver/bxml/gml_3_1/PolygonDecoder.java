package org.geoserver.bxml.gml_3_1;

import static org.geotools.gml3.GML.Polygon;
import static org.geotools.gml3.GML.exterior;
import static org.geotools.gml3.GML.interior;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.geoserver.bxml.ChoiceDecoder;
import org.geoserver.bxml.SequenceDecoder;
import org.geoserver.bxml.SetterDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.google.common.collect.Iterators;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class PolygonDecoder extends AbstractGeometryDecoder<Geometry> {

    public PolygonDecoder() {
        super(Polygon);
    }

    @Override
    public Geometry decodeInternal(BxmlStreamReader r, QName name) throws Exception {
        ChoiceDecoder<Object> choice = new ChoiceDecoder<Object>();

        PolygonRings polygonRing = new PolygonRings();

        choice.addOption(new SetterDecoder<Object>(new PolygonLineRingDecoder(getCrs(),
                getDimension(), interior), polygonRing, "interior"));
        choice.addOption(new SetterDecoder<Object>(new PolygonLineRingDecoder(getCrs(),
                getDimension(), exterior), polygonRing, "exterior"));

        SequenceDecoder<Object> seq = new SequenceDecoder<Object>(1, 1);
        seq.add(choice, 0, Integer.MAX_VALUE);

        r.nextTag();
        final Iterator<Object> iterator = seq.decode(r);
        Iterators.toArray(iterator, Object.class);

        Polygon geometry = new GeometryFactory()
                .createPolygon(
                        polygonRing.getExterior(),
                        polygonRing.getInterior().toArray(
                                new LinearRing[polygonRing.getInterior().size()]));

        return geometry;
    }

    public class PolygonRings {

        private LinearRing exterior;

        private List<LinearRing> interior;

        public LinearRing getExterior() {
            return exterior;
        }

        public void setExterior(LinearRing exterior) {
            this.exterior = exterior;
        }

        public List<LinearRing> getInterior() {
            if (interior == null) {
                interior = new ArrayList<LinearRing>(2);
            }
            return interior;
        }

        public void setInterior(List<LinearRing> interior) {
            this.interior = interior;
        }

    }

}
