package org.geoserver.bxml.atom;

import static org.geoserver.gss.internal.atom.GeoRSS.where;

import javax.xml.namespace.QName;

import org.geoserver.bxml.base.SimpleDecoder;
import org.geoserver.bxml.gml_3_1.GMLChainDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class WhereDecoder extends SimpleDecoder<Object> {

    public WhereDecoder() {
        super(where);
    }
    
    @Override
    public Object decode(BxmlStreamReader r) throws Exception {
        final QName elementName = r.getElementName();
        r.require(EventType.START_ELEMENT, elementName.getNamespaceURI(),
                elementName.getLocalPart());

        r.nextTag();
        Object value = new GMLChainDecoder().decode(r);
        r.nextTag();

        r.require(EventType.END_ELEMENT, elementName.getNamespaceURI(), elementName.getLocalPart());
        return value;
    }

}
