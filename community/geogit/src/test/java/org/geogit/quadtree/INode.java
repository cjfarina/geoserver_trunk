package org.geogit.quadtree;

import java.io.Serializable;

import com.vividsolutions.jts.geom.Envelope;

public class INode implements Serializable {

    private static final long serialVersionUID = 1L;

    public final Envelope envelope;

    public final String id;

    public INode(Envelope e, String id) {
        envelope = e;
        this.id = id;
    }
}
