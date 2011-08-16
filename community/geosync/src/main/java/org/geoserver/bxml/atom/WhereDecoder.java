package org.geoserver.bxml.atom;

import static org.geoserver.gss.internal.atom.GeoRSS.where;

import javax.xml.namespace.QName;

import org.geoserver.bxml.ChoiceDecoder;
import org.geoserver.bxml.base.SimpleDecoder;
import org.geoserver.bxml.gml_3_1.EnvelopeDecoder;
import org.geoserver.bxml.gml_3_1.GeometryDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class WhereDecoder extends SimpleDecoder<Object> {

    @SuppressWarnings("rawtypes")
    private ChoiceDecoder choice;

    @SuppressWarnings("unchecked")
    public WhereDecoder() {
        super(where);
        choice = new ChoiceDecoder<Object>();
        choice.addOption(new GeometryDecoder());
        choice.addOption(new EnvelopeDecoder());
    }

    @Override
    public Object decode(BxmlStreamReader r) throws Exception {
        final QName elementName = r.getElementName();
        r.require(EventType.START_ELEMENT, elementName.getNamespaceURI(),
                elementName.getLocalPart());

        r.nextTag();
        
        Object value = choice.decode(r);
        r.nextTag();

        r.require(EventType.END_ELEMENT, elementName.getNamespaceURI(), elementName.getLocalPart());
        return value;
    }

}
