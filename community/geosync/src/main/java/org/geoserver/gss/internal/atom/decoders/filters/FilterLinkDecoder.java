package org.geoserver.gss.internal.atom.decoders.filters;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.geoserver.gss.internal.atom.decoders.AbstractDecoder;
import org.gvsig.bxml.stream.BxmlStreamReader;
import org.opengis.filter.Filter;

public abstract class FilterLinkDecoder extends AbstractDecoder<Filter> {

    private FilterLinkDecoder filterLink;
    
    public FilterLinkDecoder(final QName name){
        super(name);
    }
    
    public FilterLinkDecoder(final QName name, final FilterLinkDecoder filterLink) {
        this(name);
        this.filterLink = filterLink;
    }
    
    public Filter decodeFilter(final BxmlStreamReader r) throws IOException {
        QName name = r.getElementName();
        
        if(this.name.equals(name)){
            return decode(r);
        } else if(filterLink != null) {
            return filterLink.decodeFilter(r);
        } else {
            return null;
        }
    }
}
