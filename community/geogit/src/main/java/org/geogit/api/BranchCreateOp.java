package org.geogit.api;

import org.geogit.repository.Repository;

public class BranchCreateOp extends AbstractGeoGitOp<Ref> {

    private String branchName;

    public BranchCreateOp(Repository repository) {
        super(repository);
    }

    public BranchCreateOp setName(final String branchName) {
        this.branchName = branchName;
        return this;
    }

    public Ref call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
