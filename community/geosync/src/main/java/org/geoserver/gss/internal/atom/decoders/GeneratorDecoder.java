package org.geoserver.gss.internal.atom.decoders;

import static org.geoserver.gss.internal.atom.Atom.uri;
import static org.geoserver.gss.internal.atom.Atom.version;

import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.Atom;
import org.geoserver.gss.internal.atom.GeneratorImpl;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class GeneratorDecoder extends AbstractDecoder<GeneratorImpl> {

    private GeneratorImpl generator;

    @Override
    public void setupInitialData() {
        generator = new GeneratorImpl();
    }

    @Override
    public void decodeAttributtes(BxmlStreamReader r, Map<QName, String> attributes)
            throws IOException {
        generator.setUri(attributes.get(uri));
        generator.setVersion(attributes.get(version));
    }

    public void setStringValue(String value) {
        generator.setValue(value);
    }

    @Override
    public void decodeElement(BxmlStreamReader r) throws IOException {
        QName name = r.getElementName();

        if (generator.equals(name)) {
            generator.setValue(readStringValue(r, Atom.generator));
        }
    }

    @Override
    public GeneratorImpl buildResult() {
        return generator;
    }

}
