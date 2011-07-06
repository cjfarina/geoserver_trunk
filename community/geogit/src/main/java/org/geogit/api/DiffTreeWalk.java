package org.geogit.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.geogit.api.DiffEntry.ChangeType;
import org.geogit.api.RevObject.TYPE;
import org.geogit.repository.DepthSearch;
import org.geogit.repository.Repository;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

class DiffTreeWalk {

    private final Repository repo;

    private final ObjectId fromCommit;

    private final ObjectId toCommit;

    private List<String> basePath;

    public DiffTreeWalk(final Repository repo, final ObjectId fromCommit, final ObjectId toCommit) {
        this.repo = repo;
        this.fromCommit = fromCommit;
        this.toCommit = toCommit;
        this.basePath = Collections.emptyList();
    }

    public void setFilter(final String... path) {
        if (path == null || path.length == 0) {
            this.basePath = Collections.emptyList();
        } else {
            this.basePath = Arrays.asList(path);
        }
    }

    @SuppressWarnings("unchecked")
    public Iterator<DiffEntry> get() {
        if (fromCommit.equals(toCommit)) {
            return Collections.EMPTY_LIST.iterator();
        }

        final Ref oldObject = getFilteredObject(fromCommit);
        final Ref newObject = getFilteredObject(toCommit);
        if (oldObject == null && newObject == null) {
            // filter didn't match anything
            return Collections.EMPTY_LIST.iterator();
        }

        // easy, filter addressed a single blob
        if ((oldObject != null && oldObject.getType() == TYPE.BLOB)
                || (newObject != null && newObject.getType() == TYPE.BLOB)) {

            DiffEntry entry = DiffEntry.newInstance(fromCommit, toCommit, oldObject, newObject,
                    basePath);
            return Collections.singleton(entry).iterator();
        }

        // filter addressed a tree...
        final ObjectId oldTreeId = oldObject == null ? ObjectId.NULL : oldObject.getObjectId();
        final ObjectId newTreeId = newObject == null ? ObjectId.NULL : newObject.getObjectId();
        final RevTree oldTree = repo.getTree(oldTreeId);
        final RevTree newTree = repo.getTree(newTreeId);
        Assert.isTrue(oldTree.isNormalized());
        Assert.isTrue(newTree.isNormalized());

        return new TreeDiffEntryIterator(basePath, fromCommit, toCommit, oldTree, newTree);

    }

    /**
     * @param commitId
     */
    private Ref getFilteredObject(final ObjectId commitId) {
        if (commitId.isNull()) {
            return new Ref("", ObjectId.NULL, TYPE.TREE);
        }
        final RevCommit commit = repo.getCommit(commitId);
        final ObjectId treeId = commit.getTreeId();
        if (basePath.isEmpty()) {
            return new Ref("", treeId, TYPE.TREE);
        }

        final DepthSearch search = new DepthSearch(repo);
        Ref ref = search.find(treeId, basePath);
        return ref;
    }

    /**
     * @author groldan
     * 
     */
    private static abstract class AbstractDiffIterator implements Iterator<DiffEntry> {

        protected final List<String> basePath;

        public AbstractDiffIterator(final List<String> basePath) {
            this.basePath = basePath;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        protected List<String> path(final String name) {
            List<String> path = new ArrayList<String>(this.basePath.size() + 1);
            path.addAll(basePath);
            path.add(name);
            return path;
        }

    }

    /**
     * 
     * @author groldan
     */
    private class AddRemoveAllTreeIterator extends AbstractDiffIterator {

        private final ObjectId oldCommit;

        private final ObjectId newCommit;

        private Iterator<?> treeIterator;

        private final DiffEntry.ChangeType changeType;

        public AddRemoveAllTreeIterator(final DiffEntry.ChangeType changeType,
                final List<String> basePath, final ObjectId fromCommit, final ObjectId toCommit,
                final RevTree tree) {
            this(changeType, basePath, fromCommit, toCommit, tree.iterator(null));
        }

        public AddRemoveAllTreeIterator(final DiffEntry.ChangeType changeType,
                final List<String> basePath, final ObjectId fromCommit, final ObjectId toCommit,
                final Iterator<Ref> treeIterator) {
            super(basePath);
            oldCommit = fromCommit;
            newCommit = toCommit;
            this.treeIterator = treeIterator;
            this.changeType = changeType;
        }

        private Object cachedNext;

        /**
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            if (cachedNext != null) {
                return true;
            }
            if (!treeIterator.hasNext()) {
                return false;
            }

            Object nextObj = treeIterator.next();
            if (nextObj instanceof Ref) {
                Ref next = (Ref) nextObj;
                if (TYPE.TREE.equals(next.getType())) {
                    RevTree tree = repo.getTree(next.getObjectId());
                    Predicate<Ref> filter = null;// TODO: propagate filter
                    List<String> childPath = path(next.getName());
                    Iterator<?> childTreeIterator;
                    childTreeIterator = new AddRemoveAllTreeIterator(changeType, childPath,
                            oldCommit, newCommit, tree);
                    this.treeIterator = Iterators.concat(childTreeIterator, this.treeIterator);
                    return hasNext();
                }
            }
            cachedNext = nextObj;
            return true;
        }

        /**
         * @see java.util.Iterator#next()
         */
        public DiffEntry next() {
            if (hasNext()) {
                Object currObj = cachedNext;
                cachedNext = null;
                if (currObj instanceof DiffEntry) {
                    return (DiffEntry) currObj;
                } else {
                    final Ref curr = (Ref) currObj;
                    Ref oldObject = null;
                    Ref newObject = null;
                    String name = curr.getName();
                    if (changeType == ChangeType.ADD) {
                        newObject = curr;
                    } else {
                        oldObject = curr;
                    }
                    List<String> path = path(name);
                    return DiffEntry.newInstance(oldCommit, newCommit, oldObject, newObject, path);
                }
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Iterator over the differences between two trees
     * 
     * @author groldan
     * 
     */
    private class TreeDiffEntryIterator extends AbstractDiffIterator {

        private final ObjectId oldCommit;

        private final ObjectId newCommit;

        private final RevTree oldTree;

        private final RevTree newTree;

        private Iterator<DiffEntry> currSubTree;

        private Iterator<Ref> oldEntries;

        private Iterator<Ref> newEntries;

        public TreeDiffEntryIterator(final List<String> basePath, final ObjectId fromCommit,
                final ObjectId toCommit, final RevTree fromTree, final RevTree toTree) {
            super(basePath);
            oldCommit = fromCommit;
            newCommit = toCommit;
            oldTree = fromTree;
            newTree = toTree;
            oldEntries = oldTree.iterator(null);
            newEntries = newTree.iterator(null);
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            if (currSubTree != null && currSubTree.hasNext()) {
                return true;
            }
            if (!oldEntries.hasNext() && !newEntries.hasNext()) {
                return false;
            }
            if (oldEntries.hasNext() && !newEntries.hasNext()) {
                currSubTree = new AddRemoveAllTreeIterator(ChangeType.DELETE, super.basePath,
                        oldCommit, newCommit, oldEntries);
                return true;
            }
            if (!oldEntries.hasNext() && newEntries.hasNext()) {
                currSubTree = new AddRemoveAllTreeIterator(ChangeType.ADD, super.basePath,
                        oldCommit, newCommit, newEntries);
                return true;
            }

            // ok, both have next
            Ref oldEntry;
            Ref newEntry;
            while (oldEntries.hasNext() && newEntries.hasNext()) {
                oldEntry = oldEntries.next();
                newEntry = newEntries.next();
                final String oldEntryName = oldEntry.getName();
                final String newEntryName = newEntry.getName();

                if (oldEntryName.equals(newEntryName)) {
                    if (oldEntry.getObjectId().equals(newEntry.getObjectId())) {
                        // same object, skip it
                        continue;
                    } else {
                        // found single content modification, could be a blob or a tree
                        if (oldEntry.getType().equals(RevObject.TYPE.TREE)) {
                            Assert.isTrue(newEntry.getType().equals(RevObject.TYPE.TREE));
                            final String name = oldEntryName;
                            List<String> childTreePath = path(name);
                            RevTree oldChildTree = repo.getTree(oldEntry.getObjectId());
                            RevTree newChildTree = repo.getTree(newEntry.getObjectId());
                            this.currSubTree = new TreeDiffEntryIterator(childTreePath, oldCommit,
                                    newCommit, oldChildTree, newChildTree);
                        } else {
                            // single feature modification
                            Assert.isTrue(oldEntry.getType().equals(RevObject.TYPE.BLOB));
                            Assert.isTrue(newEntry.getType().equals(RevObject.TYPE.BLOB));
                            List<String> path = path(oldEntryName);
                            DiffEntry next = DiffEntry.newInstance(oldCommit, newCommit, oldEntry,
                                    newEntry, path);
                            currSubTree = Collections.singleton(next).iterator();
                        }
                    }
                } else {
                    // not the same feature/tree, find out whether it's an addition or a
                    // deletion
                    final int comparison = ObjectId.forString(oldEntryName).compareTo(
                            ObjectId.forString(newEntryName));
                    Assert.isTrue(comparison != 0, "Comparison can't be 0 if reached this point!");

                    if (comparison < 0) {
                        if (oldEntry.getType().equals(RevObject.TYPE.TREE)) {
                            final String name = oldEntryName;
                            List<String> childTreePath = path(name);
                            RevTree oldChildTree = repo.getTree(oldEntry.getObjectId());
                            ChangeType changeType = ChangeType.DELETE;
                            this.currSubTree = new AddRemoveAllTreeIterator(changeType,
                                    childTreePath, oldCommit, newCommit, oldChildTree);
                        } else {
                            Assert.isTrue(oldEntry.getType().equals(RevObject.TYPE.BLOB));
                            List<String> path = path(oldEntryName);
                            DiffEntry next = DiffEntry.newInstance(oldCommit, newCommit, oldEntry,
                                    null, path);
                            currSubTree = Collections.singleton(next).iterator();
                        }
                        // need to "return" newEntry for the next iteration
                        this.newEntries = Iterators.concat(Collections.singleton(newEntry)
                                .iterator(), this.newEntries);
                    } else {
                        if (newEntry.getType().equals(RevObject.TYPE.TREE)) {
                            final String name = newEntryName;
                            List<String> childTreePath = path(name);
                            RevTree newChildTree = repo.getTree(newEntry.getObjectId());
                            ChangeType changeType = ChangeType.ADD;
                            this.currSubTree = new AddRemoveAllTreeIterator(changeType,
                                    childTreePath, oldCommit, newCommit, newChildTree);
                        } else {
                            Assert.isTrue(newEntry.getType().equals(RevObject.TYPE.BLOB));
                            List<String> path = path(newEntryName);
                            DiffEntry next = DiffEntry.newInstance(oldCommit, newCommit, null,
                                    newEntry, path);
                            currSubTree = Collections.singleton(next).iterator();
                        }
                        // need to "return" oldEntry for the next iteration
                        this.oldEntries = Iterators.concat(Collections.singleton(oldEntry)
                                .iterator(), this.oldEntries);
                    }
                }
            }

            return hasNext();
        }

        /**
         * @see java.util.Iterator#next()
         */
        public DiffEntry next() {
            return currSubTree.next();
        }
    }

    private static class RefPairToDiffEntry implements Function<Ref[], DiffEntry> {

        private final ObjectId oldCommit;

        private final ObjectId newCommit;

        private final List<String> parentPath;

        public RefPairToDiffEntry(final List<String> parentPath, final ObjectId oldCommit,
                final ObjectId newCommit) {
            this.parentPath = Collections.unmodifiableList(parentPath);
            this.oldCommit = oldCommit;
            this.newCommit = newCommit;
        }

        /**
         * @see com.google.common.base.Function#apply(java.lang.Object)
         */
        public DiffEntry apply(Ref[] input) {
            final Ref oldObject = input[0];
            final Ref newObject = input[1];
            List<String> path = new ArrayList<String>(parentPath);
            String refName = oldObject == null ? newObject.getName() : oldObject.getName();
            path.add(refName);

            DiffEntry entry = DiffEntry.newInstance(oldCommit, newCommit, oldObject, newObject,
                    path);
            return entry;
        }
    }
}
