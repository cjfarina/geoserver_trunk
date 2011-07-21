package org.geoserver.bxml.gml_3_1;

import static org.geotools.gml3.GML.coord;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.geoserver.bxml.SequenceDecoder;
import org.geotools.gml2.GML;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

import com.google.common.collect.Iterators;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;

public class CoordDecoder implements Decoder<CoordinateSequence> {

    private static final QName X = new QName(GML.NAMESPACE, "X");

    private static final QName Y = new QName(GML.NAMESPACE, "Y");

    private static final QName Z = new QName(GML.NAMESPACE, "Z");

    private SequenceDecoder<Double> seq;

    public CoordDecoder() {
        seq = new SequenceDecoder<Double>();
        seq.add(new DoubleDecoder(X), 1, 1);
        seq.add(new DoubleDecoder(Y), 0, 0);
        seq.add(new DoubleDecoder(Z), 0, 0);
    }

    @Override
    public CoordinateSequence decode(final BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, coord.getNamespaceURI(), coord.getLocalPart());
        r.nextTag();
        Double[] doubles = Iterators.toArray(seq.decode(r), Double.class);
        r.require(EventType.END_ELEMENT, coord.getNamespaceURI(), coord.getLocalPart());

        final int dimension = doubles.length;
        double[] values = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            values[i] = doubles[i].doubleValue();
        }
        return new PackedCoordinateSequence.Double(values, dimension);
    }

    @Override
    public boolean canHandle(QName name) {
        return coord.equals(name);
    }

    @Override
    public Set<QName> getTargets() {
        return Collections.singleton(coord);
    }

}
