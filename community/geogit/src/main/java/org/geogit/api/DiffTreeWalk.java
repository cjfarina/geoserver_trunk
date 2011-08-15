package org.geogit.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geogit.api.DiffEntry.ChangeType;
import org.geogit.api.RevObject.TYPE;
import org.geogit.repository.DepthSearch;
import org.geogit.repository.Repository;
import org.springframework.util.Assert;

import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;

class DiffTreeWalk {

    private final Repository repo;

    private final ObjectId fromCommit;

    private final ObjectId toCommit;

    private List<String> basePath;

    public DiffTreeWalk(final Repository repo, final ObjectId fromCommit, final ObjectId toCommit) {
        Assert.notNull(repo);
        Assert.notNull(fromCommit);
        Assert.notNull(toCommit);

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

            // but addressed object didn't change
            if (oldObject != null && newObject != null && oldObject.equals(newObject)) {
                return Iterators.emptyIterator();
            }
            // ok, found change between new and old version of the filter addressed object
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

        return new TreeDiffEntryIterator(basePath, fromCommit, toCommit, oldTree, newTree, repo);

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
    private static abstract class AbstractDiffIterator extends AbstractIterator<DiffEntry> {

        protected final List<String> basePath;

        public AbstractDiffIterator(final List<String> basePath) {
            this.basePath = Collections.unmodifiableList(basePath);
        }

        protected List<String> childPath(final String name) {
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
    private static class AddRemoveAllTreeIterator extends AbstractDiffIterator {

        private final ObjectId oldCommit;

        private final ObjectId newCommit;

        private Iterator<?> treeIterator;

        private final DiffEntry.ChangeType changeType;

        private final Repository repo;

        public AddRemoveAllTreeIterator(final DiffEntry.ChangeType changeType,
                final List<String> basePath, final ObjectId fromCommit, final ObjectId toCommit,
                final RevTree tree, final Repository repo) {
            this(changeType, basePath, fromCommit, toCommit, tree.iterator(null), repo);
        }

        public AddRemoveAllTreeIterator(final DiffEntry.ChangeType changeType,
                final List<String> basePath, final ObjectId fromCommit, final ObjectId toCommit,
                final Iterator<Ref> treeIterator, final Repository repo) {
            super(basePath);
            this.oldCommit = fromCommit;
            this.newCommit = toCommit;
            this.treeIterator = treeIterator;
            this.changeType = changeType;
            this.repo = repo;
        }

        @Override
        protected DiffEntry computeNext() {
            if (!treeIterator.hasNext()) {
                return endOfData();
            }

            final Object nextObj = treeIterator.next();
            if (nextObj instanceof DiffEntry) {
                return (DiffEntry) nextObj;
            }

            Assert.isTrue(nextObj instanceof Ref);

            final Ref next = (Ref) nextObj;
            final List<String> childPath = childPath(next.getName());

            if (TYPE.TREE.equals(next.getType())) {
                RevTree tree = repo.getTree(next.getObjectId());
                Predicate<Ref> filter = null;// TODO: propagate filter?
                Iterator<?> childTreeIterator;
                childTreeIterator = new AddRemoveAllTreeIterator(this.changeType, childPath,
                        oldCommit, newCommit, tree, repo);
                this.treeIterator = Iterators.concat(childTreeIterator, this.treeIterator);
                return computeNext();
            }

            Assert.isTrue(TYPE.BLOB.equals(next.getType()));

            Ref oldObject = null;
            Ref newObject = null;
            String name = next.getName();
            if (changeType == ChangeType.ADD) {
                newObject = next;
            } else {
                oldObject = next;
            }
            DiffEntry diffEntry;
            diffEntry = DiffEntry
                    .newInstance(oldCommit, newCommit, oldObject, newObject, childPath);
            return diffEntry;
        }

    }

    /**
     * Traverses the direct children iterators of both trees (fromTree and toTree) simultaneously.
     * If the current children is named the same for both iterators, finds out whether the two
     * children are changed. If the two elements of the current iteration are not the same, find out
     * whether it's an addition or a deletion.
     * 
     * @author groldan
     * 
     */
    private static class TreeDiffEntryIterator extends AbstractDiffIterator {

        private final ObjectId oldCommit;

        private final ObjectId newCommit;

        private final RevTree oldTree;

        private final RevTree newTree;

        private Iterator<DiffEntry> currSubTree;

        private RewindableIterator<Ref> oldEntries;

        private RewindableIterator<Ref> newEntries;

        private final Repository repo;

        public TreeDiffEntryIterator(final List<String> basePath, final ObjectId fromCommit,
                final ObjectId toCommit, final RevTree fromTree, final RevTree toTree,
                final Repository repo) {
            super(basePath);
            this.oldCommit = fromCommit;
            this.newCommit = toCommit;
            this.oldTree = fromTree;
            this.newTree = toTree;
            this.repo = repo;
            this.oldEntries = new RewindableIterator<Ref>(oldTree.iterator(null));
            this.newEntries = new RewindableIterator<Ref>(newTree.iterator(null));
        }

        @Override
        protected DiffEntry computeNext() {
            if (currSubTree != null && currSubTree.hasNext()) {
                return currSubTree.next();
            }
            if (!oldEntries.hasNext() && !newEntries.hasNext()) {
                return endOfData();
            }
            if (oldEntries.hasNext() && !newEntries.hasNext()) {
                currSubTree = new AddRemoveAllTreeIterator(ChangeType.DELETE, this.basePath,
                        this.oldCommit, this.newCommit, this.oldEntries, this.repo);
                return computeNext();
            }
            if (!oldEntries.hasNext() && newEntries.hasNext()) {
                currSubTree = new AddRemoveAllTreeIterator(ChangeType.ADD, basePath, oldCommit,
                        newCommit, newEntries, repo);
                return computeNext();
            }
            Assert.isTrue(currSubTree == null || !currSubTree.hasNext());
            Assert.isTrue(oldEntries.hasNext() && newEntries.hasNext());
            Ref nextOld = oldEntries.next();
            Ref nextNew = newEntries.next();

            while (nextOld.equals(nextNew)) {
                // no change, keep going, but avoid too much recursion
                if (oldEntries.hasNext() && newEntries.hasNext()) {
                    nextOld = oldEntries.next();
                    nextNew = newEntries.next();
                } else {
                    return computeNext();
                }
            }

            final String oldEntryName = nextOld.getName();
            final String newEntryName = nextNew.getName();

            final ChangeType changeType;
            final Ref oldRef, newRef;
            final RevObject.TYPE objectType;
            final List<String> childPath;

            if (oldEntryName.equals(newEntryName)) {
                // same child name, found a changed object
                childPath = childPath(oldEntryName);
                changeType = ChangeType.MODIFY;
                objectType = nextOld.getType();
                oldRef = nextOld;
                newRef = nextNew;

            } else {
                // not the same object (blob or tree), find out whether it's an addition or a
                // deletion. Uses the same ordering than RevTree's iteration order to perform the
                // comparison
                final int comparison = ObjectId.forString(oldEntryName).compareTo(
                        ObjectId.forString(newEntryName));
                Assert.isTrue(comparison != 0, "Comparison can't be 0 if reached this point!");

                if (comparison < 0) {
                    // something was deleted in oldVersion, return a delete diff from oldVersion and
                    // return the item to the "newVersion" iterator for the next round of
                    // pair-to-pair comparisons
                    newEntries.returnElement(nextNew);
                    changeType = ChangeType.DELETE;
                    childPath = childPath(oldEntryName);
                    objectType = nextOld.getType();
                    oldRef = nextOld;
                    newRef = null;
                } else {
                    // something was added in newVersion, return an "add diff" for newVersion and
                    // return the item to the "oldVersion" iterator for the next rounds of
                    // pair-to-pair comparisons
                    oldEntries.returnElement(nextOld);
                    changeType = ChangeType.ADD;
                    childPath = childPath(newEntryName);
                    objectType = nextNew.getType();
                    oldRef = null;
                    newRef = nextNew;
                }

            }

            if (RevObject.TYPE.BLOB.equals(objectType)) {
                DiffEntry singleChange = DiffEntry.newInstance(oldCommit, newCommit, oldRef,
                        newRef, childPath);
                return singleChange;
            }

            Assert.isTrue(RevObject.TYPE.TREE.equals(objectType));

            Iterator<DiffEntry> changesIterator;

            switch (changeType) {
            case ADD:
            case DELETE: {
                ObjectId treeId = null == oldRef ? newRef.getObjectId() : oldRef.getObjectId();
                RevTree childTree = repo.getTree(treeId);
                changesIterator = new AddRemoveAllTreeIterator(changeType, childPath, oldCommit,
                        newCommit, childTree, repo);
                break;
            }
            case MODIFY: {
                Assert.isTrue(RevObject.TYPE.TREE.equals(nextOld.getType()));
                Assert.isTrue(RevObject.TYPE.TREE.equals(nextNew.getType()));
                RevTree oldChildTree = repo.getTree(oldRef.getObjectId());
                RevTree newChildTree = repo.getTree(newRef.getObjectId());
                changesIterator = new TreeDiffEntryIterator(childPath, oldCommit, newCommit,
                        oldChildTree, newChildTree, repo);
                break;
            }
            default:
                throw new IllegalStateException("Unrecognized change type: " + changeType);
            }
            if (this.currSubTree == null || !this.currSubTree.hasNext()) {
                this.currSubTree = changesIterator;
            } else {
                this.currSubTree = Iterators.concat(changesIterator, this.currSubTree);
            }
            return computeNext();
        }

    }

    private static class RewindableIterator<T> extends AbstractIterator<T> {

        private Iterator<T> subject;

        private LinkedList<T> returnQueue;

        public RewindableIterator(Iterator<T> subject) {
            this.subject = subject;
            this.returnQueue = new LinkedList<T>();
        }

        public void returnElement(T element) {
            this.returnQueue.offer(element);
        }

        @Override
        protected T computeNext() {
            T peak = returnQueue.poll();
            if (peak != null) {
                return peak;
            }
            if (!subject.hasNext()) {
                return endOfData();
            }
            return subject.next();
        }

    }
}
