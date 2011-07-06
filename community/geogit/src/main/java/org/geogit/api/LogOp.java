package org.geogit.api;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.geogit.repository.Repository;
import org.geotools.util.Range;
import org.springframework.util.Assert;

/**
 * 
 * @author groldan
 * 
 */
public class LogOp extends AbstractGeoGitOp<Iterable<RevCommit>> {

    private static final Range<Long> ALWAYS = new Range<Long>(Long.class, 0L, true, Long.MAX_VALUE,
            true);

    private boolean ascending;

    private Range<Long> timeRange;

    public LogOp(final Repository repository) {
        super(repository);
        ascending = false;
        timeRange = ALWAYS;
    }

    /**
     * Sets whether to return the list of commits in ascending or descending temporal order; default
     * is to return in descending temporal order.
     * 
     * @param ascending
     *            {@code true} to return oldest commit first
     * @return {@code this}
     */
    public LogOp setAscending(boolean ascending) {
        this.ascending = ascending;
        return this;
    }

    public LogOp setCommitRangeTime(Range<Date> commitRange) {
        if (commitRange == null) {
            this.timeRange = ALWAYS;
        } else {
            this.timeRange = new Range<Long>(Long.class, commitRange.getMinValue().getTime(),
                    commitRange.isMinIncluded(), commitRange.getMaxValue().getTime(),
                    commitRange.isMaxIncluded());
        }
        return this;
    }

    /**
     * @return the list of commits that satisfy the query criteria, most recent first by default, or
     *         oldest first if such is requested through {@code #setAscending(true)}
     * @see org.geogit.api.AbstractGeoGitOp#call()
     */
    @Override
    public Iterable<RevCommit> call() throws Exception {
        final Repository repository = getRepository();

        ObjectId commitId;
        {
            Ref head = repository.getRef(Ref.HEAD);
            commitId = head.getObjectId();
        }
        LinkedList<RevCommit> commits = new LinkedList<RevCommit>();
        RevCommit commit;
        while (commitId != null && !commitId.isNull()) {
            commit = repository.getCommit(commitId);
            if (timeRange.contains(Long.valueOf(commit.getTimestamp()))) {
                if (ascending) {
                    commits.addFirst(commit);
                } else {
                    commits.addLast(commit);
                }
            }
            List<ObjectId> parentIds = commit.getParentIds();
            Assert.notNull(parentIds);
            Assert.isTrue(parentIds.size() > 0);
            commitId = parentIds.get(0);
        }

        return commits;
    }

}
