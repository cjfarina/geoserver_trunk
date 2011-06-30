package org.geogit.api;

import org.geogit.repository.Repository;

public class BranchDeleteOp extends AbstractGeoGitOp<String> {

    private String branchName;

    private boolean force;

    public BranchDeleteOp(Repository repository) {
        super(repository);
    }

    /**
     * @return the name of the branch deleted
     * @see java.util.concurrent.Callable#call()
     */
    public String call() throws Exception {
        return null;
    }

    public BranchDeleteOp setName(final String branchName) {
        this.branchName = branchName;
        return this;
    }

    public BranchDeleteOp setForce(final boolean force) {
        this.force = force;
        return this;
    }

}
