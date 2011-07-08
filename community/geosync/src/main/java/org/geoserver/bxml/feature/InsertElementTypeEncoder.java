package org.geoserver.bxml.feature;

import static org.geoserver.wfs.xml.v1_1_0.WFS.INSERT;

import java.io.IOException;
import java.util.Iterator;

import net.opengis.wfs.DeleteElementType;
import net.opengis.wfs.InsertElementType;

import org.geoserver.bxml.atom.ContentEncoder;
import org.gvsig.bxml.geoserver.Gml3Encoder;
import org.gvsig.bxml.geoserver.SimpleFeatureEncoder;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Handles the encoding of a {@link DeleteElementType} as the content of an {@code atom:enty}
 */
public class InsertElementTypeEncoder extends ContentEncoder {

    @SuppressWarnings("unchecked")
    @Override
    public void encode(final BxmlStreamWriter w, final Object value) throws IOException {
        InsertElementType insert = (InsertElementType) value;
        // WFS namespace is already bound at root document
        w.writeStartElement(INSERT.getNamespaceURI(), INSERT.getLocalPart());
        {
            Gml3Encoder gmlEncoder = getGmlEncoder();
            SimpleFeatureEncoder simpleFeatureEncoder = new SimpleFeatureEncoder(gmlEncoder);
            Feature feature;
            for (Iterator<Feature> it = insert.getFeature().iterator(); it.hasNext();) {
                feature = it.next();
                if (feature instanceof SimpleFeature) {
                    final String namespaceURI = feature.getType().getName().getNamespaceURI();
                    String prefix = w.getPrefix(namespaceURI);
                    if (null == prefix) {
                        w.writeNamespace("f", namespaceURI);
                    }
                    // new SimpleFeatureEncoder2().encode(w, (SimpleFeature) feature);
                    simpleFeatureEncoder.encode((SimpleFeature) feature, w);
                } else {
                    throw new UnsupportedOperationException("Only SimpleFeature supported so far");
                }
            }
        }
        w.writeEndElement();
    }

}
