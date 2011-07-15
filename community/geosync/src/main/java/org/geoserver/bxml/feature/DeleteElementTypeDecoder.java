package org.geoserver.bxml.feature;

import static org.geoserver.wfs.xml.v1_1_0.WFS.DELETE;
import static org.geotools.filter.v1_1.OGC.Filter;

import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.wfs.DeleteElementType;
import net.opengis.wfs.WfsFactory;
import net.opengis.wfs.impl.WfsFactoryImpl;

import org.geoserver.bxml.AbstractDecoder;
import org.geoserver.bxml.filter_1_1.FilterChainDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class DeleteElementTypeDecoder extends AbstractDecoder<DeleteElementType> {

    private final DeleteElementType element;

    public DeleteElementTypeDecoder() {
        super(DELETE);
        final WfsFactory factory = WfsFactoryImpl.eINSTANCE;
        element = factory.createDeleteElementType();
    }

    @Override
    protected void decodeElement(BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if (Filter.equals(name)) {
            FilterChainDecoder filterDecoder = new FilterChainDecoder(Filter);
            element.setFilter(filterDecoder.decode(r));
        }
    }

    @Override
    protected void decodeAttributtes(BxmlStreamReader r, Map<QName, String> attributes) {
        element.setTypeName(FeatureTypeUtil.buildFeatureTypeName(r, attributes, name));
    }

    @Override
    protected DeleteElementType buildResult() {
        return element;
    }

}
