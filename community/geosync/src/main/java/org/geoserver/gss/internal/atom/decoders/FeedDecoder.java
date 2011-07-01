package org.geoserver.gss.internal.atom.decoders;

import static org.geoserver.gss.internal.atom.Atom.author;
import static org.geoserver.gss.internal.atom.Atom.category;
import static org.geoserver.gss.internal.atom.Atom.contributor;
import static org.geoserver.gss.internal.atom.Atom.entry;
import static org.geoserver.gss.internal.atom.Atom.feed;
import static org.geoserver.gss.internal.atom.Atom.id;
import static org.geoserver.gss.internal.atom.Atom.maxEntries;
import static org.geoserver.gss.internal.atom.Atom.startPosition;
import static org.geoserver.gss.internal.atom.Atom.subtitle;
import static org.geoserver.gss.internal.atom.Atom.title;
import static org.geoserver.gss.internal.atom.Atom.updated;
import static org.geoserver.gss.internal.atom.Atom.generator;
import static org.geoserver.gss.internal.atom.Atom.icon;
import static org.geoserver.gss.internal.atom.Atom.rights;
import static org.geoserver.gss.internal.atom.Atom.link;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.FeedImpl;
import org.geoserver.gss.internal.atom.builders.FeedBuilder;
import org.geotools.util.logging.Logging;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class FeedDecoder extends AbstractDecoder<FeedImpl> {

    protected final Logger LOGGER;

    private final EntryDecoder entryDecoder;

    private FeedBuilder builder;

    private PersonDecoder personDecoder = new PersonDecoder();

    private CategoryDecoder categoryDecoder = new CategoryDecoder();

    private GeneratorDecoder generatorDecoder = new GeneratorDecoder();

    private LinkDecoder linkDecoder = new LinkDecoder();

    public FeedDecoder() {
        entryDecoder = new EntryDecoder();
        LOGGER = Logging.getLogger(getClass());
    }

    @Override
    public void setupInitialData() {
        builder = new FeedBuilder();
    }

    @Override
    public void decodeElement(BxmlStreamReader r) throws IOException {
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
            builder.addAuthor(personDecoder.decodeElement(r, author));
        }

        if (contributor.equals(name)) {
            builder.addContributor(personDecoder.decodeElement(r, contributor));
        }

        if (category.equals(name)) {
            builder.addCategory(categoryDecoder.decodeElement(r, category));
        }

        if (link.equals(name)) {
            builder.addLink(linkDecoder.decodeElement(r, link));
        }

        if (generator.equals(name)) {
            builder.setGenerator(generatorDecoder.decodeElement(r, generator));
        }

        if (entry.equals(name)) {
            builder.addEntry(entryDecoder.decodeElement(r, entry));
        }

    }

    @Override
    public void decodeAttributtes(BxmlStreamReader r, Map<QName, String> attributes)
            throws IOException {
        QName name = r.getElementName();
        if(feed.equals(name)){
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
    public FeedImpl buildResult() {
        return builder.build();
    }

}
