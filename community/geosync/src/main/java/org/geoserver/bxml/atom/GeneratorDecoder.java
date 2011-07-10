package org.geoserver.bxml.atom;

import static org.geoserver.gss.internal.atom.Atom.uri;
import static org.geoserver.gss.internal.atom.Atom.version;

import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.geoserver.gss.internal.atom.Atom;
import org.geoserver.gss.internal.atom.GeneratorImpl;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class GeneratorDecoder extends AbstractDecoder<GeneratorImpl> {

    private GeneratorImpl generator;

    public GeneratorDecoder() {
        super(Atom.generator);
        generator = new GeneratorImpl();
    }

    @Override
    protected void decodeAttributtes(BxmlStreamReader r, Map<QName, String> attributes)
            throws Exception {
        generator.setUri(attributes.get(uri));
        generator.setVersion(attributes.get(version));
    }

    protected void setStringValue(String value) {
        generator.setValue(value);
    }

    @Override
    protected void decodeElement(BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if (generator.equals(name)) {
            generator.setValue(readStringValue(r, Atom.generator));
        }
    }

    @Override
    protected GeneratorImpl buildResult() {
        return generator;
    }

}
