package org.geogit.storage;

import org.geogit.api.ObjectId;

/**
 * Encapsulates a transaction.
 * <p>
 * Use the same ObjectInserter for a single transaction
 * </p>
 * 
 * @author groldan
 * 
 */
public class ObjectInserter {

    private ObjectDatabase objectDb;

    // TODO: transaction management
    public ObjectInserter(ObjectDatabase objectDatabase) {
        objectDb = objectDatabase;
    }

    public ObjectId insert(final ObjectWriter<?> writer) throws Exception {
        ObjectId objectId = objectDb.put(writer);
        return objectId;
    }

}
