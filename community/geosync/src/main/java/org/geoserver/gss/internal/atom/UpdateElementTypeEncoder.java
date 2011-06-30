package org.geoserver.gss.internal.atom;

import static org.geoserver.wfs.xml.v1_1_0.WFS.UPDATE;
import static org.geotools.filter.v1_1.OGC.Filter;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.xml.namespace.QName;

import net.opengis.wfs.PropertyType;
import net.opengis.wfs.UpdateElementType;

import org.geoserver.gss.impl.GSS;
import org.geoserver.wfs.xml.v1_1_0.WFS;
import org.geotools.feature.NameImpl;
import org.gvsig.bxml.geoserver.Gml3Encoder;
import org.gvsig.bxml.geoserver.Gml3Encoder.AttributeEncoder;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.springframework.util.Assert;

/**
 * Handles the XML encoding of an {@link UpdateElementType} as the content of an {@code atom:entry}
 * 
 */
public class UpdateElementTypeEncoder extends ContentEncoder {

    @Override
    public void encode(BxmlStreamWriter w, Object value) throws IOException {
        final UpdateElementType update = (UpdateElementType) value;
        final QName typeName = update.getTypeName();

        final String namespaceURI = typeName.getNamespaceURI();
        final String localTypeName = typeName.getLocalPart();

        final GSS gss = GSS.get();
        final FeatureType featureType = gss.getFeatureType(namespaceURI, localTypeName);

        final Gml3Encoder gmlencoder = getGmlEncoder();

        startElement(w, UPDATE);
        w.writeNamespace("f", namespaceURI);
        attributes(w, true, "typeName", "f:" + localTypeName);
        {
            @SuppressWarnings("unchecked")
            final List<PropertyType> properties = update.getProperty();
            QName propertyName;
            Object propertyValue;
            String propertyNsUri;
            PropertyDescriptor descriptor;
            Class<?> binding;
            AttributeEncoder attributeEncoder;
            for (PropertyType property : properties) {
                propertyName = property.getName();
                propertyValue = property.getValue();
                propertyNsUri = propertyName.getNamespaceURI();

                String simplePropertyName = propertyName.getLocalPart();
                descriptor = featureType.getDescriptor(new NameImpl(propertyNsUri,
                        simplePropertyName));

                // well, SimpleFeatureType is not behaving correctly and returning null...
                if (descriptor == null && featureType instanceof SimpleFeatureType) {
                    descriptor = ((SimpleFeatureType) featureType)
                            .getDescriptor(simplePropertyName);
                }
                Assert.notNull(descriptor);

                binding = descriptor.getType().getBinding();
                attributeEncoder = Gml3Encoder.getAttributeEncoder(binding);
                Assert.notNull(attributeEncoder);

                startElement(w, WFS.PROPERTY);
                {
                    String prefix = w.getPrefix(propertyNsUri);
                    if (prefix == null) {
                        // we found a property whose namespace is not yet bound. It has to be as the
                        // content of wfs:PropertyName is QName
                        prefix = "p" + Math.abs(new Random().nextInt());
                        w.writeNamespace(prefix, propertyNsUri);
                    }
                    String qName = prefix + ":" + simplePropertyName;
                    element(w, new QName(WFS.NAMESPACE, "Name"), true, qName, true);
                    startElement(w, new QName(WFS.NAMESPACE, "Value"));
                    {
                        attributeEncoder.encode(gmlencoder, propertyValue,
                                ((AttributeDescriptor) descriptor), w);
                    }
                    endElement(w);
                }
                endElement(w);
            }

            final Filter filter = update.getFilter();
            startElement(w, Filter);
            {
                FilterEncoder filterEncoder = new FilterEncoder(w);
                filter.accept(filterEncoder, null);
            }
            endElement(w);
        }
        endElement(w);
    }
}
