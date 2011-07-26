package org.geoserver.bxml.atom;

import static org.geoserver.gss.internal.atom.Atom.author;
import static org.geoserver.gss.internal.atom.Atom.contributor;
import static org.geoserver.gss.internal.atom.Atom.entry;
import static org.geoserver.gss.internal.atom.Atom.id;
import static org.geoserver.gss.internal.atom.Atom.summary;
import static org.geoserver.gss.internal.atom.Atom.title;
import static org.geoserver.gss.internal.atom.Atom.updated;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geoserver.bxml.ChoiceDecoder;
import org.geoserver.bxml.Decoder;
import org.geoserver.bxml.SequenceDecoder;
import org.geoserver.bxml.SetterDecoder;
import org.geoserver.bxml.base.DateDecoder;
import org.geoserver.bxml.base.StringDecoder;
import org.geoserver.gss.internal.atom.Atom;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

public class EntryDecoder implements Decoder<EntryImpl> {

    @Override
    public EntryImpl decode(BxmlStreamReader r) throws Exception {
        r.require(EventType.START_ELEMENT, entry.getNamespaceURI(), entry.getLocalPart());

        final EntryImpl entry = new EntryImpl();
        ChoiceDecoder<Object> choice = new ChoiceDecoder<Object>();
        choice.addOption(new SetterDecoder<Object>(new StringDecoder(title), entry, "title"));
        choice.addOption(new SetterDecoder<Object>(new StringDecoder(summary), entry, "summary"));
        choice.addOption(new SetterDecoder<Object>(new StringDecoder(id), entry, "id"));
        choice.addOption(new SetterDecoder<Object>(new DateDecoder(updated), entry, "updated"));
        choice.addOption(new SetterDecoder<Object>(new PersonDecoder(author), entry, "author"));
        choice.addOption(new SetterDecoder<Object>(new PersonDecoder(contributor), entry,
                "contributor"));

        SequenceDecoder<Object> seq = new SequenceDecoder<Object>(1, 1);
        seq.add(choice, 0, Integer.MAX_VALUE);

        r.nextTag();
        Iterator<Object> decode = seq.decode(r);
        // consume and let functors do their job
        while (decode.hasNext()) {
            decode.next();
        }

        return entry;

        // if (updated.equals(name)) {
        // builder.setUpdated(readDateValue(r, updated));
        // }
        //
        // if (author.equals(name)) {
        // PersonDecoder personDecoder = new PersonDecoder(author);
        // builder.addAuthor(personDecoder.decode(r));
        // }
        //
        // if (contributor.equals(name)) {
        // PersonDecoder personDecoder = new PersonDecoder(contributor);
        // builder.addContributor(personDecoder.decode(r));
        // }
        //
        // if (category.equals(name)) {
        // CategoryDecoder categoryDecoder = new CategoryDecoder();
        // builder.addCategory(categoryDecoder.decode(r));
        // }
        //
        // if (link.equals(name)) {
        // LinkDecoder linkDecoder = new LinkDecoder();
        // builder.addLink(linkDecoder.decode(r));
        // }
        //
        // if (published.equals(name)) {
        // builder.setPublished(readDateValue(r, published));
        // }
        //
        // if (rights.equals(name)) {
        // builder.setRights(readStringValue(r, rights));
        // }
        //
        // if (source.equals(name)) {
        // builder.setSource(readStringValue(r, source));
        // }
        //
        // if (content.equals(name)) {
        // ContentDecoder contentDecoder = new ContentDecoder();
        // builder.setContent(contentDecoder.decode(r));
        // }
        //
        // if (where.equals(name)) {
        // builder.setWhere(new WhereDecoder().decode(r));
        // }

    }

    @Override
    public boolean canHandle(QName name) {
        return Atom.entry.equals(name);
    }

    @Override
    public Set<QName> getTargets() {
        return Collections.singleton(Atom.entry);
    }

}
