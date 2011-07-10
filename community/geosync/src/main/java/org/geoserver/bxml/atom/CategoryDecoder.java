package org.geoserver.bxml.atom;

import static org.geoserver.gss.internal.atom.Atom.scheme;
import static org.geoserver.gss.internal.atom.Atom.term;

import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
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
    protected void decodeAttributtes(BxmlStreamReader r, Map<QName, String> attributes)
            throws Exception {
        category.setScheme(attributes.get(scheme));
        category.setTerm(attributes.get(term));
    }

    @Override
    protected void decodeElement(BxmlStreamReader r) throws Exception {
    }

    @Override
    protected CategoryImpl buildResult() {
        return category;
    }

}
