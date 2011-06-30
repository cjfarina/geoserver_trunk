package org.geoserver.gss.internal.atom;

import static org.geoserver.gss.internal.atom.Atom.author;
import static org.geoserver.gss.internal.atom.Atom.category;
import static org.geoserver.gss.internal.atom.Atom.content;
import static org.geoserver.gss.internal.atom.Atom.contributor;
import static org.geoserver.gss.internal.atom.Atom.entry;
import static org.geoserver.gss.internal.atom.Atom.id;
import static org.geoserver.gss.internal.atom.Atom.link;
import static org.geoserver.gss.internal.atom.Atom.summary;
import static org.geoserver.gss.internal.atom.Atom.title;
import static org.geoserver.gss.internal.atom.Atom.updated;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.gvsig.bxml.geoserver.Gml3Encoder;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Encodes an {@link EntryImpl} as {@code atom:entry} element.
 * <p>
 * {@link ContentEncoder#findEncoderFor(Object)} is used to locate an appropriate encoder depending
 * on the entry's {@link EntryImpl#getContent() content}
 * </p>
 * 
 * @see ContentEncoder
 * @see InsertElementTypeEncoder
 * @see UpdateElementTypeEncoder
 * @see DeleteElementTypeEncoder
 */
public class EntryEncoder extends AbstractEncoder {

    private final BxmlStreamWriter w;

    public EntryEncoder(BxmlStreamWriter w) {
        this.w = w;
    }

    public void encode(final EntryImpl e) throws IOException {
        startElement(w, entry);
        {
            element(w, title, false, e.getTitle());
            element(w, summary, false, e.getSummary(), true);
            element(w, id, false, e.getId());
            element(w, updated, false, e.getUpdated(), true);

            for (PersonImpl eauthor : e.getAuthor()) {
                final QName personElem = author;
                person(w, eauthor, personElem);
            }

            for (PersonImpl econtributor : e.getContributor()) {
                final QName personElem = contributor;
                person(w, econtributor, personElem);
            }

            for (CategoryImpl cat : e.getCategory()) {
                element(w, category, true, null, true, true, "term", cat.getTerm(), "scheme",
                        cat.getScheme());
            }

            for (LinkImpl elink : e.getLink()) {
                element(w, link, true, null, "rel", elink.getRel(), "href", elink.getHref());
            }

            final ContentImpl econtent = e.getContent();
            if (null != econtent) {
                Object value = econtent.getValue();
                ContentEncoder contentEncoder = ContentEncoder.findEncoderFor(value);
                startElement(w, content);
                {
                    contentEncoder.encode(w, value);
                }
                endElement(w);
            }

            Object georssWhere = e.getWhere();
            if (georssWhere != null) {
                Gml3Encoder gmlEncoder = getGmlEncoder();
                startElement(w, GeoRSS.where);
                {
                    if (georssWhere instanceof BoundingBox) {
                        gmlEncoder.encodeEnvelope(w, (BoundingBox) georssWhere);
                    } else if (georssWhere instanceof Geometry) {
                        CoordinateReferenceSystem crs = guessCRS((Geometry) georssWhere);
                        gmlEncoder.encodeGeometry(w, crs, (Geometry) georssWhere);
                    }
                }
                endElement(w);
            }
        }
        endElement(w);
    }
}
