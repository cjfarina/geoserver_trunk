package org.geoserver.gss.internal.atom.decoders;

import org.geoserver.gss.internal.atom.Atom;
import org.geoserver.gss.internal.atom.ContentImpl;
import org.geoserver.gss.internal.atom.DeleteElementTypeEncoder;

import static org.geoserver.wfs.xml.v1_1_0.WFS.DELETE;

public class DeleteElementTypeDecoder extends AbstractDecoder<DeleteElementTypeEncoder> {

    private final DeleteElementTypeEncoder deleteElement;
    public DeleteElementTypeDecoder() {
        super(DELETE);
        deleteElement = new DeleteElementTypeEncoder();
    }
    
    @Override
    protected DeleteElementTypeEncoder buildResult() {
        return deleteElement;
    }

}
