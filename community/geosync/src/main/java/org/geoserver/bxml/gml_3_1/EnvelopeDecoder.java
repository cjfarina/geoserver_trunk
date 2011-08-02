package org.geoserver.bxml.gml_3_1;

import static org.geotools.gml3.GML.Envelope;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.geoserver.bxml.ChoiceDecoder;
import org.geoserver.bxml.SequenceDecoder;
import org.geoserver.bxml.SetterDecoder;
import org.geoserver.bxml.filter_1_1.AbstractTypeDecoder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gml3.GML;
import org.gvsig.bxml.stream.BxmlStreamReader;

import com.google.common.collect.Iterators;
import com.vividsolutions.jts.geom.Envelope;

public class EnvelopeDecoder extends AbstractTypeDecoder<ReferencedEnvelope> {

    private static final QName lowerCorner = new QName(GML.NAMESPACE, "lowerCorner");

    private static final QName upperCorner = new QName(GML.NAMESPACE, "upperCorner");

    public EnvelopeDecoder() {
        super(Envelope);
    }

    @Override
    protected ReferencedEnvelope decodeInternal(BxmlStreamReader r, QName name) throws Exception {
        ChoiceDecoder<Object> choice = new ChoiceDecoder<Object>();

        EnvelopeParams params = new EnvelopeParams();
        choice.addOption(new SetterDecoder<Object>(new DoubleListDecoder(lowerCorner),
                params, "lowerCornerValues"));
        choice.addOption(new SetterDecoder<Object>(new DoubleListDecoder(upperCorner),
                params, "uperCornerValues"));

        SequenceDecoder<Object> seq = new SequenceDecoder<Object>(1, 1);
        seq.add(choice, 0, Integer.MAX_VALUE);

        r.nextTag();
        final Iterator<Object> iterator = seq.decode(r);
        Iterators.toArray(iterator, Object.class);

        Envelope envelope = new Envelope(params.getLowerCornerValues()[0], params.getUperCornerValues()[0],
                params.getLowerCornerValues()[1], params.getUperCornerValues()[1]);
        //TODO: set crs
        ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(envelope, null);
        
        return referencedEnvelope;
    }
    
    public class EnvelopeParams {
        
        private double[] lowerCornerValues;

        private double[] uperCornerValues;

        public double[] getUperCornerValues() {
            return uperCornerValues;
        }
        
        public void setUperCornerValues(double[] uperCornerValues) {
            this.uperCornerValues = uperCornerValues;
        }

        public double[] getLowerCornerValues() {
            return lowerCornerValues;
        }

        public void setLowerCornerValues(double[] lowerCornerValues) {
            this.lowerCornerValues = lowerCornerValues;
        }
    }

}
