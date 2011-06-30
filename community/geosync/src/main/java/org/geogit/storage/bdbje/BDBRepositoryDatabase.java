package org.geogit.storage.bdbje;

import org.geogit.storage.ObjectDatabase;
import org.geogit.storage.RefDatabase;
import org.geogit.storage.RepositoryDatabase;

import com.sleepycat.je.Environment;

public class BDBRepositoryDatabase implements RepositoryDatabase {

    private final Environment environment;

    private ObjectDatabase objectDatabase;

    private RefDatabase referenceDatabase;

    public BDBRepositoryDatabase(final Environment environment) {
        this.environment = environment;
        objectDatabase = new ObjectDatabase(environment);
        referenceDatabase = new RefDatabase(objectDatabase);
    }

    /**
     * @see org.geogit.storage.RepositoryDatabase#create()
     */
    public void create() {
        objectDatabase.create();
        referenceDatabase.create();
    }

    /**
     * @see org.geogit.storage.RepositoryDatabase#close()
     */
    public void close() {
        referenceDatabase.close();
        objectDatabase.close();
        environment.close();
    }

    /**
     * @see org.geogit.storage.RepositoryDatabase#getReferenceDatabase()
     */
    public RefDatabase getReferenceDatabase() {
        return referenceDatabase;
    }

    /**
     * @see org.geogit.storage.RepositoryDatabase#getObjectDatabase()
     */
    public ObjectDatabase getObjectDatabase() {
        return objectDatabase;
    }

    public void beginTransaction() {
        // CurrentTransaction.getInstance(environment).beginTransaction(null);
    }

    public void commitTransaction() {
        // CurrentTransaction.getInstance(environment).commitTransaction();
    }

    public void rollbackTransaction() {
        // CurrentTransaction.getInstance(environment).abortTransaction();
    }

}
