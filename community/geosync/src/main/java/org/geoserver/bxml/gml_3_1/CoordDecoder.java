package org.geoserver.bxml.gml_3_1;

import static org.geotools.gml3.GML.coord;

import javax.xml.namespace.QName;

import org.geoserver.bxml.SequenceDecoder;
import org.geoserver.bxml.base.DoubleDecoder;
import org.geotools.gml2.GML;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.google.common.collect.Iterators;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;

public class CoordDecoder extends AbstractGeometryDecoder<CoordinateSequence> {

    private static final QName X = new QName(GML.NAMESPACE, "X");

    private static final QName Y = new QName(GML.NAMESPACE, "Y");

    private static final QName Z = new QName(GML.NAMESPACE, "Z");

    private SequenceDecoder<Double> seq;

    public CoordDecoder() {
        super(coord);
        seq = new SequenceDecoder<Double>();
        seq.add(new DoubleDecoder(X), 1, 1);
        seq.add(new DoubleDecoder(Y), 1, 1);
        seq.add(new DoubleDecoder(Z), 1, 1);
    }

    @Override
    public CoordinateSequence decodeInternal(final BxmlStreamReader r, QName name) throws Exception {
        r.nextTag();
        Double[] doubles = Iterators.toArray(seq.decode(r), Double.class);

        final int dimension = doubles.length;
        double[] values = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            values[i] = doubles[i].doubleValue();
        }
        return new PackedCoordinateSequence.Double(values, dimension);
    }

}
