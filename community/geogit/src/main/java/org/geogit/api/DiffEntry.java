package org.geogit.api;

import java.util.Collections;
import java.util.List;

import org.springframework.util.Assert;

public class DiffEntry {

    public static enum ChangeType {
        /**
         * Add a new Feature
         */
        ADD,

        /**
         * Modify an existing Feature
         */
        MODIFY,

        /**
         * Delete an existing Feature
         */
        DELETE
    }

    private final ObjectId oldObjectId;

    private final ObjectId newObjectId;

    private final ChangeType type;

    /**
     * Path to object. Basically a three step path name made of
     * {@code [namespace, FeatureType name, Feature ID]}
     */
    private final List<String> path;

    private final ObjectId oldCommitId;

    private final ObjectId newCommitId;

    public DiffEntry(ChangeType type, ObjectId oldCommitId, ObjectId newCommitId,
            ObjectId oldVersion, ObjectId newVersion, List<String> path) {
        this.type = type;
        this.oldCommitId = oldCommitId;
        this.newCommitId = newCommitId;
        this.oldObjectId = oldVersion;
        this.newObjectId = newVersion;
        this.path = Collections.unmodifiableList(path);
    }

    /**
     * @return the id of the old version of the object, or {@link ObjectId#NULL} if
     *         {@link #getType()} is {@code ADD}
     */
    public ObjectId getOldObjectId() {
        return oldObjectId;
    }

    /**
     * @return the id of the new version of the object, or {@link ObjectId#NULL} if
     *         {@link #getType()} is {@code DELETE}
     */
    public ObjectId getNewObjectId() {
        return newObjectId;
    }

    public ObjectId getOldCommitId() {
        return oldCommitId;
    }

    public ObjectId getNewCommitId() {
        return newCommitId;
    }

    /**
     * @return the type of change
     */
    public ChangeType getType() {
        return type;
    }

    /**
     * @return Path to object. Basically a three step path name made of
     *         {@code [<namespace>, <FeatureType name>, <Feature ID>]}
     */
    public List<String> getPath() {
        return path;
    }

    public String toString() {
        return new StringBuilder(getType().toString()).append(' ').append(getPath()).toString();
    }

    public static DiffEntry newInstance(final ObjectId fromCommit, final ObjectId toCommit,
            final Ref oldObject, final Ref newObject, final List<String> path) {

        Assert.isTrue(oldObject != null || newObject != null);

        ObjectId oldVersion = oldObject == null ? ObjectId.NULL : oldObject.getObjectId();
        ObjectId newVersion = newObject == null ? ObjectId.NULL : newObject.getObjectId();

        ChangeType type;

        if (oldObject == null) {
            type = ChangeType.ADD;
        } else if (newObject == null) {
            type = ChangeType.DELETE;
        } else {
            type = ChangeType.MODIFY;
        }

        DiffEntry entry = new DiffEntry(type, fromCommit, toCommit, oldVersion, newVersion, path);
        return entry;
    }
}
