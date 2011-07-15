package org.geoserver.bxml.wfs_1_1;

import static org.geoserver.wfs.xml.v1_1_0.WFS.PROPERTY;
import static org.geoserver.wfs.xml.v1_1_0.WFS.UPDATE;
import static org.geotools.filter.v1_1.OGC.Filter;

import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.wfs.UpdateElementType;
import net.opengis.wfs.WfsFactory;
import net.opengis.wfs.impl.WfsFactoryImpl;

import org.geoserver.bxml.AbstractDecoder;
import org.geoserver.bxml.feature.FeatureTypeUtil;
import org.geoserver.bxml.feature.PropertyDecoder;
import org.geoserver.bxml.filter_1_1.FilterChainDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class UpdateElementTypeDecoder extends AbstractDecoder<UpdateElementType> {

    private final UpdateElementType element;

    public UpdateElementTypeDecoder() {
        super(UPDATE);
        final WfsFactory factory = WfsFactoryImpl.eINSTANCE;
        element = factory.createUpdateElementType();
    }

    @Override
    protected void decodeElement(BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if (Filter.equals(name)) {
            FilterChainDecoder filterDecoder = new FilterChainDecoder(Filter);
            element.setFilter(filterDecoder.decode(r));
        }

        if (PROPERTY.equals(name)) {
            element.getProperty().add(new PropertyDecoder(element.getTypeName()).decode(r));
        }
    }

    @Override
    protected void decodeAttributtes(BxmlStreamReader r, Map<QName, String> attributes) {
        element.setTypeName(FeatureTypeUtil.buildFeatureTypeName(r, attributes, name));
    }

    @Override
    protected UpdateElementType buildResult() {
        return element;
    }

}
