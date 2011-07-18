package org.geoserver.gss.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geoserver.gss.internal.atom.CategoryImpl;
import org.geoserver.gss.internal.atom.ContentImpl;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.geoserver.gss.internal.atom.PersonImpl;
import org.opengis.filter.Filter;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

/**
 * Applies {@link Filter}, {@code SEARCHTERMS}, {@code STARTPOSITION}, and {@code MAXENTRIES}
 * filtering to an {@code Iterator<EntryImpl>}.
 * 
 * @author groldan
 * @see CommitsEntryListBuilder
 * @see DiffEntryListBuilder
 */
class FilteringEntryListBuilder {

    private final Filter filter;

    private final List<String> searchTerms;

    private final Long startPosition;

    private final Long maxEntries;

    public FilteringEntryListBuilder(final Filter filter, final List<String> searchTerms,
            final Long startPosition, final Long maxEntries) {
        this.filter = filter;
        this.searchTerms = searchTerms;
        this.startPosition = startPosition;
        this.maxEntries = maxEntries;
    }

    public Iterator<EntryImpl> filter(Iterator<EntryImpl> entries) {

        if (!Filter.INCLUDE.equals(filter)) {
            EntryFilter entryFilter = new EntryFilter(filter);
            entries = Iterators.filter(entries, entryFilter);
        }

        if (searchTerms != null && searchTerms.size() > 0) {
            entries = Iterators.filter(entries, new SearchTermsPredicate(searchTerms));
        }

        if (startPosition != null && startPosition.intValue() > 1) {
            final int numberToSkip = startPosition.intValue() - 1;
            final int skipped = Iterators.skip(entries, numberToSkip);
            if (skipped < numberToSkip) {
                entries = Iterators.emptyIterator();
            }
        }

        if (maxEntries != null) {
            entries = Iterators.limit(entries, maxEntries.intValue());
        }
        return entries;
    }

    /**
     * Adapts a {@link Filter} to a {@link Predicate} against an {@link EntryImpl}.
     * 
     * @author groldan
     * 
     */
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

    /**
     * {@link Predicate} to evaluate whether an {@link EntryImpl} contains any of the given search
     * terms, as per the GetEntries SEARCHTERMS parameter.
     * 
     * @author groldan
     * 
     */
    private static class SearchTermsPredicate implements Predicate<EntryImpl> {

        private final List<String> searchTerms;

        public SearchTermsPredicate(final List<String> searchTerms) {
            this.searchTerms = new ArrayList<String>(searchTerms.size());
            for (String s : searchTerms) {
                if (s != null && s.trim().length() > 0) {
                    this.searchTerms.add(s.toUpperCase());
                }
            }
        }

        @Override
        public boolean apply(final EntryImpl e) {
            if (searchTerms.size() == 0) {
                return true;
            }

            boolean applies = applies(e.getTitle()) || applies(e.getSummary())
                    || appliesPerson(e.getAuthor()) || appliesCategory(e.getCategory())
                    || appliesPerson(e.getContributor()) || applies(e.getRights())
                    || appliesContent(e.getContent());

            return applies;
        }

        private boolean appliesContent(final ContentImpl content) {
            if (content == null) {
                return false;
            }
            if (applies(content.getSrc()) || applies(content.getType())) {
                return true;
            }
            return false;
        }

        private boolean appliesCategory(final List<CategoryImpl> categories) {
            if (categories.size() == 0) {
                return searchTerms.size() == 0;
            }
            for (CategoryImpl c : categories) {
                if (applies(c.getTerm()) || applies(c.getScheme())) {
                    return true;
                }
            }
            return false;
        }

        private boolean appliesPerson(final List<PersonImpl> persons) {
            if (persons.size() == 0) {
                return searchTerms.size() == 0;
            }
            for (PersonImpl p : persons) {
                if (applies(p.getName()) || applies(p.getEmail()) || applies(p.getUri())) {
                    return true;
                }
            }
            return false;
        }

        private boolean applies(final String s) {
            if (s == null || s.length() == 0) {
                return searchTerms.size() == 0;
            }
            for (int i = 0; i < searchTerms.size(); i++) {
                if (s.toUpperCase().contains(searchTerms.get(i))) {
                    return true;
                }
            }
            return false;
        }
    }

}
