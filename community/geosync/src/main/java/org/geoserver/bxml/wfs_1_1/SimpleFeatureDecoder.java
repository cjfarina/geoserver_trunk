package org.geoserver.bxml.wfs_1_1;

import static org.geotools.gml3.GML.id;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.Decoder;
import org.geoserver.bxml.SetterDecoder;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import com.google.common.collect.Iterators;

/**
 * The Class SimpleFeatureDecoder.
 * 
 * @author cfarina
 */
public class SimpleFeatureDecoder implements Decoder<SimpleFeature> {

    /** The namespace. */
    private final String namespace;

    /** The catalog. */
    private final Catalog catalog;

    /**
     * Instantiates a new simple feature decoder.
     * 
     * @param catalog the catalog
     * @param namespaceURI the namespace uri
     */
    public SimpleFeatureDecoder(Catalog catalog, String namespaceURI) {
        this.namespace = namespaceURI;
        this.catalog = catalog;
    }

    /**
     * Decode.
     * 
     * @param r the r
     * @return the simple feature
     * @throws Exception the exception
     */
    @Override
    public SimpleFeature decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, null, null);
        // SimpleFeature simpleFeature =
        QName typeName = r.getElementName();

        FeatureTypeInfo featureTypeInfo = catalog.getFeatureTypeByName(typeName.getNamespaceURI(),
                typeName.getLocalPart());
        FeatureType featureType = featureTypeInfo.getFeatureType();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder((SimpleFeatureType) featureType);
        String featureId = r.getAttributeValue(null, id.getLocalPart());

        SimpleFeatureSequenceDecoder<Object> seq = new SimpleFeatureSequenceDecoder<Object>(
                namespace, 1, 1);
        SimpleFeatureAttributes simpleFeatureAttributes = new SimpleFeatureAttributes();
        seq.add(new SetterDecoder<Object>(new SimpleFeatureAttributeDecoder(namespace),
                simpleFeatureAttributes, "attributes"), 0, Integer.MAX_VALUE);

        r.nextTag();
        Iterator<Object> iterator = seq.decode(r);
        Iterators.toArray(iterator, Object.class);

        int index = 0;
        for (Object attribute : simpleFeatureAttributes.getAttributes()) {
            builder.set(index, attribute);
            index++;
        }
        SimpleFeature simpleFeature = builder.buildFeature(featureId);
        return simpleFeature;
    }

    /**
     * Can handle.
     * 
     * @param name the name
     * @return true, if successful
     */
    @Override
    public boolean canHandle(QName name) {
        return namespace.equals(name.getNamespaceURI());
    }

    /**
     * Gets the targets.
     * 
     * @return the targets
     */
    @Override
    public Set<QName> getTargets() {
        return new HashSet<QName>();
    }

}
