package org.geogit.api;

import java.util.Iterator;

import org.geogit.repository.Repository;

/**
 * Perform a diff between trees pointed out by two commits
 * 
 */
public class DiffOp extends AbstractGeoGitOp<Iterator<DiffEntry>> {

    private ObjectId oldVersion;

    private ObjectId newVersion;

    private String[] pathFilter;

    public DiffOp(Repository repository) {
        super(repository);
    }

    /**
     * @return the oldVersion
     */
    public ObjectId getOldVersion() {
        return oldVersion;
    }

    /**
     * @param oldTreeId
     *            the oldVersion to set
     * @return
     */
    public DiffOp setOldVersion(ObjectId oldTreeId) {
        this.oldVersion = oldTreeId;
        return this;
    }

    /**
     * @return the newVersion
     */
    public ObjectId getNewVersion() {
        return newVersion;
    }

    /**
     * @param newTreeId
     *            the newVersion to set
     * @return
     */
    public DiffOp setNewVersion(ObjectId newTreeId) {
        this.newVersion = newTreeId;
        return this;
    }

    public DiffOp setFiler(String... pathFilter) {
        this.pathFilter = pathFilter;
        return this;
    }

    @Override
    public Iterator<DiffEntry> call() throws Exception {
        if (oldVersion == null) {
            throw new IllegalStateException("Old version not specified");
        }
        final Repository repo = getRepository();
        if (newVersion == null) {
            /*
             * new version not specified, assume head
             */
            Ref head = repo.getRef(Ref.HEAD);
            newVersion = head.getObjectId();
        }

        DiffTreeWalk diffReader = new DiffTreeWalk(repo, oldVersion, newVersion);

        diffReader.setFilter(this.pathFilter);

        Iterator<DiffEntry> iterator = diffReader.get();

        return iterator;
    }

}
