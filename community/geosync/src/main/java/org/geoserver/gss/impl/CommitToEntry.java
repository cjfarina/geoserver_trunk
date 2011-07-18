package org.geoserver.gss.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.geogit.api.ObjectId;
import org.geogit.api.RevCommit;
import org.geoserver.gss.internal.atom.ContentImpl;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.geoserver.gss.internal.atom.PersonImpl;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.opengis.filter.FilterFactory2;

import com.google.common.base.Function;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Adapts {@link RevCommit} to an Atom {@link EntryImpl}.
 * 
 */
class CommitToEntry implements Function<RevCommit, EntryImpl> {

    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    private final GSS gss;

    /**
     * Set by {@link #content()}, to be used by {@link #where()}
     */
    private Object currGeorssWhereValue;

    public CommitToEntry(final GSS gss) {
        this.gss = gss;
    }

    @Override
    public EntryImpl apply(final RevCommit commit) {

        EntryImpl atomEntry = new EntryImpl();

        // NOTE: this is not really what the atom:entry should be, as if someone requested an entry
        // by it this wouldn't indicate whether it's a feature insert,update,or delete. But this is
        // a concept of GSS exclusively, as we can't use the commit id to refer to a single feature
        // change neither, so the mapping from entry id to DiffEntry should be in the GSS database,
        // and a new atom:entry id should be automatically generated as stated in the spec
        ObjectId objectId = commit.getId();

        atomEntry.setId(objectId.toString());// TODO: convert to UUID
        atomEntry.setTitle(title(commit));
        atomEntry.setSummary(commit.getMessage());
        atomEntry.setUpdated(new Date(commit.getTimestamp()));
        atomEntry.setAuthor(author(commit));
        atomEntry.setContributor(contributor(commit));
        atomEntry.setContent(content());
        atomEntry.setWhere(where());

        // atomEntry.setCategory(category);
        // atomEntry.setLink(link);
        // atomEntry.setPublished(published);
        // atomEntry.setRights(rights);
        // atomEntry.setSource(source);

        return atomEntry;
    }

    private Object where() {
        if (currGeorssWhereValue == null) {
            return null;
        }
        return currGeorssWhereValue;
    }

    private ContentImpl content() {
        return null;// TODO
    }

    /**
     * @param commit
     * @return committer
     */
    private List<PersonImpl> contributor(final RevCommit commit) {
        PersonImpl contributor = new PersonImpl();
        contributor.setName(commit.getCommitter());
        return Collections.singletonList(contributor);
    }

    /**
     * @param commit
     * @return commit author
     */
    private List<PersonImpl> author(final RevCommit commit) {
        PersonImpl author = new PersonImpl();
        author.setName(commit.getAuthor());
        return Collections.singletonList(author);
    }

    private String title(final RevCommit commit) {
        return commit.getMessage();
    }

}
