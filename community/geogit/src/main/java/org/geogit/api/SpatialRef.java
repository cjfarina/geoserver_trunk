package org.geogit.api;

import org.geogit.api.RevObject.TYPE;
import org.opengis.geometry.BoundingBox;

public class SpatialRef extends Ref {

    private BoundingBox bounds;

    public SpatialRef(String name, ObjectId oid, TYPE type, BoundingBox bounds) {
        super(name, oid, type);
        this.bounds = bounds;
    }

    public BoundingBox getBounds() {
        return bounds;
    }
}
