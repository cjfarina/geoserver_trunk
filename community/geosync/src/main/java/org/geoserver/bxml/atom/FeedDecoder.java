package org.geoserver.bxml.atom;

import static org.geoserver.gss.internal.atom.Atom.author;
import static org.geoserver.gss.internal.atom.Atom.category;
import static org.geoserver.gss.internal.atom.Atom.contributor;
import static org.geoserver.gss.internal.atom.Atom.entry;
import static org.geoserver.gss.internal.atom.Atom.feed;
import static org.geoserver.gss.internal.atom.Atom.generator;
import static org.geoserver.gss.internal.atom.Atom.icon;
import static org.geoserver.gss.internal.atom.Atom.id;
import static org.geoserver.gss.internal.atom.Atom.link;
import static org.geoserver.gss.internal.atom.Atom.rights;
import static org.geoserver.gss.internal.atom.Atom.subtitle;
import static org.geoserver.gss.internal.atom.Atom.title;
import static org.geoserver.gss.internal.atom.Atom.updated;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.geoserver.bxml.BxmlElementIterator;
import org.geoserver.gss.internal.atom.Atom;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.geoserver.gss.internal.atom.FeedImpl;
import org.geotools.util.logging.Logging;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.gvsig.bxml.stream.EventType;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;

public class FeedDecoder extends AbstractDecoder<FeedImpl> {

    protected final Logger LOGGER;

    public static QName startPosition = new QName("http://www.w3.org/2005/Atom", "startPosition");

    public static QName maxEntries = new QName("http://www.w3.org/2005/Atom", "maxEntries");

    private FeedBuilder builder;

    Function<BxmlStreamReader, EntryImpl> entryReaderFunction;

    public FeedDecoder() {
        super(feed);
        builder = new FeedBuilder();
        LOGGER = Logging.getLogger(getClass());

        entryReaderFunction = new Function<BxmlStreamReader, EntryImpl>() {

            @Override
            public EntryImpl apply(BxmlStreamReader input) {
                EntryImpl entry = null;
                try {
                    entry = new EntryDecoder().decode(input);
                } catch (Exception e) {
                    Throwables.propagate(e);
                }
                return entry;
            }
        };
    }

    @Override
    public FeedImpl decode(BxmlStreamReader r) throws Exception {
        EventType event;
        while ((event = r.next()) != EventType.END_DOCUMENT) {
            if (EventType.START_ELEMENT != event) {
                continue;
            }
            if (r.getAttributeCount() > 0) {
                decodeAttributtes(r, getAttributes(r));
            }
            decodeElement(r);
            QName name = r.getElementName();
            if (name.equals(entry)) {
                break;
            }
        }
        return buildResult();
    }

    @Override
    protected void decodeElement(BxmlStreamReader r) throws Exception {
        QName name = r.getElementName();

        if (id.equals(name)) {
            builder.setId(readStringValue(r, id));
        }

        if (title.equals(name)) {
            builder.setTitle(readStringValue(r, title));
        }

        if (subtitle.equals(name)) {
            builder.setSubtitle(readStringValue(r, subtitle));
        }

        if (icon.equals(name)) {
            builder.setIcon(readStringValue(r, icon));
        }

        if (rights.equals(name)) {
            builder.setRights(readStringValue(r, rights));
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

        if (generator.equals(name)) {
            GeneratorDecoder generatorDecoder = new GeneratorDecoder();
            builder.setGenerator(generatorDecoder.decode(r));
        }

        if (entry.equals(name)) {
            Iterator<BxmlStreamReader> entryElemIterator = new BxmlElementIterator(r, Atom.entry);

            Iterator<EntryImpl> entryIterator;
            entryIterator = Iterators.transform(entryElemIterator, entryReaderFunction);

            builder.setEntry(entryIterator);

        }

    }

    @Override
    protected void decodeAttributtes(BxmlStreamReader r, Map<QName, String> attributes)
            throws IOException {
        QName name = r.getElementName();
        if (feed.equals(name)) {
            if (attributes.get(startPosition) != null) {
                builder.setStartPosition(parseLongValue(attributes.get(startPosition),
                        startPosition.getLocalPart()));
            }
            if (attributes.get(maxEntries) != null) {
                builder.setMaxEntries(parseLongValue(attributes.get(maxEntries),
                        maxEntries.getLocalPart()));
            }
        }
    }

    @Override
    protected FeedImpl buildResult() {
        return builder.build();
    }

}
