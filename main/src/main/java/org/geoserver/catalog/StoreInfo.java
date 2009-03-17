/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.catalog;

import java.io.Serializable;
import java.util.Map;

/**
 * A store of geoaspatial resources.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 */
public interface StoreInfo extends Serializable, Info {

    /**
     * The store identifier.
     */
    String getId();

    /**
     * The catalog the store is part of.
     */
    Catalog getCatalog();
    
    /**
     * The store name.
     * <p>
     * This value is unique among all stores and can be used to identify the
     * store.
     * </p>
     * 
     * @uml.property name="name"
     */
    String getName();

    /**
     * Sets the name of the store.
     * 
     * @uml.property name="name"
     */
    void setName(String name);

    /**
     * The store description.
     * <p>
     * This is usually something that is used in a user interface.
     * </p>
     * 
     * @uml.property name="description"
     */
    String getDescription(); // FIXME: InternationalString ?

    /**
     * Sets the store description.
     * <p>
     * This is usually something that is used in a user interface.
     * </p>
     * 
     * @uml.property name="description"
     */
    void setDescription(String description);

    /**
     * Map of persistent properties associated with the store.
     * <p>
     * The intent of this map is for services to associate data with a
     * particular store which must be persisted.
     * </p>
     * <p>
     * Key values in this map are of type {@link String}, and values are of
     * type {@link Serializable}.
     * </p>
     * 
     * @uml.property name="metadata"
     */
    Map<String,Serializable> getMetadata();

    /**
     * Flag indicating wether or not teh store is enabled or not.
     * 
     * @uml.property name="enabled"
     */
    boolean isEnabled();

    /**
     * Sets the store enabled / disabled flag.
     * 
     * @uml.property name="enabled"
     */
    void setEnabled(boolean enabled);

    /**
     * The namespace the store is part of.
     * <p>
     * This value is often used to set the namespace of {@link ResourceInfo}objects
     * which are associated to the store.
     * </p>
     * 
     * @uml.property name="namespace"
     * @uml.associationEnd inverse="storeInfo:org.geoserver.catalog.NamespaceInfo"
     */
    //NamespaceInfo getNamespace();

    /**
     * Sets the namespace the store is part of.
     * 
     * @uml.property name="namespace"
     */
    //void setNamespace(NamespaceInfo namespace);
    
    /**
     * The workspace the store is part of.
     *
     */
    WorkspaceInfo getWorkspace();
    
    /**
     * Sets the workspace the store is part of.
     */
    void setWorkspace( WorkspaceInfo workspace );

    /**
     * The map of connection paramters specific to the store.
     * <p>
     * Key values in this map are of type {@link String}, and values are of
     * type {@link Serializable}.
     * </p>
     * 
     * @uml.property name="connectionParameters"
     */
    Map<String, Serializable> getConnectionParameters();
    
    /**
     * An error associated with the store.
     * <p>
     * This value is used to store a problem that occured while attemping to 
     * connect to the underlying resource of the store. It returns <code>null</code>
     * if no such error exists.
     * </p>
     * <p>
     * This is a transient property of the store.
     * </p>
     */
    Throwable getError();

    /**
     * Associates an error with the store.
     *
     * @see {@link #getError()}
     */
    void setError(Throwable t);
    
    /**
     * Creates an adapter for the store.
     * <p>
     * 
     * </p>
     * 
     * @param adapterClass
     *                The class of the adapter.
     * @param hints
     *                Hints to use when creating the adapter.
     * 
     * @return The adapter, an intsanceof adapterClass, or <code>null</code>.
     */
    <T extends Object> T getAdapter(Class<T> adapterClass, Map<?,?> hints);

    /**
     * @return Returns a resource with the specified name that is provided by 
     * the store, or <code>null</code> if no such resource exists.
     * <p>
     * The monitor is used to report the progress of loading resoures
     * and report any warnings / errors that occur in doing so. Monitor
     * may also be null.
     *</p>

     */
    //<T extends Resource> T getResource( String name, ProgressListener monitor) 
    //    throws IOException;
    
    /**
     * @return Returns the resources provided by this store.
     * <p>
     * The monitor is used to report the progress of loading resoures
     * and report any warnings / errors that occur in doing so. Monitor
     * may also be null.
     *</p>
     * @uml.property name="resources"
     * @uml.associationEnd multiplicity="(0 -1)"
     *                     inverse="storeInfo:org.geoserver.catalog.Resource"
     */
    //<T extends Resource> Iterator<T> getResources(ProgressListener monitor) throws IOException;

}
