package org.geoserver.gss.internal.atom.decoders;

import org.opengis.filter.Filter;
import static org.geoserver.wfs.xml.v1_1_0.WFS.DELETE;

public class FilterDecoder extends AbstractDecoder<Filter> {

    public FilterDecoder() {
        super(DELETE);
    }
    
    
    @Override
    protected Filter buildResult() {
        // TODO Auto-generated method stub
        return null;
    }

}
