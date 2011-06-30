package org.geogit.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.geogit.repository.Index;
import org.geogit.storage.FeatureWriter;
import org.geogit.test.RepositoryTestCase;
import org.geotools.util.Range;
import org.opengis.feature.Feature;
import org.opengis.feature.type.Name;

public class LogOpTest extends RepositoryTestCase {

    private GeoGIT ggit;

    private LogOp logOp;

    private Index index;

    @Override
    protected void setUpInternal() throws Exception {
        ggit = new GeoGIT(repo);
        logOp = ggit.log();
        index = ggit.getRepository().getIndex();
    }

    public void testEmptyRepo() throws Exception {
        Iterable<RevCommit> logs = logOp.call();
        assertNotNull(logs);
        assertFalse(logs.iterator().hasNext());
    }

    public void testHeadWithSingleCommit() throws Exception {

        index.inserted(new FeatureWriter(feature1_1), namespace1, typeName1, feature1_1
                .getIdentifier().getID());
        final RevCommit firstCommit = ggit.commit().call();

        Iterable<RevCommit> logs = logOp.call();
        assertNotNull(logs);

        Iterator<RevCommit> iterator = logs.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(firstCommit, iterator.next());
        assertFalse(iterator.hasNext());
    }

    public void testHeadWithTwoCommits() throws Exception {

        index.inserted(new FeatureWriter(feature1_1), namespace1, typeName1, feature1_1
                .getIdentifier().getID());
        final RevCommit firstCommit = ggit.commit().call();

        index.inserted(new FeatureWriter(feature2_1), namespace2, typeName2, feature2_1
                .getIdentifier().getID());
        final RevCommit secondCommit = ggit.commit().call();

        Iterable<RevCommit> logs = logOp.call();
        assertNotNull(logs);

        Iterator<RevCommit> iterator = logs.iterator();
        assertTrue(iterator.hasNext());
        // by default returns most recent first
        assertEquals(secondCommit, iterator.next());

        assertTrue(iterator.hasNext());
        assertEquals(firstCommit, iterator.next());

        assertFalse(iterator.hasNext());
    }

    public void testHeadWithMultipleCommits() throws Exception {

        List<Feature> features = Arrays.asList(feature1_1, feature2_1, feature1_2, feature2_2,
                feature1_3, feature2_3);
        LinkedList<RevCommit> expected = new LinkedList<RevCommit>();

        for (Feature f : features) {
            Name name = f.getType().getName();
            String namespaceURI = name.getNamespaceURI();
            String localPart = name.getLocalPart();
            String id = f.getIdentifier().getID();
            index.inserted(new FeatureWriter(f), namespaceURI, localPart, id);
            final RevCommit commit = ggit.commit().call();
            expected.addFirst(commit);
        }

        Iterable<RevCommit> logs = logOp.call();
        List<RevCommit> logged = new ArrayList<RevCommit>();
        for (RevCommit c : logs) {
            logged.add(c);
        }

        assertEquals(expected, logged);
    }

    public void testHeadWithMultipleCommitsOldestFirst() throws Exception {

        List<Feature> features = Arrays.asList(feature1_1, feature2_1, feature1_2, feature2_2,
                feature1_3, feature2_3);
        LinkedList<RevCommit> expected = new LinkedList<RevCommit>();

        for (Feature f : features) {
            Name name = f.getType().getName();
            String namespaceURI = name.getNamespaceURI();
            String localPart = name.getLocalPart();
            String id = f.getIdentifier().getID();
            index.inserted(new FeatureWriter(f), namespaceURI, localPart, id);
            final RevCommit commit = ggit.commit().call();
            // adLast cause we'll ask for logOp.setAscending(true)
            expected.addLast(commit);
        }

        Iterable<RevCommit> logged = toList(logOp.setAscending(true).call());

        assertEquals(expected, logged);
    }

    public void testTemporalConstraint() throws Exception {

        List<Feature> features = Arrays.asList(feature1_1, feature2_1, feature1_2, feature2_2,
                feature1_3, feature2_3);
        List<Long> timestamps = Arrays.asList(Long.valueOf(1000), Long.valueOf(2000),
                Long.valueOf(3000), Long.valueOf(4000), Long.valueOf(5000), Long.valueOf(6000));

        LinkedList<RevCommit> allCommits = new LinkedList<RevCommit>();

        for (int i = 0; i < features.size(); i++) {
            Feature f = features.get(i);
            Long timestamp = timestamps.get(i);
            Name name = f.getType().getName();
            String namespaceURI = name.getNamespaceURI();
            String localPart = name.getLocalPart();
            String id = f.getIdentifier().getID();
            index.inserted(new FeatureWriter(f), namespaceURI, localPart, id);
            final RevCommit commit = ggit.commit().setTimestamp(timestamp).call();
            // adLast cause we'll ask for logOp.setAscending(true)
            allCommits.addLast(commit);
        }

        // test time range exclusive
        boolean minInclusive = false;
        boolean maxInclusive = false;
        Range<Date> commitRange = new Range<Date>(Date.class, new Date(2000), minInclusive,
                new Date(5000), maxInclusive);
        logOp.setCommitRangeTime(commitRange);
        logOp.setAscending(true);

        List<RevCommit> logged = toList(logOp.call());
        List<RevCommit> expected = allCommits.subList(2, 4);
        assertEquals(expected, logged);

        // test time range inclusive
        minInclusive = true;
        maxInclusive = true;
        commitRange = new Range<Date>(Date.class, new Date(2000), minInclusive, new Date(5000),
                maxInclusive);
        logOp = ggit.log().setAscending(true).setCommitRangeTime(commitRange);

        logged = toList(logOp.call());
        expected = allCommits.subList(1, 5);
        assertEquals(expected, logged);

        // test reset time range
        logOp = ggit.log().setAscending(true).setCommitRangeTime(commitRange)
                .setCommitRangeTime(null);
        logged = toList(logOp.call());
        expected = allCommits;
        assertEquals(expected, logged);
    }

}
