package org.geoserver.gss.internal.atom.decoders;

import static org.geoserver.gss.internal.atom.Atom.scheme;
import static org.geoserver.gss.internal.atom.Atom.term;

import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.Atom;
import org.geoserver.gss.internal.atom.CategoryImpl;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class CategoryDecoder extends AbstractDecoder<CategoryImpl> {

    private CategoryImpl category;

    public CategoryDecoder() {
        super(Atom.category);
        category = new CategoryImpl();
    }

    @Override
    protected void decodeAttributtes(BxmlStreamReader r, Map<QName, String> attributes) throws IOException {
        category.setScheme(attributes.get(scheme));
        category.setTerm(attributes.get(term));
    }

    @Override
    protected void decodeElement(BxmlStreamReader r) throws IOException {
    }

    @Override
    protected CategoryImpl buildResult() {
        return category;
    }

}
