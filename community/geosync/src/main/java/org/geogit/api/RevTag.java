package org.geogit.api;

/**
 * An annotated tag.
 * 
 * @author groldan
 * 
 */
public class RevTag extends RevObject {

    private String name;

    private ObjectId commit;

    public RevTag(ObjectId id) {
        super(id);
    }

    @Override
    public TYPE getType() {
        return TYPE.TAG;
    }

}
