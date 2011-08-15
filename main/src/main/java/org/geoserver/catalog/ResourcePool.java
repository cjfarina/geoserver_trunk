/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.catalog;

import java.awt.RenderingHints;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.io.IOUtils;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geoserver.catalog.event.CatalogAddEvent;
import org.geoserver.catalog.event.CatalogListener;
import org.geoserver.catalog.event.CatalogModifyEvent;
import org.geoserver.catalog.event.CatalogPostModifyEvent;
import org.geoserver.catalog.event.CatalogRemoveEvent;
import org.geoserver.catalog.impl.ModificationProxy;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.data.VersioningPlugin;
import org.geoserver.data.util.CoverageStoreUtils;
import org.geoserver.data.util.CoverageUtils;
import org.geoserver.feature.retype.RetypingFeatureSource;
import org.geoserver.platform.GeoServerExtensions;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.Join;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wms.WebMapServer;
import org.geotools.factory.Hints;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gml2.GML;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.VirtualTable;
import org.geotools.referencing.CRS;
import org.geotools.styling.Style;
import org.geotools.util.logging.Logging;
import org.geotools.xml.Schemas;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.vfny.geoserver.global.GeoServerFeatureLocking;
import org.vfny.geoserver.global.GeoserverDataDirectory;
import org.vfny.geoserver.util.DataStoreUtils;

/**
 * Provides access to resources such as datastores, coverage readers, and 
 * feature types.
 * <p>
 * 
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class ResourcePool {

    /**
     * Hint to specify if reprojection should occur while loading a 
     * resource.
     */
    public static Hints.Key REPROJECT = new Hints.Key( Boolean.class );

    /**
     * Hint to specify additional joined attributes when loading a feature type
     */
    public static Hints.Key JOINS = new Hints.Key(List.class);

    /** logging */
    static Logger LOGGER = Logging.getLogger( "org.geoserver.catalog");

    /**
     * Default number of hard references
     */
    static int FEATURETYPE_CACHE_SIZE_DEFAULT = 100;
    
    private static final String IMAGE_PYRAMID = "ImagePyramid";
    private static final String IMAGE_MOSAIC = "ImageMosaic";

    Catalog catalog;
    HashMap<String, CoordinateReferenceSystem> crsCache;
    DataStoreCache dataStoreCache;
    FeatureTypeCache featureTypeCache;
    FeatureTypeAttributeCache featureTypeAttributeCache;
    WMSCache wmsCache;
    CoverageReaderCache coverageReaderCache;
    CoverageHintReaderCache hintCoverageReaderCache;
    HashMap<StyleInfo,Style> styleCache;
    List<Listener> listeners;
    ThreadPoolExecutor coverageExecutor;

    public ResourcePool(Catalog catalog) {
        this.catalog = catalog;
        crsCache = new HashMap<String, CoordinateReferenceSystem>();
        dataStoreCache = new DataStoreCache();
        featureTypeCache = new FeatureTypeCache(FEATURETYPE_CACHE_SIZE_DEFAULT);
        
        featureTypeAttributeCache = new FeatureTypeAttributeCache(FEATURETYPE_CACHE_SIZE_DEFAULT);
        coverageReaderCache = new CoverageReaderCache();
        hintCoverageReaderCache = new CoverageHintReaderCache();
        
        wmsCache = new WMSCache();
        
        styleCache = new HashMap<StyleInfo, Style>();
        listeners = new CopyOnWriteArrayList<Listener>();
        
        catalog.addListener( new CacheClearingListener() );
    }
    
    /**
     * Sets the size of the feature type cache.
     * <p>
     * A warning that calling this method will blow away the existing cache.
     * </p>
     */
    public void setFeatureTypeCacheSize(int featureTypeCacheSize) {
        synchronized (this) {
            featureTypeCache.clear();
            featureTypeCache = new FeatureTypeCache(featureTypeCacheSize);
            featureTypeAttributeCache.clear();
            featureTypeAttributeCache = new FeatureTypeAttributeCache(featureTypeCacheSize);
        }
    }
    
    /**
     * Sets the size of the feature type cache.
     * <p>
     * A warning that calling this method will blow away the existing cache.
     * </p>
     */
    public void setCoverageExecutor(ThreadPoolExecutor coverageExecutor) {
        synchronized (this) {
            this.coverageExecutor = coverageExecutor;
        }
    }
    
    /**
     * Adds a pool listener.
     */
    public void addListener(Listener l) {
        listeners.add(l);
    }

    /**
     * Removes a pool listener.
     */
    public void removeListener(Listener l) {
        listeners.remove(l);
    }
    
    /**
     * Returns a {@link CoordinateReferenceSystem} object based on its identifier
     * caching the result.
     * <p>
     * The <tt>srsName</tt> parameter should have one of the forms:
     * <ul>
     *   <li>EPSG:XXXX
     *   <li>http://www.opengis.net/gml/srs/epsg.xml#XXXX
     *   <li>urn:x-ogc:def:crs:EPSG:XXXX
     * </ul>
     * OR be something parsable by {@link CRS#decode(String)}.
     * </p>
     * @param srsName The coordinate reference system identifier.
     * 
     * @throws IOException In the event the srsName can not be parsed or leads 
     * to an exception in the underlying call to CRS.decode.
     */
    public CoordinateReferenceSystem getCRS( String srsName )
        throws IOException {
        
        if(srsName == null)
            return null;
        
        CoordinateReferenceSystem crs = crsCache.get( srsName );
        if ( crs == null ) {
            synchronized (crsCache) {
                crs = crsCache.get( srsName );
                if ( crs == null ) {
                    try {
                        crs = CRS.decode( srsName );
                        crsCache.put( srsName, crs );
                    }
                    catch( Exception e) {
                        throw (IOException) new IOException().initCause(e);
                    }
                }
            }
        }
        
        return crs;
    }
    
    /**
     * Returns the datastore factory used to create underlying resources for a datastore.
     * <p>
     * This method first uses {@link DataStoreInfo#getType()} to obtain the datastore. In the 
     * event of a failure it falls back on {@link DataStoreInfo#getConnectionParameters()}.
     * </p>
     * @param info The data store metadata.
     * 
     * @return The datastore factory, or null if no such factory could be found, or the factory
     * is not available.
     * 
     * @throws IOException Any I/O errors.
     */
    public DataAccessFactory getDataStoreFactory( DataStoreInfo info ) throws IOException {
        DataAccessFactory factory = null;
    
        if ( info.getType() != null ) {
            factory = DataStoreUtils.aquireFactory( info.getType() );    
        }
    
        if ( factory == null ) {
            factory = DataStoreUtils.aquireFactory( getParams( info.getConnectionParameters() , GeoserverDataDirectory.getGeoserverDataDirectory().getCanonicalPath()));    
        }
   
        return factory;
    }
    
    /**
     * Returns the underlying resource for a datastore, caching the result.
     * <p>
     * In the result of the resource not being in the cache {@link DataStoreInfo#getConnectionParameters()}
     * is used to connect to it.
     * </p>
     * @param info the data store metadata.
     * 
     * @throws IOException Any errors that occur connecting to the resource.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public DataAccess<? extends FeatureType, ? extends Feature> getDataStore( DataStoreInfo info ) throws IOException {
        try {
            String id = info.getId();
            DataAccess<? extends FeatureType, ? extends Feature> dataStore;
            dataStore = (DataAccess<? extends FeatureType, ? extends Feature>) dataStoreCache.get(id);
            if ( dataStore == null ) {
                synchronized (dataStoreCache) {
                    dataStore = (DataAccess<? extends FeatureType, ? extends Feature>) dataStoreCache.get( id );
                    if ( dataStore == null ) {
                        //create data store
                        Map<String, Serializable> connectionParameters = info.getConnectionParameters();
                        
                        //call this methdo to execute the hack which recognizes 
                        // urls which are relative to the data directory
                        // TODO: find a better way to do this
                        connectionParameters = DataStoreUtils.getParams(connectionParameters,null);
                        
                        //ensure that the namespace parameter is set for the datastore
                        if (!connectionParameters.containsKey( "namespace")) {
                            //obtain the factory
                            DataAccessFactory factory = null;
                            try {
                                factory = getDataStoreFactory(info);
                            }
                            catch(Exception e ) {
                                //ignore, it will fail later
                            }
                            
                            //if we grabbed the factory, check that the factory actually supports
                            // a namespace parameter, if we could not get the factory, assume that
                            // it does
                            boolean supportsNamespace = true;
                            if ( factory != null ) {
                                supportsNamespace = false;
                                Param[] params = factory.getParametersInfo();
                                for ( Param p : params ) {
                                    if ( "namespace".equalsIgnoreCase( p.key ) ) {
                                        supportsNamespace = true;
                                        break;
                                    }
                                }
                            }
                            
                            if ( supportsNamespace ) {
                                WorkspaceInfo ws = info.getWorkspace();
                                NamespaceInfo ns = info.getCatalog().getNamespaceByPrefix( ws.getName() );
                                if ( ns == null ) {
                                    ns = info.getCatalog().getDefaultNamespace();
                                }
                                if ( ns != null ) {
                                    connectionParameters.put( "namespace", ns.getURI() );
                                }    
                            }
                        }
                        
                        dataStore = DataStoreUtils.getDataAccess(connectionParameters);
                        if (dataStore == null) {
                            /*
                             * Preserve DataStore retyping behaviour by calling
                             * DataAccessFinder.getDataStore after the call to
                             * DataStoreUtils.getDataStore above.
                             * 
                             * TODO: DataAccessFinder can also find DataStores, and when retyping is
                             * supported for DataAccess, we can use a single mechanism.
                             */
                            dataStore = DataAccessFinder.getDataStore(connectionParameters);
                        }
                        
                        if ( dataStore == null ) {
                            throw new NullPointerException("Could not acquire data access '" + info.getName() + "'");
                        }
                        
//                        if (GEOGIT_VERSIONING_FS != null) {
//                            LOGGER.fine("Creating versioning wrapper for DataStore " + info.getName()
//                                    + ". Whether or not version info is available is FeatureTypeInfo dependant.");
//                            try {
//                                Method m = GEOGIT_VERSIONING_FS.getMethod("create", DataAccess.class);
//                                dataStore = (DataAccess) m.invoke(null, dataStore);
//                            } catch (Exception e) {
//                                throw new DataSourceException("Creation of a versioning wrapper failed", e);
//                            }
//                        }
                        
                        // cache only if the id is not null, no need to cache the stores
                        // returned from un-saved DataStoreInfo objects (it would be actually
                        // harmful, NPE when trying to dispose of them)
                        if(id != null) {
                            dataStoreCache.put( id, dataStore );
                        }
                    }
                } 
            }
            
            return dataStore;
        } 
        catch (IOException ioe){
            throw ioe;
        }
        catch (Exception e) {
            throw (IOException) new IOException().initCause(e);
        }
    }
        
    /**
     * Get Connect params.
     *
     * <p>
     * This is used to smooth any relative path kind of issues for any file
     * URLS. This code should be expanded to deal with any other context
     * sensitve isses dataStores tend to have.
     * </p>
     *
     * @return DOCUMENT ME!
     *
     * @task REVISIT: cache these?
     */
    public static Map getParams(Map m, String baseDir) {
        Map params = Collections.synchronizedMap(new HashMap(m));

        for (Iterator i = params.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();

            //TODO: this code is a pretty big hack, using the name to 
            // determine if the key is a url, could be named something else
            // and still be a url
            if ((key != null) && key.matches(".* *url") && value instanceof String) {
                String path = (String) value;

                if (path.startsWith("file:")) {
                    File fixedPath = GeoserverDataDirectory.findDataFile(path);
                    entry.setValue(DataUtilities.fileToURL(fixedPath).toExternalForm());
                }
            } else if (value instanceof URL && ((URL) value).getProtocol().equals("file")) {
                File fixedPath = GeoserverDataDirectory.findDataFile(((URL) value).toString());
                entry.setValue(DataUtilities.fileToURL(fixedPath));
            }
        }

        return params;
    }
    
    /**
     * Clears the cached resource for a data store.
     * 
     * @param info The data store metadata.
     */
    public void clear( DataStoreInfo info ) {
        dataStoreCache.remove( info.getId() );
    }
    
    public List<AttributeTypeInfo> getAttributes(FeatureTypeInfo info) throws IOException {
        //first check the feature type itself
        //      workaround for GEOS-3294, upgrading from 2.0 data directory,
        //      simply ignore any stored attributes
        //      Also check if the bindings has been set, if it's not set it means we're reloading
        //      an old set of attributes, by forcing them to be reloaded the binding and length
        //      will be added into the info classes
        if (info.getAttributes() != null && !info.getAttributes().isEmpty() 
                && info.getAttributes().get(0).getBinding() != null) {
            return info.getAttributes();
        }
        
        //check the cache
        List<AttributeTypeInfo> atts = (List<AttributeTypeInfo>) featureTypeAttributeCache.get(info.getId());
        if (atts == null) {
            synchronized (featureTypeAttributeCache) {
                atts = (List<AttributeTypeInfo>) featureTypeAttributeCache.get(info.getId());
                if (atts == null) {
                    //load from feature type
                    atts = loadAttributes(info);
                    
                    //check for a schema override
                    try {
                        handleSchemaOverride(atts,info);
                    }
                    catch( Exception e ) {
                        LOGGER.log( Level.WARNING, 
                            "Error occured applying schema override for "+info.getName(), e);
                    }
                    
                    // cache attributes only if the id is not null -> the feature type is not new
                    if(info.getId() != null) {
                        featureTypeAttributeCache.put(info.getId(), atts);
                    }
                }
            }
        }
        
        return atts;
    }
    
    public List<AttributeTypeInfo> loadAttributes(FeatureTypeInfo info) throws IOException {
        List<AttributeTypeInfo> attributes = new ArrayList();
        FeatureType ft = getFeatureType(info);
        
        for (PropertyDescriptor pd : ft.getDescriptors()) {
            AttributeTypeInfo att = catalog.getFactory().createAttribute();
            att.setFeatureType(info);
            att.setName(pd.getName().getLocalPart());
            att.setMinOccurs(pd.getMinOccurs());
            att.setMaxOccurs(pd.getMaxOccurs());
            att.setNillable(pd.isNillable());
            att.setBinding(pd.getType().getBinding());
            int length = FeatureTypes.getFieldLength((AttributeDescriptor) pd);
            if(length > 0) {
                att.setLength(length);
            }
            attributes.add(att);
        }
        
        return attributes;
    }
    
    void handleSchemaOverride( List<AttributeTypeInfo> atts, FeatureTypeInfo ft ) throws IOException {
        GeoServerDataDirectory dd = new GeoServerDataDirectory(catalog.getResourceLoader());
        File schemaFile = dd.findSuppResourceFile(ft, "schema.xsd");
        if (schemaFile == null) {
            schemaFile = dd.findSuppLegacyResourceFile(ft, "schema.xsd");
            if ( schemaFile == null ) {
                //check for the old style schema.xml
                File oldSchemaFile = dd.findSuppResourceFile(ft, "schema.xml");
                if ( oldSchemaFile == null ) {
                    oldSchemaFile = dd.findSuppLegacyResourceFile(ft, "schema.xml");
                }
                if ( oldSchemaFile != null ) {
                    schemaFile = new File( oldSchemaFile.getParentFile(), "schema.xsd");
                    BufferedWriter out = 
                        new BufferedWriter(new OutputStreamWriter( new FileOutputStream( schemaFile ) ) );
                    out.write( "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'");
                    out.write( " xmlns:gml='http://www.opengis.net/gml'");
                    out.write(">");
                    IOUtils.copy( new FileInputStream( oldSchemaFile ), out );
                    out.write( "</xs:schema>" );
                    out.flush();
                    out.close();
                }
            }
        }
        
        if (schemaFile != null) {
            //TODO: farm this schema loading stuff to some utility class
            //parse the schema + generate attributes from that
            List locators = Arrays.asList( GML.getInstance().createSchemaLocator() );
            XSDSchema schema = null;
            try {
                schema = Schemas.parse( schemaFile.getAbsolutePath(), locators, null );
            }
            catch( Exception e ) {
                LOGGER.warning( "Unable to parse " + schemaFile.getAbsolutePath() + "." +
                    " Falling back on native feature type");
            }
            if ( schema != null ) {
                XSDTypeDefinition type = null;
                for ( Iterator e = schema.getElementDeclarations().iterator(); e.hasNext(); ) {
                    XSDElementDeclaration element = (XSDElementDeclaration) e.next();
                    if ( ft.getName().equals( element.getName() ) ) {
                        type = element.getTypeDefinition();
                        break;
                    }
                }
                if ( type == null ) {
                    for ( Iterator t = schema.getTypeDefinitions().iterator(); t.hasNext(); ) {
                        XSDTypeDefinition typedef = (XSDTypeDefinition) t.next();
                        if ( (ft.getName() + "_Type").equals( typedef.getName() ) ) {
                            type = typedef;
                            break;
                        }
                    }
                }
                
                if ( type != null ) {
                    List children = Schemas.getChildElementDeclarations(type,true);
                    for ( Iterator<AttributeTypeInfo> i = atts.iterator(); i.hasNext(); ) {
                        AttributeTypeInfo at = i.next();
                        boolean found = false;
                        for ( Iterator c = children.iterator(); c.hasNext(); ) {
                            XSDElementDeclaration ce = (XSDElementDeclaration) c.next();
                            if ( at.getName().equals( ce.getName() ) ) {
                                found = true;
                                if (ce.getContainer() instanceof XSDParticle) {
                                    XSDParticle part = (XSDParticle) ce.getContainer();
                                    at.setMinOccurs(part.getMinOccurs());
                                    at.setMaxOccurs(part.getMaxOccurs());
                                }
                                break;
                            }
                        }
                        
                        if ( !found ) {
                            i.remove();
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Returns the underlying resource for a feature type, caching the result.
     * <p>
     * In the event that the resource is not in the cache the associated data store
     * resource is loaded, and the feature type resource obtained. During loading
     * the underlying feature type resource is "wrapped" to take into account 
     * feature type name aliasing and reprojection.
     * </p>
     * @param info The feature type metadata.
     * 
     * @throws IOException Any errors that occure while loading the resource.
     */
    public FeatureType getFeatureType( FeatureTypeInfo info ) throws IOException {
        return getFeatureType(info, true);
    }
    
    FeatureType getFeatureType( FeatureTypeInfo info, boolean handleProjectionPolicy ) throws IOException {
        boolean cacheable = isCacheable(info) && handleProjectionPolicy;
        FeatureType ft = (FeatureType) featureTypeCache.get( info.getId() );
        if ( ft == null || !cacheable ) {
            synchronized ( featureTypeCache ) {
                ft = (FeatureType) featureTypeCache.get( info.getId() );
                if ( ft == null || !cacheable) {
                    
                    //grab the underlying feature type
                    DataAccess<? extends FeatureType, ? extends Feature> dataAccess = getDataStore(info.getStore());
                    
                    // sql view handling
                    VirtualTable vt = null;
                    String vtName = null;
                    if(dataAccess instanceof JDBCDataStore && info.getMetadata() != null &&
                            (info.getMetadata().get(FeatureTypeInfo.JDBC_VIRTUAL_TABLE) instanceof VirtualTable)) {
                        JDBCDataStore jstore = (JDBCDataStore) dataAccess;
                        vt = info.getMetadata().get(FeatureTypeInfo.JDBC_VIRTUAL_TABLE, VirtualTable.class);
                        
                        if(!cacheable) {
                            // use a highly random name, we don't want to actually add the
                            // virtual table to the store as this feature type is not cacheable,
                            // it is "dirty" or un-saved. The renaming below will take care
                            // of making the user see the actual name
                            final String[] typeNames = jstore.getTypeNames();
                            do {
                                vtName = UUID.randomUUID().toString();
                            } while (Arrays.asList(typeNames).contains(vtName));
    
                            // try adding the vt and see if that works
                            jstore.addVirtualTable(new VirtualTable(vtName, vt));
                            ft = jstore.getSchema(vtName);
                        } else {
                            vtName = vt.getName();
                            jstore.addVirtualTable(vt);
                            ft = jstore.getSchema(vt.getName());
                        }
                    } else {
                        ft = dataAccess.getSchema(info.getQualifiedNativeName());
                    }
                    
                    // TODO: support reprojection for non-simple FeatureType
                    if (ft instanceof SimpleFeatureType) {
                        SimpleFeatureType sft = (SimpleFeatureType) ft;
                        //create the feature type so it lines up with the "declared" schema
                        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
                        tb.setName( info.getName() );
                        tb.setNamespaceURI( info.getNamespace().getURI() );

                        if ( info.getAttributes() == null || info.getAttributes().isEmpty() ) {
                            //take this to mean just load all native
                            for ( PropertyDescriptor pd : ft.getDescriptors() ) {
                                if ( !( pd instanceof AttributeDescriptor ) ) {
                                    continue;
                                }
                                
                                AttributeDescriptor ad = (AttributeDescriptor) pd;
                                if(handleProjectionPolicy) {
                                    ad = handleDescriptor(ad, info);
                                }
                                tb.add( ad );
                            }
                        }
                        else {
                            //only load native attributes configured
                            for ( AttributeTypeInfo att : info.getAttributes() ) {
                                String attName = att.getName();
                                
                                //load the actual underlying attribute type
                                PropertyDescriptor pd = ft.getDescriptor( attName );
                                if ( pd == null || !( pd instanceof AttributeDescriptor) ) {
                                    throw new IOException("the SimpleFeatureType " + info.getPrefixedName()
                                            + " does not contains the configured attribute " + attName
                                            + ". Check your schema configuration");
                                }
                            
                                AttributeDescriptor ad = (AttributeDescriptor) pd;
                                ad = handleDescriptor(ad, info);
                                tb.add( (AttributeDescriptor) ad );
                            }
                        }
                        ft = tb.buildFeatureType();
                    } // end special case for SimpleFeatureType
                    
                    if(cacheable) {
                        featureTypeCache.put( info.getId(), ft );
                    } else if(vtName != null) {
                        JDBCDataStore jstore = (JDBCDataStore) dataAccess;
                        jstore.removeVirtualTable(vtName);
                    }
                }
            }
        }
        
        return ft;
    }
    
    /**
     * Returns true if this object is saved in the catalog and not a modified proxy. We don't want to
     * cache the result of computations made against a dirty object, nor the ones made against an 
     * object that still haven't been saved
     * @param info
     * @return
     */
    boolean isCacheable(CatalogInfo info) {
        // saved?
        if(info.getId() == null) {
            return false;
        }
        
        // dirty?
        if(Proxy.isProxyClass(info.getClass())) {
            Object invocationHandler = Proxy.getInvocationHandler(info);
            if(invocationHandler instanceof ModificationProxy 
                    && ((ModificationProxy) invocationHandler).isDirty()) {
                return false;
            }
        } 
        
        return true;
    }

    /*
     * Helper method which overrides geometric attributes based on the reprojection policy.
     */
    AttributeDescriptor handleDescriptor( AttributeDescriptor ad, FeatureTypeInfo info ) {

        // force the user specified CRS if the data has no CRS, or reproject it 
        // if necessary
        if ( ad instanceof GeometryDescriptor ) {
            GeometryDescriptor old = (GeometryDescriptor) ad;
            try {
                //if old has no crs, change the projection handlign policy
                // to be the declared
                boolean rebuild = false;

                if ( old.getCoordinateReferenceSystem() == null ) {
                    //(JD) TODO: this is kind of wierd... we should at least
                    // log something here, and this is not thread safe!!
                    if(info.getProjectionPolicy() != ProjectionPolicy.FORCE_DECLARED) {
                        // modify the actual type info if possible, not the modification
                        // proxy around it
                        if(Proxy.isProxyClass(info.getClass())) {
                            FeatureTypeInfo inner = ModificationProxy.unwrap(info);
                            inner.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
                        } else {
                            info.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
                        }
                    }
                    rebuild = true;
                }
                else {
                    ProjectionPolicy projPolicy = info.getProjectionPolicy();
                    if ( projPolicy == ProjectionPolicy.REPROJECT_TO_DECLARED || 
                            projPolicy == ProjectionPolicy.FORCE_DECLARED ) {
                        rebuild = true;
                    }
                }

                if ( rebuild ) {
                    //rebuild with proper crs
                    AttributeTypeBuilder b = new AttributeTypeBuilder();
                    b.init(old);
                    b.setCRS( getCRS(info.getSRS()) );
                    ad = b.buildDescriptor(old.getLocalName());
                }
            }
            catch( Exception e ) {
                //log exception
            }
        }
        
        return ad;
    }
    
    /**
     * Loads an attribute descriptor from feature type and attribute type metadata.
     * <p>
     * This method returns null if the attribute descriptor could not be loaded.
     * </p>
     */
    public AttributeDescriptor getAttributeDescriptor( FeatureTypeInfo ftInfo, AttributeTypeInfo atInfo ) 
        throws Exception {
    
        FeatureType featureType = getFeatureType( ftInfo );
        if ( featureType != null ) {
            for ( PropertyDescriptor pd : featureType.getDescriptors() ) {
                if (pd instanceof AttributeDescriptor) {
                    AttributeDescriptor ad = (AttributeDescriptor) pd;
                    if (atInfo.getName().equals(ad.getLocalName())) {
                        return ad;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Clears a feature type resource from the cache.
     * 
     * @param info The feature type metadata.
     */
    public void clear( FeatureTypeInfo info ) {
        featureTypeCache.remove( info.getId() );
        featureTypeAttributeCache.remove( info.getId() );
    }
    
    /**
     * Loads the feature source for a feature type.
     * <p>
     * The <tt>hints</tt> parameter is used to control how the feature source is 
     * loaded. An example is using the {@link #REPROJECT} hint to control if the 
     * resulting feature source is reprojected or not.
     * </p>
     * @param info The feature type info.
     * @param hints Any hints to take into account while loading the feature source, 
     *  may be <code>null</code>.
     * 
     * @throws IOException Any errors that occur while loading the feature source.
     */
    public FeatureSource<? extends FeatureType, ? extends Feature> getFeatureSource( FeatureTypeInfo info, Hints hints ) throws IOException {
        DataAccess<? extends FeatureType, ? extends Feature> dataAccess = getDataStore(info.getStore());
        
        // TODO: support aliasing (renaming), reprojection, versioning, and locking for DataAccess
        if (!(dataAccess instanceof DataStore)) {
            return dataAccess.getFeatureSource(info.getQualifiedName());
        }
        
        DataStore dataStore = (DataStore) dataAccess;
        SimpleFeatureSource fs;
        
        // sql view handling
        if(dataStore instanceof JDBCDataStore && info.getMetadata() != null &&
                info.getMetadata().containsKey(FeatureTypeInfo.JDBC_VIRTUAL_TABLE)) {
            VirtualTable vt = (VirtualTable) info.getMetadata().get(FeatureTypeInfo.JDBC_VIRTUAL_TABLE);
            JDBCDataStore jstore = (JDBCDataStore) dataStore;
            jstore.addVirtualTable(vt);
        }
                
        //
        // aliasing and type mapping
        //
        final String typeName = info.getNativeName();
        final String alias = info.getName();
        final SimpleFeatureType nativeFeatureType = dataStore.getSchema( typeName );
        final SimpleFeatureType renamedFeatureType = (SimpleFeatureType) getFeatureType( info, false );
        if ( !typeName.equals( alias ) || DataUtilities.compare(nativeFeatureType,renamedFeatureType) != 0 ) {
            // rename and retype as necessary
            fs = RetypingFeatureSource.getRetypingSource(dataStore.getFeatureSource(typeName), renamedFeatureType);
        } else {
            //normal case
            fs = dataStore.getFeatureSource(info.getQualifiedName());   
        }

        //
        // reprojection
        //
        Boolean reproject = Boolean.TRUE;
        if ( hints != null ) {
            if ( hints.get( REPROJECT ) != null ) {
                reproject = (Boolean) hints.get( REPROJECT );
            }
        }
        
        //get the reprojection policy
        ProjectionPolicy ppolicy = info.getProjectionPolicy();
        
        //if projection policy says to reproject, but calling code specified hint 
        // not to, respect hint
        if ( ppolicy == ProjectionPolicy.REPROJECT_TO_DECLARED && !reproject) {
            ppolicy = ProjectionPolicy.NONE;
        }
        
        List<AttributeTypeInfo> attributes = info.attributes();
        if (attributes == null || attributes.isEmpty()) { 
            return fs;
        } 
        else {
            CoordinateReferenceSystem resultCRS = null;
            GeometryDescriptor gd = fs.getSchema().getGeometryDescriptor();
            CoordinateReferenceSystem nativeCRS = gd != null ? gd.getCoordinateReferenceSystem() : null;
            
            if (ppolicy == ProjectionPolicy.NONE && nativeCRS != null) {
                resultCRS = nativeCRS;
            } else {
                resultCRS = getCRS(info.getSRS());
            }

            // make sure we create the appropriate schema, with the right crs
            // we checked above we are using DataStore/SimpleFeature/SimpleFeatureType (DSSFSFT)
            SimpleFeatureType schema = (SimpleFeatureType) getFeatureType(info);
            try {
                if (!CRS.equalsIgnoreMetadata(resultCRS, schema.getCoordinateReferenceSystem()))
                    schema = FeatureTypes.transform(schema, resultCRS);
            } catch (Exception e) {
                throw new DataSourceException(
                        "Problem forcing CRS onto feature type", e);
            }

            //joining, check for join hint which requires us to create a shcema with some additional
            // attributes
            if (hints != null && hints.containsKey(JOINS)) {
                List<Join> joins = (List<Join>) hints.get(JOINS);
                SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
                typeBuilder.init(schema);
                
                for (Join j : joins) {
                    String attName = j.getAlias() != null ? j.getAlias() : j.getTypeName();
                    typeBuilder.add(attName, SimpleFeature.class);
                }
                schema = typeBuilder.buildFeatureType();
            }

            //return a normal 
            fs = GeoServerFeatureLocking.create(fs, schema,
                    info.getFilter(), resultCRS, info.getProjectionPolicy().getCode());

            // versioning
            //
            VersioningPlugin verPlugin = GeoServerExtensions.bean(VersioningPlugin.class);
            if (verPlugin != null) {
                fs = verPlugin.wrap(fs, schema, info, resultCRS);
            }
            
            return fs;
        }
    }
    
    /**
     * Returns a coverage reader, caching the result.
     *  
     * @param info The coverage metadata.
     * @param hints Hints to use when loading the coverage, may be <code>null</code>.
     * 
     * @throws IOException Any errors that occur loading the reader.
     */
    @SuppressWarnings("deprecation")
    public GridCoverageReader getGridCoverageReader( CoverageStoreInfo info, Hints hints ) 
        throws IOException {
        
        final AbstractGridFormat gridFormat = info.getFormat();
        if(gridFormat == null) {
            throw new IOException("Could not find the raster plugin for format " + info.getType());
        }
        
        GridCoverageReader reader = null;
        Object key;
        if ( hints != null ) {
            // expand the hints if necessary
            final String formatName = gridFormat.getName();
            if (formatName.equalsIgnoreCase(IMAGE_MOSAIC) || formatName.equalsIgnoreCase(IMAGE_PYRAMID)){
                if (coverageExecutor != null){
                    if (hints != null){
                        hints.add(new RenderingHints(Hints.EXECUTOR_SERVICE, coverageExecutor));
                    } else {
                        hints = new Hints(new RenderingHints(Hints.EXECUTOR_SERVICE, coverageExecutor));
                    }
                }
            }
            
            key = new CoverageHintReaderKey(info, hints);
            reader = (GridCoverageReader) hintCoverageReaderCache.get( key );    
        } else {
            key = info;
            reader = (GridCoverageReader) coverageReaderCache.get( key );
        }
        
        if (reader != null) {
            return reader;
        }
        
        synchronized ( hints != null ? hintCoverageReaderCache : coverageReaderCache ) {
            if (hints != null) {
                reader = (GridCoverageReader) hintCoverageReaderCache.get(key);
            } else {
                reader = (GridCoverageReader) coverageReaderCache.get(key);
            }
            if (reader == null) {
                /////////////////////////////////////////////////////////
                //
                // Getting coverage reader using the format and the real path.
                //
                // /////////////////////////////////////////////////////////
                final File obj = GeoserverDataDirectory.findDataFile(info.getURL());
    
                reader = gridFormat.getReader(obj,hints);
                (hints != null ? hintCoverageReaderCache : coverageReaderCache ).put(key, reader);
            }
        }
        
        return reader;
            
    }
    
    /**
     * Clears any cached readers for the coverage.
     */
    public void clear(CoverageStoreInfo info) {
        coverageReaderCache.remove(info);
    }
    
    /**
     * Loads a grid coverage.
     * <p>
     * 
     * </p>
     * 
     * @param info The grid coverage metadata.
     * @param envelope The section of the coverage to load. 
     * @param hints Hints to use while loading the coverage.
     * 
     * @throws IOException Any errors that occur loading the coverage.
     */
    @SuppressWarnings("deprecation")
    public GridCoverage getGridCoverage( CoverageInfo info, ReferencedEnvelope env, Hints hints) throws IOException {
        final GridCoverageReader reader = getGridCoverageReader(info.getStore(), hints);
        if(reader == null) {
            return null;
        }
        
        return getGridCoverage(info, reader, env, hints);
    }
 
    /**
     * Loads a grid coverage.
     * <p>
     * 
     * </p>
     * 
     * @param info The grid coverage metadata.
     * @param envelope The section of the coverage to load. 
     * @param hints Hints to use while loading the coverage.
     * 
     * @throws IOException Any errors that occur loading the coverage.
     */
    @SuppressWarnings("deprecation")
    public GridCoverage getGridCoverage( CoverageInfo info, GridCoverageReader reader, ReferencedEnvelope env, Hints hints) 
        throws IOException {
        
        ReferencedEnvelope coverageBounds;
        try {
            coverageBounds = info.boundingBox();
        } 
        catch (Exception e) {
            throw (IOException) new IOException( "unable to calculate coverage bounds")
                .initCause( e );
        }
        
        GeneralEnvelope envelope = null;
        if (env == null) {
            envelope = new GeneralEnvelope( coverageBounds );
        }
        else {
            envelope = new GeneralEnvelope( env );
        }
    
        // /////////////////////////////////////////////////////////
        //
        // Do we need to proceed?
        // I need to check the requested envelope in order to see if the
        // coverage we ask intersect it otherwise it is pointless to load it
        // since its reader might return null;
        // /////////////////////////////////////////////////////////
        final CoordinateReferenceSystem sourceCRS = envelope.getCoordinateReferenceSystem();
        CoordinateReferenceSystem destCRS;
        try {
            destCRS = info.getCRS();
        } 
        catch (Exception e) {
			final IOException ioe= new IOException( "unable to determine coverage crs");
			ioe.initCause(e);
			throw ioe;
        }
        
        if (!CRS.equalsIgnoreMetadata(sourceCRS, destCRS)) {
            // get a math transform
            MathTransform transform;
			try {
				transform = CRS.findMathTransform(sourceCRS, destCRS,true);
			} catch (FactoryException e) {
				final IOException ioe= new IOException( "unable to determine coverage crs");
				ioe.initCause(e);
				throw ioe;
			}
        
            // transform the envelope
            if (!transform.isIdentity()) {
                try {
                    envelope = CRS.transform(transform, envelope);
                } 
                catch (TransformException e) {
                    throw (IOException) new IOException( "error occured transforming envelope").initCause( e );
                }
            }
        }
        
        // just do the intersection since
        envelope.intersect(coverageBounds);
        
        if (envelope.isEmpty()) {
            return null;
        }
        
        envelope.setCoordinateReferenceSystem(destCRS);
        
        // /////////////////////////////////////////////////////////
        //
        // Reading the coverage
        //
        // /////////////////////////////////////////////////////////
        
        GridCoverage gc  = reader.read(CoverageUtils.getParameters(
                    reader.getFormat().getReadParameters(), info.getParameters()));
        
        if ((gc == null) || !(gc instanceof GridCoverage2D)) {
            throw new IOException("The requested coverage could not be found.");
        }
        
        return gc;
    }

    /**
     * Returns the format for a coverage.
     * <p>
     * The format is inferred from {@link CoverageStoreInfo#getType()}
     * </p>
     * @param info The coverage metadata.
     * 
     * @return The format, or null.
     */
    @SuppressWarnings("deprecation")
    public AbstractGridFormat getGridCoverageFormat( CoverageStoreInfo info ) {
        final int length = CoverageStoreUtils.formats.length;

        for (int i = 0; i < length; i++) {
            if (CoverageStoreUtils.formats[i].getName().equals(info.getType())) {
                return (AbstractGridFormat) CoverageStoreUtils.formats[i];
            }
        }

        return null;
    }
    
    /**
     * Returns the {@link WebMapServer} for a {@link WMSStoreInfo}  object
     * @param info The WMS configuration
     * @throws IOException
     */
    public WebMapServer getWebMapServer(WMSStoreInfo info) throws IOException {
        try {
            String id = info.getId();
            WebMapServer wms = (WebMapServer) wmsCache.get(id);
            if (wms == null) {
                synchronized (wmsCache) {
                    wms = (WebMapServer) wmsCache.get(id);
                    if (wms == null) {
                        wms = new WebMapServer(new URL(info.getCapabilitiesURL()));

                        wmsCache.put(id, wms);
                    }
                }
            }

            return wms;
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            throw (IOException) new IOException().initCause(e);
        }
    }
    
    /**
     * Locates and returns a WMS {@link Layer} based on the configuration stored in WMSLayerInfo 
     * @param info
     * @return
     */
    public Layer getWMSLayer(WMSLayerInfo info) throws IOException {
     // check which actual name we have to use
        String name = info.getName();
        if (info.getNativeName() != null) {
            name = info.getNativeName();
        }

        WMSCapabilities caps = info.getStore().getWebMapServer(null).getCapabilities();
        for (Layer layer : caps.getLayerList()) {
            if (name.equals(layer.getName())) {
                return layer;
            }
        }

        throw new IOException("Could not find layer " + info.getName()
                + " in the server capabilitiles document");
        
    }
    
    /**
     * Clears the cached resource for a web map server
     */
    public void clear( WMSStoreInfo info ) {
        wmsCache.remove( info.getId() );
    }
    
    /**
     * Returns a style resource, caching the result.
     * <p>
     * The resource is loaded by parsing {@link StyleInfo#getFilename()} as an 
     * SLD.
     * </p>
     * @param info The style metadata.
     * 
     * @throws IOException Any parsing errors.
     */
    public Style getStyle( StyleInfo info ) throws IOException {
        Style style = styleCache.get( info );
        if ( style == null ) {
            synchronized (styleCache) {
                style = styleCache.get( info );
                if ( style == null ) {
                    
                    //JD: it is important that we call the SLDParser(File) constructor because
                    // if not the sourceURL will not be set which will mean it will fail to 
                    //resolve relative references to online resources
                    File styleFile = GeoserverDataDirectory.findStyleFile( info.getFilename() );
                    if ( styleFile == null ){
                        throw new IOException( "No such file: " + info.getFilename());
                    }
                    
                    style = Styles.style(Styles.parse(styleFile, info.getSLDVersion()));
                    
                    //set the name of the style to be the name of hte style metadata
                    // remove this when wms works off style info
                    style.setName( info.getName() );
                    styleCache.put( info, style );
                }
            }
        }
        
        return style;
    }
    
    /**
     * Clears a style resource from the cache.
     * 
     * @param info The style metadata.
     */
    public void clear(StyleInfo info) {
        styleCache.remove( info );
    }
    
    /**
     * Reads a raw style from persistence.
     *
     * @param style The configuration for the style. 
     * 
     * @return A reader for the style.
     */
    public BufferedReader readStyle( StyleInfo style ) throws IOException {
        File styleFile = GeoserverDataDirectory.findStyleFile(style.getFilename());
        if( styleFile == null ) {
            throw new IOException( "No such file: " + style.getFilename() );
        }
        return new BufferedReader( new InputStreamReader( new FileInputStream( styleFile ) ) );
        
    }
    
    /**
     * Serializes a style to configuration.
     * 
     * @param info The configuration for the style.
     * @param style The style object.
     * 
     */
    public void writeStyle( StyleInfo info, Style style ) throws IOException {
        writeStyle(info,style,false);
    }
    
    /**
     * Serializes a style to configuration optionally formatting the style when writing it.
     * 
     * @param info The configuration for the style.
     * @param style The style object.
     * @param format Whether to format the style
     */
    public void writeStyle( StyleInfo info, Style style, boolean format) throws IOException {
        synchronized ( styleCache ) {
            File styleFile = GeoserverDataDirectory.findStyleFile( info.getFilename(), true );
            BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( styleFile ) );
            
            try {
                Styles.encode(Styles.sld(style), info.getSLDVersion(), format, out);
                clear(info);
            }
            finally {
                out.close();
            }
        }
    }
    
    /**
     * Writes a raw style to configuration.
     * 
     * @param style The configuration for the style.
     * @param in input stream representing the raw a style.
     * 
     */
    public void writeStyle( StyleInfo style, InputStream in ) throws IOException {
        synchronized ( styleCache ) {
            File styleFile = GeoserverDataDirectory.findStyleFile( style.getFilename(), true );
            BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( styleFile ) );
            
            try {
                IOUtils.copy( in, out );
                out.flush();
                
                clear(style);
            }
            finally {
                out.close();
            }
        }
    }
    
    /**
     * Deletes a style from the configuration.
     * 
     * @param style The configuration for the style.
     * @param purge Whether to delete the file from disk.
     * 
     */
    public void deleteStyle( StyleInfo style, boolean purgeFile ) throws IOException {
        synchronized ( styleCache ) {
           
            if( purgeFile ){
                File styleFile = GeoserverDataDirectory.findStyleFile( style.getFilename(), true );
                if( styleFile.exists() ){
                    styleFile.delete();
                }
            }
        }
    }
    
    
    /**
     * Disposes all cached resources.
     *
     */
    public void dispose() {
        crsCache.clear();
        dataStoreCache.clear();
        featureTypeCache.clear();
        featureTypeAttributeCache.clear();
        coverageReaderCache.clear();
        hintCoverageReaderCache.clear();
        wmsCache.clear();
        styleCache.clear();
        listeners.clear();
    }
    
    class FeatureTypeCache extends LRUMap {
        
        public FeatureTypeCache(int maxSize) {
            super(maxSize);
        }
        
        protected boolean removeLRU(LinkEntry entry) {
            String id = (String) entry.getKey();
            FeatureType featureType = (FeatureType) entry.getValue();
            FeatureTypeInfo info = catalog.getFeatureType(id);
            LOGGER.info( "Disposing feature type '" + info.getName() + "'");
            
            fireDisposed(info, featureType);
            return super.removeLRU(entry);
        }
    }
    
    class DataStoreCache extends LRUMap {
        protected boolean removeLRU(LinkEntry entry) {
            String name = (String) entry.getKey();
            dispose(name,(DataAccess) entry.getValue());
            
            return super.removeLRU(entry);
        }
        
        void dispose(String id, DataAccess dataStore) {
            DataStoreInfo info = catalog.getDataStore(id);
            String name = info != null ? info.getName() : id;
            LOGGER.info( "Disposing datastore '" + name + "'" );
            
            fireDisposed(info, dataStore);
            
            try {
                dataStore.dispose();
            }
            catch( Exception e ) {
                LOGGER.warning( "Error occured disposing datastore '" + name + "'");
                LOGGER.log(Level.FINE, "", e );
            }
            
        }
        
        protected void destroyEntry(HashEntry entry) {
            dispose( (String) entry.getKey(), (DataAccess) entry.getValue() );
            super.destroyEntry(entry);
        }
        
        public void clear() {
            for ( Iterator e = entrySet().iterator(); e.hasNext(); ) {
                Map.Entry<String,DataAccess<? extends FeatureType, ? extends Feature>> entry = 
                    (Entry<String, DataAccess<? extends FeatureType, ? extends Feature>>) e.next();
                dispose( entry.getKey(), entry.getValue() );
            }
            super.clear();
        }
    }
    
    class CoverageReaderCache extends LRUMap {
        protected boolean removeLRU(LinkEntry entry) {
            CoverageStoreInfo info = (CoverageStoreInfo) entry.getKey();
            dispose( info, (GridCoverageReader) entry.getValue() );
            return super.removeLRU(entry);
        }
        
        void dispose( CoverageStoreInfo info, GridCoverageReader reader ) {
            LOGGER.info( "Disposing grid coverage reader '" + info.getName() + "'");
            fireDisposed(info, reader);
            try {
                reader.dispose();
            }
            catch( Exception e ) {
                LOGGER.warning( "Error occured disposing coverage reader '" + info.getName() + "'");
                LOGGER.log(Level.FINE, "", e );
            }
        }
        
        protected void destroyEntry(HashEntry entry) {
            dispose( (CoverageStoreInfo) entry.getKey(), (GridCoverageReader) entry.getValue() );
            super.destroyEntry(entry);
        }
        
        public void clear() {
            for ( Iterator e = entrySet().iterator(); e.hasNext(); ) {
                Map.Entry<CoverageStoreInfo,GridCoverageReader> entry = 
                    (Entry<CoverageStoreInfo, GridCoverageReader>) e.next();
                dispose( entry.getKey(), entry.getValue() );
            }
            super.clear();
        }
    }
    
    class CoverageHintReaderCache extends LRUMap {
        
        protected boolean removeLRU(LinkEntry entry) {
            CoverageHintReaderKey key = (CoverageHintReaderKey) entry.getKey();
            dispose( key.info, (GridCoverageReader) entry.getValue() );
            return super.removeLRU(entry);
        }
        
        void dispose( CoverageStoreInfo info, GridCoverageReader reader ) {
            LOGGER.info( "Disposing grid coverage reader '" + info.getName() + "'");
            fireDisposed(info, reader);
            try {
                reader.dispose();
            }
            catch( Exception e ) {
                LOGGER.warning( "Error occured disposing coverage reader '" + info.getName() + "'");
                LOGGER.log(Level.FINE, "", e );
            }
        }
        
        protected void destroyEntry(HashEntry entry) {
            CoverageHintReaderKey key = (CoverageHintReaderKey) entry.getKey();
            dispose( key.info, (GridCoverageReader) entry.getValue() );
            super.destroyEntry(entry);
        }
        
        public void clear() {
            for ( Iterator e = entrySet().iterator(); e.hasNext(); ) {
                Map.Entry<CoverageHintReaderKey,GridCoverageReader> entry = 
                    (Entry<CoverageHintReaderKey, GridCoverageReader>) e.next();
                dispose( entry.getKey().info, entry.getValue() );
            }
            super.clear();
        }
        
    }
    
    /**
     * The key in the {@link CoverageHintReaderCache}
     * 
     * @author Andrea Aime - GeoSolutions
     */
    static class CoverageHintReaderKey {
        CoverageStoreInfo info;
        Hints hints;
        
        public CoverageHintReaderKey(CoverageStoreInfo info, Hints hints) {
            this.info = info;
            this.hints = hints;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((hints == null) ? 0 : hints.hashCode());
            result = prime * result + ((info == null) ? 0 : info.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CoverageHintReaderKey other = (CoverageHintReaderKey) obj;
            if (hints == null) {
                if (other.hints != null)
                    return false;
            } else if (!hints.equals(other.hints))
                return false;
            if (info == null) {
                if (other.info != null)
                    return false;
            } else if (!info.equals(other.info))
                return false;
            return true;
        }
        
    }
    
    static class FeatureTypeAttributeCache extends LRUMap {
        FeatureTypeAttributeCache(int size) {
            super(size);
        }
    }
    
    static class WMSCache extends LRUMap {
        
    }
    
    /**
     * Listens to catalog events clearing cache entires when resources are modified.
     */
    class CacheClearingListener extends CatalogVisitorAdapter implements CatalogListener {

        public void handleAddEvent(CatalogAddEvent event) {
        }

        public void handleModifyEvent(CatalogModifyEvent event) {
        }

        public void handlePostModifyEvent(CatalogPostModifyEvent event) {
            event.getSource().accept( this );
        }

        public void handleRemoveEvent(CatalogRemoveEvent event) {
            event.getSource().accept( this );
        }

        public void reloaded() {
        }
       
        @Override
        public void visit(DataStoreInfo dataStore) {
            clear(dataStore);
        }
        @Override
        public void visit(CoverageStoreInfo coverageStore) {
            clear(coverageStore);
        }
        @Override
        public void visit(FeatureTypeInfo featureType) {
            clear(featureType);
        }

        @Override
        public void visit(WMSStoreInfo wmsStore) {
            clear(wmsStore);
        }

        @Override
        public void visit(StyleInfo style) {
            clear(style);
        }
    }
    
    void fireDisposed(DataStoreInfo dataStore, DataAccess da) {
        for (Listener l : listeners) {
            try {
                l.disposed(dataStore, da);
            }
            catch(Throwable t) {
                LOGGER.warning("Resource pool listener threw error");
                LOGGER.log(Level.INFO, t.getLocalizedMessage(), t);
            }
        }
    }
    
    void fireDisposed(FeatureTypeInfo featureType, FeatureType ft) {
        for (Listener l : listeners) {
            try {
                l.disposed(featureType, ft);
            }
            catch(Throwable t) {
                LOGGER.warning("Resource pool listener threw error");
                LOGGER.log(Level.INFO, t.getLocalizedMessage(), t);
            }
        }
    }
    
    void fireDisposed(CoverageStoreInfo coverageStore, GridCoverageReader gcr) {
        for (Listener l : listeners) {
            try {
                l.disposed(coverageStore, gcr);
            }
            catch(Throwable t) {
                LOGGER.warning("Resource pool listener threw error");
                LOGGER.log(Level.INFO, t.getLocalizedMessage(), t);
            }
        }
    }
    
    /**
     * Listener for resource pool events.
     * 
     * @author Justin Deoliveira, OpenGeo
     *
     */
    public static interface Listener {
        
        /**
         * Event fired when a data store is evicted from the resource pool.
         */
        void disposed(DataStoreInfo dataStore, DataAccess da);

        /**
         * Event fired when a coverage store is evicted from the resource pool.
         */
        void disposed(CoverageStoreInfo coverageStore, GridCoverageReader gcr);

        /**
         * Event fired when a feature type is evicted from the resource pool. 
         */
        void disposed(FeatureTypeInfo featureType, FeatureType ft);
    }

    
    
}
