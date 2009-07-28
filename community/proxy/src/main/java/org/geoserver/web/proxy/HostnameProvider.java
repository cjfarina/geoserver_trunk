/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.proxy;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.geoserver.proxy.ProxyConfig;
import org.geoserver.web.wicket.GeoServerDataProvider;



/**
 * Provides a table model for listing layer groups
 */
@SuppressWarnings("serial")
public class HostnameProvider extends GeoServerDataProvider<String> {
    
    public static Property<String> hostnameProp = 
        new AbstractProperty<String>("hostnameProp"){
        public String getPropertyValue(String string)
        {
            return string;
        }
    };

    static List<Property<String>> PROPERTIES = Arrays.asList(hostnameProp);
    
    @Override
    protected List<String> getItems() {
        return ProxyConfig.loadConfFromDisk().hostnameWhitelist;
    }

    @Override
    protected List<Property<String>> getProperties() {
        return PROPERTIES;
    }

    public IModel model(Object object) {
        return new Model((String) object );
    }

}