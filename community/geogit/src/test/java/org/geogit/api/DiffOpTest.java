package org.geogit.api;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.geogit.test.RepositoryTestCase;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;

public class DiffOpTest extends RepositoryTestCase {

    private GeoGIT ggit;

    private DiffOp diffOp;

    @Override
    protected void setUpInternal() throws Exception {
        this.ggit = new GeoGIT(getRepository());
        this.diffOp = ggit.diff();
    }

    public void testDiffPreconditions() throws Exception {
        try {
            diffOp.call();
            fail("Expected ISE: old version not specified");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Old version"));
        }
        Iterator<DiffEntry> difflist = ggit.diff().setOldVersion(ObjectId.NULL).call();
        assertNotNull(difflist);
    }

    public void testEmptyRepo() throws Exception {
        Iterator<DiffEntry> difflist = diffOp.setOldVersion(ObjectId.NULL).call();
        assertNotNull(difflist);
        assertFalse(difflist.hasNext());
    }

    public void testNoChangeSameCommit() throws Exception {

        final ObjectId newOid = insert(feature1_1);
        final RevCommit commit = ggit.commit().setAll(true).call();

        assertFalse(diffOp.setOldVersion(commit.getId()).setNewVersion(commit.getId()).call()
                .hasNext());
    }

    public void testFilterNamespaceNoChanges() throws Exception {

        // two commits on different trees
        insert(feature1_1);
        final RevCommit commit1 = ggit.commit().setAll(true).call();

        insert(feature2_1);
        final RevCommit commit2 = ggit.commit().setAll(true).call();

        diffOp.setOldVersion(commit1.getId()).setNewVersion(commit2.getId());
        diffOp.setFilter(namespace1);

        Iterator<DiffEntry> diffs = diffOp.call();
        assertFalse(diffs.hasNext());
    }

    public void testFilterTypeNameNoChanges() throws Exception {

        // two commits on different trees
        insert(feature1_1);
        final RevCommit commit1 = ggit.commit().setAll(true).call();

        insert(feature2_1);
        final RevCommit commit2 = ggit.commit().setAll(true).call();

        diffOp.setOldVersion(commit1.getId()).setNewVersion(commit2.getId());
        diffOp.setFilter(namespace1, typeName1);

        Iterator<DiffEntry> diffs = diffOp.call();
        assertFalse(diffs.hasNext());
    }

    public void testFilterFeatureIdNoChanges() throws Exception {

        // two commits on different trees
        insert(feature1_1);
        final RevCommit commit1 = ggit.commit().setAll(true).call();

        insert(feature2_1);
        final RevCommit commit2 = ggit.commit().setAll(true).call();

        // filter on feature1_1, it didn't change between commit2 and commit1

        diffOp.setOldVersion(commit1.getId()).setNewVersion(commit2.getId());
        diffOp.setFilter(namespace1, typeName1, feature1_1.getIdentifier().getID());

        Iterator<DiffEntry> diffs = diffOp.call();
        assertFalse(diffs.hasNext());
    }

    public void testSingleAddition() throws Exception {

        final ObjectId newOid = insert(feature1_1);
        final RevCommit commit = ggit.commit().setAll(true).call();

        List<DiffEntry> difflist = toList(diffOp.setOldVersion(ObjectId.NULL).call());

        assertNotNull(difflist);
        assertEquals(1, difflist.size());
        DiffEntry de = difflist.get(0);

        List<String> expectedPath = Arrays.asList(namespace1, typeName1, feature1_1.getIdentifier()
                .getID());
        assertEquals(expectedPath, de.getPath());

        assertEquals(DiffEntry.ChangeType.ADD, de.getType());
        assertEquals(ObjectId.NULL, de.getOldObjectId());

        assertEquals(commit.getId(), de.getNewCommitId());
        assertEquals(newOid, de.getNewObjectId());

    }

    public void testSingleAdditionReverseOrder() throws Exception {

        final ObjectId newOid = insert(feature1_1);
        final RevCommit commit = ggit.commit().setAll(true).call();

        List<DiffEntry> difflist = toList(diffOp.setOldVersion(commit.getId())
                .setNewVersion(ObjectId.NULL).call());

        assertNotNull(difflist);
        assertEquals(1, difflist.size());
        DiffEntry de = difflist.get(0);

        assertEquals(DiffEntry.ChangeType.DELETE, de.getType());
        assertEquals(ObjectId.NULL, de.getNewCommitId());
        assertEquals(ObjectId.NULL, de.getNewObjectId());

        assertEquals(commit.getId(), de.getOldCommitId());
        assertEquals(newOid, de.getOldObjectId());
    }

    public void testSingleDeletion() throws Exception {
        final ObjectId featureContentId = insert(feature1_1);
        final RevCommit addCommit = ggit.commit().setAll(true).call();

        assertTrue(delete(feature1_1));
        final RevCommit deleteCommit = ggit.commit().setAll(true).call();

        List<DiffEntry> difflist = toList(diffOp.setOldVersion(addCommit.getId())
                .setNewVersion(deleteCommit.getId()).call());

        final List<String> path = Arrays.asList(namespace1, typeName1, feature1_1.getIdentifier()
                .getID());

        assertNotNull(difflist);
        assertEquals(1, difflist.size());
        DiffEntry de = difflist.get(0);
        assertEquals(path, de.getPath());

        assertEquals(DiffEntry.ChangeType.DELETE, de.getType());

        assertEquals(addCommit.getId(), de.getOldCommitId());
        assertEquals(featureContentId, de.getOldObjectId());

        assertEquals(deleteCommit.getId(), de.getNewCommitId());
        assertEquals(ObjectId.NULL, de.getNewObjectId());
    }

    public void testSingleDeletionReverseOrder() throws Exception {

        final ObjectId featureContentId = insert(feature1_1);
        final RevCommit addCommit = ggit.commit().setAll(true).call();

        assertTrue(delete(feature1_1));
        final RevCommit deleteCommit = ggit.commit().setAll(true).call();

        // set old/new version in reverse order
        List<DiffEntry> difflist = toList(diffOp.setOldVersion(deleteCommit.getId())
                .setNewVersion(addCommit.getId()).call());

        final List<String> path = Arrays.asList(namespace1, typeName1, feature1_1.getIdentifier()
                .getID());

        // then the diff should report an ADD instead of a DELETE
        assertNotNull(difflist);
        assertEquals(1, difflist.size());
        DiffEntry de = difflist.get(0);
        assertEquals(path, de.getPath());

        assertEquals(DiffEntry.ChangeType.ADD, de.getType());

        assertEquals(deleteCommit.getId(), de.getOldCommitId());
        assertEquals(ObjectId.NULL, de.getOldObjectId());

        assertEquals(addCommit.getId(), de.getNewCommitId());
        assertEquals(featureContentId, de.getNewObjectId());
    }

    public void testSingleModification() throws Exception {

        final ObjectId oldOid = insert(feature1_1);
        final RevCommit insertCommit = ggit.commit().setAll(true).call();

        final String featureId = feature1_1.getIdentifier().getID();
        final Feature modifiedFeature = feature((SimpleFeatureType) feature1_1.getType(),
                featureId, "changedProp", new Integer(1500), null);

        final ObjectId newOid = insert(modifiedFeature);

        final RevCommit changeCommit = ggit.commit().setAll(true).call();

        List<DiffEntry> difflist = toList(diffOp.setOldVersion(insertCommit.getId())
                .setNewVersion(changeCommit.getId()).call());

        assertNotNull(difflist);
        assertEquals(1, difflist.size());
        DiffEntry de = difflist.get(0);
        List<String> expectedPath = Arrays.asList(namespace1, typeName1, featureId);
        assertEquals(expectedPath, de.getPath());

        assertEquals(DiffEntry.ChangeType.MODIFY, de.getType());
        assertEquals(insertCommit.getId(), de.getOldCommitId());
        assertEquals(oldOid, de.getOldObjectId());

        assertEquals(changeCommit.getId(), de.getNewCommitId());
        assertEquals(newOid, de.getNewObjectId());
    }

}
