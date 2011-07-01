package org.geoserver.gss.internal.atom.decoders;

import static org.geoserver.gss.internal.atom.Atom.href;
import static org.geoserver.gss.internal.atom.Atom.hreflang;
import static org.geoserver.gss.internal.atom.Atom.length;
import static org.geoserver.gss.internal.atom.Atom.rel;
import static org.geoserver.gss.internal.atom.Atom.title;
import static org.geoserver.gss.internal.atom.Atom.type;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.LinkImpl;
import org.geotools.util.logging.Logging;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class LinkDecoder extends AbstractDecoder<LinkImpl> {

    private LinkImpl link;

    protected final Logger LOGGER;

    public LinkDecoder() {
        LOGGER = Logging.getLogger(getClass());
        link = new LinkImpl();
    }

    @Override
    protected void decodeAttributtes(BxmlStreamReader r, Map<QName, String> attributes)
            throws IOException {
        link.setHref(attributes.get(href));
        link.setRel(attributes.get(rel));
        link.setType(attributes.get(type));
        link.setHreflang(attributes.get(hreflang));
        link.setTitle(attributes.get(title));
        if (attributes.get(length) != null) {
            link.setLength(parseLongValue(attributes.get(length), length.getLocalPart()));
        }
    }

    @Override
    protected void decodeElement(BxmlStreamReader r) throws IOException {
        QName name = r.getElementName();

        if (href.equals(name)) {
        }

        if (rel.equals(name)) {
        }

        if (type.equals(name)) {
        }

        if (hreflang.equals(name)) {
        }

        if (title.equals(name)) {
        }

        if (length.equals(name)) {
            link.setLength(readLongValue(r, length));
        }

    }

    @Override
    protected LinkImpl buildResult() {
        return link;
    }

}
