package org.geogit.api;

/**
 * A binary representation of the state of a Feature.
 * 
 * @author groldan
 * 
 */
public class RevBlob extends RevObject {

    public RevBlob(ObjectId id) {
        super(id);
    }

    @Override
    public TYPE getType() {
        return TYPE.BLOB;
    }

}
