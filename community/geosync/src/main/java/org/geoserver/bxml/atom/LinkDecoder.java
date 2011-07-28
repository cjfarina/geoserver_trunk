package org.geoserver.bxml.atom;

import javax.xml.namespace.QName;

import org.geoserver.bxml.BXMLDecoderUtil;
import org.geoserver.bxml.base.SimpleDecoder;
import org.geoserver.gss.internal.atom.Atom;
import org.geoserver.gss.internal.atom.CategoryImpl;
import org.geoserver.gss.internal.atom.LinkImpl;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;
import org.springframework.util.Assert;

import static org.geoserver.gss.internal.atom.Atom.href;
import static org.geoserver.gss.internal.atom.Atom.hreflang;
import static org.geoserver.gss.internal.atom.Atom.length;
import static org.geoserver.gss.internal.atom.Atom.rel;
import static org.geoserver.gss.internal.atom.Atom.title;
import static org.geoserver.gss.internal.atom.Atom.type;

public class LinkDecoder extends SimpleDecoder<LinkImpl> {

    public LinkDecoder() {
        super(Atom.link);
    }

    @Override
    public LinkImpl decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, elemName.getNamespaceURI(), elemName.getLocalPart());
        final QName name = r.getElementName();
        Assert.isTrue(canHandle(name));
        LinkImpl link = new LinkImpl();

        link.setHref(r.getAttributeValue(null, href.getLocalPart()));
        link.setRel(r.getAttributeValue(null, rel.getLocalPart()));
        link.setType(r.getAttributeValue(null, type.getLocalPart()));
        link.setHreflang(r.getAttributeValue(null, hreflang.getLocalPart()));
        link.setTitle(r.getAttributeValue(null, title.getLocalPart()));
        String lengthValue = r.getAttributeValue(null, length.getLocalPart());
        if (lengthValue != null) {
            link.setLength(BXMLDecoderUtil.parseLongValue(lengthValue, length.getLocalPart()));
        }
        
        r.nextTag();
        
        return link;
    }
}
