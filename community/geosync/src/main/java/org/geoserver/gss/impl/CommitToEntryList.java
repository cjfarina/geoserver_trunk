package org.geoserver.gss.impl;

import java.util.Iterator;
import java.util.Set;

import org.geogit.api.RevCommit;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.geotools.filter.expression.PropertyAccessor;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;

/**
 * Iterable that lazily adapts source {@link RevCommit} instances to Atim {@link EntryImpl}
 * instances.
 * 
 */
class CommitToEntryList implements Iterable<EntryImpl> {

    private final Iterator<RevCommit> commits;

    private final GSS gss;

    private final long startPosition;

    private final long maxEntries;

    private final Filter filter;

    public CommitToEntryList(final GSS gss, final Iterator<RevCommit> commits,
            final Filter entryFilter, final Long startPosition, final Long maxEntries) {
        this.gss = gss;
        this.commits = commits;
        this.filter = entryFilter;
        this.startPosition = startPosition == null ? 1 : startPosition.longValue();
        this.maxEntries = maxEntries == null ? Long.MAX_VALUE : maxEntries.longValue();
    }

    /**
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<EntryImpl> iterator() {
        Iterator<EntryImpl> targetIterator = new CommitToEntryIterator(gss, commits, filter,
                startPosition, maxEntries);
        return targetIterator;
    }

    /**
     * @author groldan
     * 
     */
    private static class CommitToEntryIterator implements Iterator<EntryImpl> {

        private final Iterator<RevCommit> sourceIterator;

        private final GSS gss;

        private final long maxPosition;

        private long currPosition;

        private final Filter filter;

        public CommitToEntryIterator(final GSS gss, Iterator<RevCommit> sourceIterator,
                final Filter filter, final long startPosition, final long maxEntries) {
            this.gss = gss;
            this.sourceIterator = sourceIterator;
            this.filter = filter;
            this.maxPosition = (Long.MAX_VALUE - startPosition) > maxEntries ? (startPosition + maxEntries)
                    : Long.MAX_VALUE;

            currPosition = 1;// lower index is 1, not 0
            while (currPosition < startPosition && sourceIterator.hasNext()) {
                sourceIterator.next();
                currPosition++;
            }
        }

        private EntryImpl next;

        /**
         * TODO: filter won't work without some POJO {@link PropertyAccessor}
         * 
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            if (!sourceIterator.hasNext() || currPosition == maxPosition) {
                return false;
            }
            if (next != null) {
                return true;
            }
            while (sourceIterator.hasNext()) {
                RevCommit commit = sourceIterator.next();
                CommitToEntry commitToEntry = new CommitToEntry(gss, commit);
                EntryImpl entry = commitToEntry.get();

                // HACK!!! shouldn't need to hack id filter like this but Filter doesn't know how to
                // evaluate POJOs out of the box
                if (filter instanceof Id) {
                    Set<Object> ids = ((Id) filter).getIDs();
                    if (ids.contains(entry.getId())) {
                        next = entry;
                        return true;
                    }
                } else if (filter.evaluate(entry)) {
                    next = entry;
                    return true;
                }
            }
            return false;
        }

        /**
         * @see java.util.Iterator#next()
         */
        public EntryImpl next() {
            EntryImpl curr = next;
            currPosition++;
            next = null;
            return curr;
        }

        /**
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
}
