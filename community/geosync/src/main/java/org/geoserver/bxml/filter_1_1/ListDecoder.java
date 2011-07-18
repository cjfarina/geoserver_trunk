package org.geoserver.bxml.filter_1_1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.geoserver.bxml.AbstractDecoder;
import org.opengis.filter.Filter;

public abstract class ListDecoder<T> extends AbstractDecoder<T> {

    private final List<QName> canHandle = new ArrayList<QName>();
    
    @Override
    public Boolean canHandle(QName name) {
        return canHandle.contains(name);
    }
    
    public void add(QName name) {
        canHandle.add(name);
    }
    
}
