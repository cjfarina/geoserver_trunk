/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.xacml.request;

import java.util.HashSet;
import java.util.Set;

import org.geoserver.security.AccessMode;
import org.geoserver.security.DataAccessManager.CatalogMode;
import org.geoserver.xacml.geoxacml.XACMLConstants;
import org.geoserver.xacml.role.XACMLRole;

import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.Subject;

/**
 * Builds a request for testing access of geoserver to the catalog (always Permit)
 * The idea here is to pass back the {@link CatalogMode} in an XACML obligation.
 * 
 * @author Christian Mueller
 *
 */
public class XACMLRoleRequestCtxBuilder extends RequestCtxBuilder {
    public final static XACMLRole RoleEnablementRole = new XACMLRole(XACMLConstants.RoleEnablementRole);
    
    XACMLRole targetRole = null;
    
    public XACMLRoleRequestCtxBuilder(XACMLRole targetRole) {
        super(RoleEnablementRole,AccessMode.READ.toString());
        this.targetRole=targetRole;
    }

    @Override
    public RequestCtx createRequestCtx() {
        
        Set<Subject> subjects = new HashSet<Subject>(1);
        addRole(subjects);
        
        Set<Attribute> resources = new HashSet<Attribute>(1);
        addGeoserverResource(resources);
        addResource(resources, XACMLConstants.RoleEnablemetnResourceURI,targetRole.getAuthority());
        
        Set<Attribute> actions = new HashSet<Attribute>(1);
        addAction(actions);
        
        Set<Attribute> environment = new HashSet<Attribute>(1);
                
        RequestCtx ctx = new RequestCtx(subjects,resources,actions,environment); 
        return ctx;

        
    }
    
    
}