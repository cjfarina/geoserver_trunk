package org.geoserver.bxml.wfs_1_1;

import static org.geoserver.wfs.xml.v1_1_0.WFS.INSERT;

import javax.xml.namespace.QName;

import net.opengis.wfs.InsertElementType;
import net.opengis.wfs.WfsFactory;
import net.opengis.wfs.impl.WfsFactoryImpl;

import org.eclipse.emf.ecore.EObject;
import org.geoserver.bxml.SequenceDecoder;
import org.geoserver.bxml.SetterDecoder;
import org.geoserver.bxml.base.SimpleDecoder;
import org.geoserver.catalog.Catalog;
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
        this.catalog = null;
    }

    public InsertElementTypeDecoder(Catalog catalog) {
        super(INSERT);
        factory = WfsFactoryImpl.eINSTANCE;
        this.catalog = catalog;
    }

    @Override
    public EObject decode(BxmlStreamReader r) throws Exception {
        final QName elementName = r.getElementName();
        Assert.isTrue(canHandle(elementName));
        r.require(EventType.START_ELEMENT, elementName.getNamespaceURI(),
                elementName.getLocalPart());

        final InsertElementType element = factory.createInsertElementType();

        String namespaceURI = r.getNamespaceURI("f");
        SequenceDecoder<SimpleFeature> sequenceDecoder = new SequenceDecoder<SimpleFeature>(1, 1);
        SimpleFeatureDecoder simpleFeatureDecoder = new SimpleFeatureDecoder(catalog, namespaceURI);
        SetterDecoder<SimpleFeature> setterDecoder = new SetterDecoder<SimpleFeature>(
                simpleFeatureDecoder, element, "feature");
        sequenceDecoder.add(setterDecoder, 1, Integer.MAX_VALUE);

        r.nextTag();

        SimpleFeature decode = simpleFeatureDecoder.decode(r);

        r.nextTag();

        r.require(EventType.END_ELEMENT, elementName.getNamespaceURI(), elementName.getLocalPart());
        return element;
    }

}
