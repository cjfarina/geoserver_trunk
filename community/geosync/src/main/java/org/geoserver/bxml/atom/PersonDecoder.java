package org.geoserver.bxml.atom;



import java.util.Iterator;

import javax.xml.namespace.QName;

import org.geoserver.bxml.ChoiceDecoder;
import org.geoserver.bxml.SequenceDecoder;
import org.geoserver.bxml.SetterDecoder;
import org.geoserver.bxml.base.SimpleDecoder;
import org.geoserver.bxml.base.StringDecoder;
import org.geoserver.gss.internal.atom.Atom;
import org.geoserver.gss.internal.atom.PersonImpl;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class PersonDecoder extends SimpleDecoder<PersonImpl> {

    public PersonDecoder(QName elemName) {
        super(elemName);
    }

    @Override
    public PersonImpl decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, elemName.getNamespaceURI(), elemName.getLocalPart());

        final PersonImpl person = new PersonImpl();
        ChoiceDecoder<Object> choice = new ChoiceDecoder<Object>();
        choice.addOption(new SetterDecoder<Object>(new StringDecoder(Atom.name), person, "name"));
        choice.addOption(new SetterDecoder<Object>(new StringDecoder(Atom.email), person, "email"));
        choice.addOption(new SetterDecoder<Object>(new StringDecoder(Atom.uri), person, "uri"));
        
        SequenceDecoder<Object> seq = new SequenceDecoder<Object>(1, 1);
        seq.add(choice, 0, Integer.MAX_VALUE);

        r.nextTag();
        Iterator<Object> decode = seq.decode(r);

        while (decode.hasNext()) {
            decode.next();
        }

        return person;
    }

}
