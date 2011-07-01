package org.geoserver.gss.internal.atom.decoders;

import static org.geoserver.gss.internal.atom.Atom.email;
import static org.geoserver.gss.internal.atom.Atom.uri;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.Atom;
import org.geoserver.gss.internal.atom.PersonImpl;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class PersonDecoder extends AbstractDecoder<PersonImpl> {

    private PersonImpl person;

    public PersonDecoder(final QName name) {
        super(name);
        person = new PersonImpl();
        
    }

    @Override
    protected void decodeElement(BxmlStreamReader r) throws IOException {
        QName name = r.getElementName();
        
        if (Atom.name.equals(name)) {
            person.setName(readStringValue(r, Atom.name));
        }

        if (email.equals(name)) {
            person.setEmail(readStringValue(r, email));
        }

        if (uri.equals(name)) {
            person.setUri(readStringValue(r, uri));
        }
    }

    @Override
    protected PersonImpl buildResult() {
        return person;
    }
    
}
