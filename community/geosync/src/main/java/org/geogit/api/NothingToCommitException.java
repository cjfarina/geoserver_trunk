package org.geogit.api;

/**
 * Indicates there are no staged changes to commit as the result of the execution of a
 * {@link CommitOp}
 * 
 * @author groldan
 * 
 */
public class NothingToCommitException extends Exception {

    private static final long serialVersionUID = 1L;

    public NothingToCommitException(String msg) {
        super(msg);
    }
}
