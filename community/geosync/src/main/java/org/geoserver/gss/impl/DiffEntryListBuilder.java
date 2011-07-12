package org.geoserver.gss.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
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
import org.opengis.filter.sort.SortOrder;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

/**
 * Builds a feed result for the REPLICATIONFEED based on the given filtering criteria.
 * 
 * @author groldan
 * 
 */
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

    private SortOrder sortOrder;

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

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
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

        final Iterator<RevCommit> commits = geoGit.log().setTimeRange(commitRange).call();

        LinkedList<Iterator<DiffEntry>> iterators = new LinkedList<Iterator<DiffEntry>>();
        // needed for the top level feed metadata
        RevCommit newest = null;
        while (commits.hasNext()) {
            RevCommit commit = commits.next();
            if (newest == null) {
                newest = commit;
            }

            final ObjectId directParentId = commit.getParentIds().get(0);
            final ObjectId commitId = commit.getId();
            final Iterator<DiffEntry> commitChanges = geoGit.diff().setOldVersion(directParentId)
                    .setNewVersion(commitId).call();

            final SortOrder order = sortOrder == null ? SortOrder.ASCENDING : sortOrder;
            if (SortOrder.ASCENDING.equals(order)) {
                /*
                 * Use adFirst so that the changes get sorted oldest first
                 */
                iterators.addFirst(commitChanges);
            } else {
                iterators.addLast(commitChanges);
            }

        }

        @SuppressWarnings("unchecked")
        final Iterator<DiffEntry>[] array = iterators.toArray(new Iterator[iterators.size()]);
        final Iterator<DiffEntry> diffEntries = Iterators.concat(array);

        Function<DiffEntry, EntryImpl> diffToEntryFunction = new DiffToEntry(gss);

        Iterator<EntryImpl> entries = Iterators.transform(diffEntries, diffToEntryFunction);
        if (!Filter.INCLUDE.equals(filter)) {
            EntryFilter entryFilter = new EntryFilter(filter);
            entries = Iterators.filter(entries, entryFilter);
        }
        if (maxEntries != null) {
            entries = Iterators.limit(entries, maxEntries.intValue());
        }

        FeedImpl feed = buildFeed(newest, entries);

        return feed;
    }

    private FeedImpl buildFeed(final RevCommit newestCommit, final Iterator<EntryImpl> entries) {
        FeedImpl feed = new FeedImpl();
        feed.setStartPosition(startPosition);
        feed.setMaxEntries(maxEntries);

        if (newestCommit == null) {
            // request didn't match any commit, lets just inform the state of the feed with no
            // entries
            final Ref head = geoGit.getRepository().getRef(Ref.HEAD);
            if (head.getObjectId().isNull()) {
                // ah, so there's still not even a single commit in the whole database
                feed.setId(FeedImpl.NULL_ID);
                feed.setUpdated(new Date());
            }
        } else {
            feed.setId(newestCommit.getId().toString());
            feed.setUpdated(new Date(newestCommit.getTimestamp()));
        }

        feed.setEntry(entries);

        return feed;
    }

    private static final class EntryFilter implements Predicate<EntryImpl> {

        private final Filter filter;

        public EntryFilter(final Filter ogcFilter) {
            this.filter = ogcFilter;
        }

        @Override
        public boolean apply(final EntryImpl input) {
            boolean applies = filter.evaluate(input);
            return applies;
        }

    }

}
