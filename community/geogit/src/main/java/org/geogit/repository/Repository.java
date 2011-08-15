package org.geogit.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevCommit;
import org.geogit.api.RevTree;
import org.geogit.storage.CommitReader;
import org.geogit.storage.FeatureReader;
import org.geogit.storage.ObjectDatabase;
import org.geogit.storage.ObjectInserter;
import org.geogit.storage.RefDatabase;
import org.geogit.storage.RepositoryDatabase;
import org.geogit.storage.RevSHA1Tree;
import org.geogit.storage.RevTreeReader;
import org.geotools.util.logging.Logging;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.springframework.util.Assert;

import com.google.common.base.Throwables;

/**
 * A repository is a collection of commits, each of which is an archive of what the project's
 * working tree looked like at a past date, whether on your machine or someone else's.
 * <p>
 * It also defines HEAD (see below), which identifies the branch or commit the current working tree
 * stemmed from. Lastly, it contains a set of branches and tags, to identify certain commits by
 * name.
 * </p>
 * 
 * @author Gabriel Roldan
 * @see WorkingTree
 */
public class Repository {

    private static final Logger LOGGER = Logging.getLogger(Repository.class);

    private final RepositoryDatabase repoDb;

    private final Index index;

    private final WorkingTree workingTree;

    public Repository(final RepositoryDatabase repoDb) {
        Assert.notNull(repoDb);
        this.repoDb = repoDb;
        index = new Index(this);
        workingTree = new WorkingTree(this);
    }

    public void create() {
        repoDb.create();
    }

    RefDatabase getRefDatabase() {
        return repoDb.getReferenceDatabase();
    }

    ObjectDatabase getObjectDatabase() {
        return repoDb.getObjectDatabase();
    }

    // TreeDatabase getTreeDatabase() {
    // return repoDb.getTreeDatabase();
    // }

    public Index getIndex() {
        return index;
    }

    public void close() {
        repoDb.close();
    }

    public WorkingTree getWorkingTree() {
        return workingTree;
    }

    public InputStream getRawObject(final ObjectId oid) throws IOException {
        return getObjectDatabase().getRaw(oid);
    }

    /**
     * @param revstr
     *            an object reference expression
     * @return the {@code ObjectId} the resolved reference points to, or null if {@code revstr} 
     *         can't be resolved to any ObjectId
     */
    public ObjectId resolve(final String revstr) {
        Ref ref = getRef(revstr);
        ObjectId oid = null;
        if (ref != null) {
            oid = ref.getObjectId();
        }
        return oid;
    }

    public Ref getRef(final String revStr) {
        return getRefDatabase().getRef(revStr);
    }

    public synchronized Ref updateRef(final Ref ref) {
        boolean updated = getRefDatabase().put(ref);
        Assert.isTrue(updated);
        Ref ref2 = getRef(ref.getName());
        Assert.isTrue(ref.equals(ref2));
        return ref;
    }

    public boolean commitExists(final ObjectId id) {
        try {
            getObjectDatabase().getCached(id, new CommitReader());
        } catch (IllegalArgumentException e) {
            return false;
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        return true;
    }

    public RevCommit getCommit(final ObjectId commitId) {
        RevCommit commit;
        try {
            commit = getObjectDatabase().getCached(commitId, new CommitReader());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return commit;
    }

    public RevTree getTree(final ObjectId treeId) {
        if (treeId.isNull()) {
            return newTree();
        }
        RevTree tree;
        try {
            tree = getObjectDatabase().get(treeId, new RevTreeReader(getObjectDatabase()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tree;
    }

    public ObjectId getRootTreeId() {
        // find the root tree
        Ref head = getRef(Ref.HEAD);
        if (head == null) {
            throw new IllegalStateException("Repository has no HEAD");
        }

        final ObjectId headCommitId = head.getObjectId();
        if (headCommitId.isNull()) {
            return ObjectId.NULL;
        }
        final RevCommit lastCommit = getCommit(headCommitId);
        final ObjectId rootTreeId = lastCommit.getTreeId();
        return rootTreeId;
    }

    /**
     * @param path
     * @return the root tree for the current HEAD
     */
    public RevTree getRootTree() {

        RevTree root;
        try {
            ObjectId rootTreeId = getRootTreeId();
            if (rootTreeId.isNull()) {
                return newTree();
            }
            root = getObjectDatabase().get(rootTreeId, new RevTreeReader(getObjectDatabase()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return root;
    }

    /**
     * @return an {@link ObjectInserter} to insert objects into the object database
     */
    public ObjectInserter newObjectInserter() {
        return getObjectDatabase().newObjectInserter();
    }

    public Feature getFeature(final FeatureType featureType, final String featureId,
            final ObjectId contentId) {
        FeatureReader reader = new FeatureReader(featureType, featureId);
        Feature feature;
        try {
            feature = getObjectDatabase().get(contentId, reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return feature;
    }

    public void beginTransaction() {
        this.repoDb.beginTransaction();
    }

    public void commitTransaction() {
        this.repoDb.commitTransaction();
    }

    public void rollbackTransaction() {
        this.repoDb.rollbackTransaction();
    }

    /**
     * Creates and return a new, empty tree
     */
    public RevTree newTree() {
        return new RevSHA1Tree(getObjectDatabase());
    }

    public ObjectId getTreeChildId(String... path) {
        RevTree root = getRootTree();
        return getTreeChildId(root, path);
    }

    public ObjectId getTreeChildId(RevTree root, String... path) {
        return getTreeChildId(root, Arrays.asList(path));
    }

    public ObjectId getTreeChildId(RevTree root, List<String> path) {
        Ref treeRef = new DepthSearch(this).find(root, path);
        return treeRef == null ? null : treeRef.getObjectId();
    }
}
