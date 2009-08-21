/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.xacml.request;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.geoserver.xacml.geoxacml.XACMLConstants;
import org.geoserver.xacml.role.XACMLRole;

import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.Subject;

/**
 * Builds a request for URL Matching against regular expressions
 * Http parameters are encoded as resources
 * 
 * 
 * @author Christian Mueller
 * 
 */
public class URLMatchRequestCtxBuilder extends RequestCtxBuilder {
    private String urlString = null;

    private Map<String, String> httpParams;

    public String getUrlString() {
        return urlString;
    }

    public URLMatchRequestCtxBuilder(XACMLRole role, String urlString, String method,
            Map<String, String> httpParams) {
        super(role, method);
        this.urlString = urlString;
        this.httpParams = httpParams;
    }

    @Override
    public RequestCtx createRequestCtx() {

        Set<Subject> subjects = new HashSet<Subject>(1);
        addRole(subjects);

        Set<Attribute> resources = new HashSet<Attribute>(1);
        addGeoserverResource(resources);
        addResource(resources, XACMLConstants.URlResourceURI, urlString);
        if (httpParams != null && httpParams.size() > 0) {
            for (Entry<String, String> entry : httpParams.entrySet()) {
                URI paramURI = null;
                try {
                    paramURI = new URI(XACMLConstants.URLParamPrefix + entry.getKey());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e); // should never happen
                }
                addResource(resources, paramURI, entry.getValue());
            }
        }

        Set<Attribute> actions = new HashSet<Attribute>(1);
        addAction(actions);

        Set<Attribute> environment = new HashSet<Attribute>(1);

        RequestCtx ctx = new RequestCtx(subjects, resources, actions, environment);
        return ctx;

    }

}
