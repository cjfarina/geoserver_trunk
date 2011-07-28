package org.geoserver.bxml.feature;

import static org.geoserver.wfs.xml.v1_1_0.WFS.PROPERTY;

import java.util.Iterator;

import javax.xml.namespace.QName;

import net.opengis.wfs.PropertyType;
import net.opengis.wfs.WfsFactory;
import net.opengis.wfs.impl.WfsFactoryImpl;

import org.geoserver.bxml.ChoiceDecoder;
import org.geoserver.bxml.SequenceDecoder;
import org.geoserver.bxml.SetterDecoder;
import org.geoserver.bxml.base.SimpleDecoder;
import org.geoserver.bxml.base.StringDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class PropertyDecoder extends SimpleDecoder<PropertyType> {

    final WfsFactory factory = WfsFactoryImpl.eINSTANCE;

    private final QName typeName;

    public PropertyDecoder(QName typeName) {
        super(PROPERTY);
        this.typeName = typeName;
    }

    @Override
    public PropertyType decode(BxmlStreamReader r) throws Exception {
        final QName elementName = r.getElementName();
        canHandle(elementName);
        r.require(EventType.START_ELEMENT, elementName.getNamespaceURI(),
                elementName.getLocalPart());
        PropertyType property = factory.createPropertyType();

        ChoiceDecoder<Object> choice = new ChoiceDecoder<Object>();

        choice.addOption(new SetterDecoder<Object>(new PropertyNameDecoder(typeName), property, "name"));
        choice.addOption(new SetterDecoder<Object>(new PropertyValueDecoder(),
                property, "value"));

        SequenceDecoder<Object> seq = new SequenceDecoder<Object>(1, 1);
        seq.add(choice, 0, Integer.MAX_VALUE);

        r.nextTag();
        Iterator<Object> decode = seq.decode(r);
        // consume and let functors do their job
        while (decode.hasNext()) {
            decode.next();
        }

        r.require(EventType.END_ELEMENT, elementName.getNamespaceURI(), elementName.getLocalPart());
        return property;
    }
}
