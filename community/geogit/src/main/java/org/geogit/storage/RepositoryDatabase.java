package org.geogit.storage;

public interface RepositoryDatabase {

    public RefDatabase getReferenceDatabase();

    public ObjectDatabase getObjectDatabase();

    public void create();

    public void close();

    public void beginTransaction();

    public void commitTransaction();

    public void rollbackTransaction();
}
