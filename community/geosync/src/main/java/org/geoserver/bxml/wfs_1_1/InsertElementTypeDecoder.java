package org.geoserver.bxml.wfs_1_1;

import static org.geoserver.wfs.xml.v1_1_0.WFS.INSERT;

import java.util.Iterator;

import javax.xml.namespace.QName;

import net.opengis.wfs.InsertElementType;
import net.opengis.wfs.WfsFactory;
import net.opengis.wfs.impl.WfsFactoryImpl;

import org.eclipse.emf.ecore.EObject;
import org.geoserver.bxml.base.SimpleDecoder;
import org.geoserver.catalog.Catalog;
import org.geoserver.platform.GeoServerExtensions;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.util.Assert;

public class InsertElementTypeDecoder extends SimpleDecoder<EObject> {

    private final WfsFactory factory;

    private final Catalog catalog;

    public InsertElementTypeDecoder() {
        super(INSERT);
        factory = WfsFactoryImpl.eINSTANCE;
        this.catalog = (Catalog) GeoServerExtensions.bean("catalog");
    }

    public InsertElementTypeDecoder(Catalog catalog) {
        super(INSERT);
        Assert.notNull(catalog);
        factory = WfsFactoryImpl.eINSTANCE;
        this.catalog = catalog;
    }

    @Override
    public EObject decode(BxmlStreamReader r) throws Exception {
        final QName elementName = r.getElementName();
        Assert.isTrue(canHandle(elementName));
        r.require(EventType.START_ELEMENT, elementName.getNamespaceURI(),
                elementName.getLocalPart());

        final InsertElementType insertElement = factory.createInsertElementType();

        String namespaceURI = r.getNamespaceURI("f");
        SimpleFeatureSequenceDecoder<SimpleFeature> sequenceDecoder = new SimpleFeatureSequenceDecoder<SimpleFeature>(
                namespaceURI, 1, 1);
        sequenceDecoder.add(new SimpleFeatureDecoder(catalog, namespaceURI), 1, Integer.MAX_VALUE);

        r.nextTag();
        Iterator<SimpleFeature> iterator = sequenceDecoder.decode(r);
        while (iterator.hasNext()) {
            SimpleFeature simpleFeature = (SimpleFeature) iterator.next();
            insertElement.getFeature().add(simpleFeature);
        }

        r.require(EventType.END_ELEMENT, elementName.getNamespaceURI(), elementName.getLocalPart());
        return insertElement;
    }

}
