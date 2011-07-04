package org.geoserver.gss.internal.atom.decoders;

import static org.geoserver.gss.internal.atom.Atom.source;
import static org.geoserver.gss.internal.atom.Atom.type;
import static org.geoserver.wfs.xml.v1_1_0.WFS.DELETE;

import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.Atom;
import org.geoserver.gss.internal.atom.ContentImpl;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class ContentDecoder extends AbstractDecoder<ContentImpl> {

    private final ContentImpl content;

    public ContentDecoder() {
        super(Atom.content);
        content = new ContentImpl();
    }

    @Override
    protected void decodeElement(BxmlStreamReader r) throws IOException {
        QName name = r.getElementName();

        if (DELETE.equals(name)) {
            DeleteElementTypeDecoder deleteElementTypeDecoder = new DeleteElementTypeDecoder();
            content.setValue(deleteElementTypeDecoder.decode(r));
        }
    }

    @Override
    protected void decodeAttributtes(BxmlStreamReader r, Map<QName, String> attributes)
            throws IOException {
        QName name = r.getElementName();
        if (Atom.content.equals(name)) {
            if (attributes.get(type) != null) {
                content.setType(attributes.get(type));
            }
            if (attributes.get(source) != null) {
                content.setSrc(attributes.get(source));
            }
        }
    }

    @Override
    protected ContentImpl buildResult() {
        return content;
    }

}
