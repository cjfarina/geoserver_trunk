package org.geoserver.gss.internal.atom.decoders;

import static org.geoserver.wfs.xml.v1_1_0.WFS.DELETE;
import static org.geoserver.gss.internal.atom.Atom.id;
import static org.geoserver.gss.internal.atom.Atom.typeName;
import static org.geotools.filter.v1_1.OGC.Filter;

import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.wfs.DeleteElementType;
import net.opengis.wfs.WfsFactory;
import net.opengis.wfs.impl.WfsFactoryImpl;

import org.geoserver.gss.internal.atom.DeleteElementTypeEncoder;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class DeleteElementTypeDecoder extends AbstractDecoder<DeleteElementType> {

    private final DeleteElementType element;

    private String featureNamespace;
    
    public DeleteElementTypeDecoder() {
        super(DELETE);
        final WfsFactory factory = WfsFactoryImpl.eINSTANCE;
        element = factory.createDeleteElementType();
    }

    @Override
    protected void decodeElement(BxmlStreamReader r) throws IOException {
        QName name = r.getElementName();
        
        if (Filter.equals(name)) {
            FilterDecoder filterDecoder = new FilterDecoder();
            element.setFilter(filterDecoder.decode(r));
        }
    }

    @Override
    protected void decodeAttributtes(BxmlStreamReader r, Map<QName, String> attributes) {
        QName name = r.getElementName();
        featureNamespace = name.getNamespaceURI();
        name.getPrefix();
        /*if(DELETE.equals(name)){
            if (attributes.get(typeName) != null) {
                element.setTypeName(attributes.get(typeName));
            }
        }*/
    }

    @Override
    protected DeleteElementType buildResult() {
        return element;
    }

}
