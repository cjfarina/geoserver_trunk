package org.geoserver.gss.internal.atom.decoders;

import static org.geoserver.gss.internal.atom.Atom.id;
import static org.geoserver.gss.internal.atom.Atom.summary;
import static org.geoserver.gss.internal.atom.Atom.title;
import static org.geoserver.gss.internal.atom.Atom.updated;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.Atom;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.geoserver.gss.internal.atom.builders.EntryBuilder;
import org.gvsig.bxml.stream.BxmlStreamReader;

public class EntryDecoder extends AbstractDecoder<EntryImpl> {

    private EntryBuilder builder;

    public EntryDecoder() {
        super(Atom.entry);
        builder = new EntryBuilder();
    }

    @Override
    protected void decodeElement(BxmlStreamReader r) throws IOException {
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

    }

    @Override
    protected EntryImpl buildResult() {
        return builder.build();
    }

}
