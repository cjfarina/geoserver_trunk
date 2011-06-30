package org.geoserver.gss.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.geogit.api.DiffEntry;
import org.geogit.api.GeoGIT;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevCommit;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.geoserver.gss.internal.atom.FeedImpl;
import org.geotools.util.Range;
import org.opengis.filter.Filter;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

class DiffEntryListBuilder {

    private final GeoGIT geoGit;

    private List<String> searchTerms;

    /**
     * A Filter against the {@link EntryImpl} construct
     */
    private Filter filter;

    private Long startPosition;

    private Long maxEntries;

    private final GSS gss;

    public DiffEntryListBuilder(GSS gss, GeoGIT geoGit) {
        this.gss = gss;
        this.geoGit = geoGit;
    }

    public void setSearchTerms(List<String> searchTerms) {
        this.searchTerms = searchTerms;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public void setStartPosition(Long startPosition) {
        this.startPosition = startPosition;
    }

    public void setMaxEntries(Long maxEntries) {
        this.maxEntries = maxEntries;
    }

    /**
     * Performs a ggit log operation that satisfies the time constraints and with the obtained list
     * of commits, geenerates a feed with an entry per feature change in every commit.
     * <p>
     * The non time related constraints are evaluated in-process by the resulting list of entries.
     * </p>
     * 
     * @return
     * @throws Exception
     */
    public FeedImpl buildFeed() throws Exception {
        final Filter filter = this.filter == null ? Filter.INCLUDE : this.filter;
        final TimeConstraintExtractor timeConstraintExtractor = new TimeConstraintExtractor();
        filter.accept(timeConstraintExtractor, null);
        final Range<Date> commitRange = timeConstraintExtractor.getValidTimeWindow();

        final Iterable<RevCommit> commits = geoGit.log().setAscending(true)
                .setCommitRangeTime(commitRange).call();

        List<Iterator<DiffEntry>> iterators = new ArrayList<Iterator<DiffEntry>>();
        // needed for the top level feed metadata
        RevCommit newest = null;
        for (RevCommit commit : commits) {
            if (newest == null) {
                newest = commit;
            } else {
                long timestamp = commit.getTimestamp();
                if (timestamp > newest.getTimestamp()) {
                    newest = commit;
                }
            }

            final ObjectId directParentId = commit.getParentIds().get(0);
            final ObjectId commitId = commit.getId();
            final Iterator<DiffEntry> commitChanges = geoGit.diff().setOldVersion(directParentId)
                    .setNewVersion(commitId).call();

            // List debuglist = new ArrayList();
            // for (DiffEntry e : commitChanges) {
            // debuglist.add(e);
            // }

            iterators.add(commitChanges);

        }

        Iterator<DiffEntry> diffEntries = Iterators.concat(iterators.toArray(new Iterator[iterators
                .size()]));
        if (maxEntries != null) {
            diffEntries = Iterators.limit(diffEntries, maxEntries.intValue());
        }

        Function<DiffEntry, EntryImpl> diffToEntryFunction = new DiffToEntry(gss);

        Iterator<EntryImpl> entries = Iterators.transform(diffEntries, diffToEntryFunction);

        FeedImpl feed = buildFeedImpl(newest);
        feed.setEntry(entries);

        return feed;
    }

    private FeedImpl buildFeedImpl(RevCommit newest) {
        FeedImpl feed = new FeedImpl();
        feed.setStartPosition(startPosition);
        feed.setMaxEntries(maxEntries);

        if (newest == null) {
            // request didn't match any commit, lets just inform the state of the feed with no
            // entries
            final Ref head = geoGit.getRepository().getRef(Ref.HEAD);
            if (head.getObjectId().isNull()) {
                // ah, so there's still not even a single commit in the whole database
                feed.setId(FeedImpl.NULL_ID);
                feed.setUpdated(new Date());
            }
        } else {
            feed.setId(newest.getId().toString());
            feed.setUpdated(new Date(newest.getTimestamp()));
        }
        return feed;
    }
}
