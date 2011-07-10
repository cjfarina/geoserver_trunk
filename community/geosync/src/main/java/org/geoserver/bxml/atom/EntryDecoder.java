package org.geoserver.bxml.atom;

import static org.geoserver.gss.internal.atom.Atom.author;
import static org.geoserver.gss.internal.atom.Atom.category;
import static org.geoserver.gss.internal.atom.Atom.content;
import static org.geoserver.gss.internal.atom.Atom.contributor;
import static org.geoserver.gss.internal.atom.Atom.id;
import static org.geoserver.gss.internal.atom.Atom.link;
import static org.geoserver.gss.internal.atom.Atom.published;
import static org.geoserver.gss.internal.atom.Atom.rights;
import static org.geoserver.gss.internal.atom.Atom.source;
import static org.geoserver.gss.internal.atom.Atom.summary;
import static org.geoserver.gss.internal.atom.Atom.title;
import static org.geoserver.gss.internal.atom.Atom.updated;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.geoserver.gss.internal.atom.Atom;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class EntryDecoder extends AbstractDecoder<EntryImpl> {

    private EntryBuilder builder;

    public EntryDecoder() {
        super(Atom.entry);
        builder = new EntryBuilder();
    }

    @Override
    protected void decodeElement(BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if (title.equals(name)) {
            builder.setTitle(readStringValue(r, title));
        }

        if (summary.equals(name)) {
            builder.setSummary(readStringValue(r, summary));
        }

        if (id.equals(name)) {
            builder.setId(readStringValue(r, id));
        }

        if (updated.equals(name)) {
            builder.setUpdated(readDateValue(r, updated));
        }

        if (author.equals(name)) {
            PersonDecoder personDecoder = new PersonDecoder(author);
            builder.addAuthor(personDecoder.decode(r));
        }

        if (contributor.equals(name)) {
            PersonDecoder personDecoder = new PersonDecoder(contributor);
            builder.addContributor(personDecoder.decode(r));
        }

        if (category.equals(name)) {
            CategoryDecoder categoryDecoder = new CategoryDecoder();
            builder.addCategory(categoryDecoder.decode(r));
        }

        if (link.equals(name)) {
            LinkDecoder linkDecoder = new LinkDecoder();
            builder.addLink(linkDecoder.decode(r));
        }

        if (published.equals(name)) {
            builder.setPublished(readDateValue(r, published));
        }

        if (rights.equals(name)) {
            builder.setRights(readStringValue(r, rights));
        }

        if (source.equals(name)) {
            builder.setSource(readStringValue(r, source));
        }

        if (content.equals(name)) {
            ContentDecoder contentDecoder = new ContentDecoder();
            builder.setContent(contentDecoder.decode(r));
        }

    }

    @Override
    protected EntryImpl buildResult() {
        return builder.build();
    }

}
