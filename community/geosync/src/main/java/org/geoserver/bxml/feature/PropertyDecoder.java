package org.geoserver.bxml.feature;

import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;

import net.opengis.wfs.PropertyType;
import net.opengis.wfs.WfsFactory;
import net.opengis.wfs.impl.WfsFactoryImpl;

import static org.geoserver.wfs.xml.v1_1_0.WFS.PROPERTY;

public class PropertyDecoder extends AbstractDecoder<PropertyType> {

    final WfsFactory factory = WfsFactoryImpl.eINSTANCE;

    public static final QName Name = new QName("http://www.opengis.net/wfs", "Name");
    public static final QName Value = new QName("http://www.opengis.net/wfs", "Value");

    private PropertyType property;

    private final QName typeName;

    public PropertyDecoder(QName typeName) {
        super(PROPERTY);
        this.typeName = typeName;
        property = factory.createPropertyType();
    }

    protected void decodeElement(final BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();
        if (Name.equals(name)){
            String nameString = readStringValue(r, name);
            QName propertyName = FeatureTypeUtil.buildQName(nameString, typeName.getNamespaceURI());
            property.setName(propertyName);
        }
        
        if(Value.equals(name)){
            property.setValue(new PropertyValueDecoder().decode(r));
        }
    }
    
    protected void decodeAttributtes(final BxmlStreamReader r, Map<QName, String> attributes)
            throws Exception {
    }

    @Override
    protected PropertyType buildResult() {
        return property;
    }

}
