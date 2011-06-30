/* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gss.wfsbridge;

import org.geoserver.catalog.CatalogException;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.MetadataMap;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.WMSLayerInfo;
import org.geoserver.catalog.event.CatalogAddEvent;
import org.geoserver.catalog.event.CatalogListener;
import org.geoserver.catalog.event.CatalogModifyEvent;
import org.geoserver.catalog.event.CatalogPostModifyEvent;
import org.geoserver.catalog.event.CatalogRemoveEvent;

/**
 * Listens for modifications to {@link ResourceInfo} (actually, only {@link FeatureTypeInfo}, as
 * {@link CoverageInfo} and {@link WMSLayerInfo} are not subject of synchronization), and updates
 * the transaction index accordingly.
 * 
 * @author Gabriel Roldan
 * 
 */
public class GSSCatalogListener implements CatalogListener {

    /**
     * 
     * @see org.geoserver.catalog.event.CatalogListener#handleAddEvent(org.geoserver.catalog.event.CatalogAddEvent)
     */
    public void handleAddEvent(final CatalogAddEvent event) throws CatalogException {
        if (!(event.getSource() instanceof FeatureTypeInfo)) {
            return;
        }
        final FeatureTypeInfo newFT = (FeatureTypeInfo) event.getSource();
        final MetadataMap metadata = newFT.getMetadata();
        final Boolean synchronizeFeatureType;
        synchronizeFeatureType = Boolean.valueOf(metadata.get("GSS.Synchronize", String.class));
        if (synchronizeFeatureType.booleanValue()) {
            // TODO: register feature type for synchronization
        }

    }

    /**
     * @see org.geoserver.catalog.event.CatalogListener#handleRemoveEvent(org.geoserver.catalog.event.CatalogRemoveEvent)
     */
    public void handleRemoveEvent(CatalogRemoveEvent event) throws CatalogException {
        if (!(event.getSource() instanceof FeatureTypeInfo)) {
            return;
        }
        final FeatureTypeInfo deletedFT = (FeatureTypeInfo) event.getSource();
        final MetadataMap metadata = deletedFT.getMetadata();
        final Boolean isSynchronizeFeatureType;
        isSynchronizeFeatureType = Boolean.valueOf(metadata.get("GSS.Synchronize", String.class));
        if (isSynchronizeFeatureType.booleanValue()) {
            // TODO: record delete
        }
    }

    /**
     * Does nothing, only interested in {@link #handlePostModifyEvent}.
     * 
     * @see org.geoserver.catalog.event.CatalogListener#handleModifyEvent(org.geoserver.catalog.event.CatalogModifyEvent)
     */
    public void handleModifyEvent(CatalogModifyEvent event) throws CatalogException {
        // I know it's empty. It's on purpose.
        // but it should actually record the previous structure of the feature type so that
        // handlePostModify catched up the difference?
    }

    /**
     * @see org.geoserver.catalog.event.CatalogListener#handlePostModifyEvent(org.geoserver.catalog.event.CatalogPostModifyEvent)
     */
    public void handlePostModifyEvent(CatalogPostModifyEvent event) throws CatalogException {
        if (!(event.getSource() instanceof FeatureTypeInfo)) {
            return;
        }
        final FeatureTypeInfo ftInfo = (FeatureTypeInfo) event.getSource();
    }

    /**
     * @see org.geoserver.catalog.event.CatalogListener#reloaded()
     */
    public void reloaded() {
        // not sure what to do?
    }

}
