package org.geoserver.bxml.filter_1_1.spatial;

import static org.geotools.filter.v1_1.OGC.BBOX;
import static org.geotools.gml3.GML.Envelope;

import java.util.logging.Level;

import javax.xml.namespace.QName;

import org.geoserver.bxml.filter_1_1.ExpressionChainDecoder;
import org.geoserver.bxml.filter_1_1.FilterLinkDecoder;
import org.geoserver.bxml.gml_3_1.EnvelopeDecoder;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.v1_1.OGC;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;

public class BBOXFilterDecoder extends FilterLinkDecoder {

    protected String propertyName;

    protected ReferencedEnvelope envelope;

    public BBOXFilterDecoder() {
        super(BBOX);
    }

    @Override
    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if (OGC.NAMESPACE.equals(name.getNamespaceURI())) {
            AttributeExpressionImpl expression = (AttributeExpressionImpl) new ExpressionChainDecoder()
                    .decode(r);
            propertyName = expression.getPropertyName();
        } else if (Envelope.equals(name)) {
            envelope = (ReferencedEnvelope) new EnvelopeDecoder().decode(r);
        }
    }

    @Override
    protected Filter buildResult() {
        String epsCode = null;
        try {
            epsCode = CRS.lookupEpsgCode(envelope.crs(), true).toString();
        } catch (FactoryException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }
        return ff.bbox(propertyName, envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(),
                envelope.getMaxY(), epsCode);
    }
}
