/* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gss.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.gss.impl.GSS;
import org.geoserver.web.GeoServerApplication;
import org.opengis.feature.type.Name;

public class GSSLayerDetachableModel extends LoadableDetachableModel<GSSLayerInfo> {

    private static final long serialVersionUID = 1L;

    private Name typeName;

    public GSSLayerDetachableModel(GSSLayerInfo gSSLayerInfo) {
        this.typeName = gSSLayerInfo.getName();
    }

    @Override
    protected GSSLayerInfo load() {
        Catalog catalog = GeoServerApplication.get().getCatalog();
        FeatureTypeInfo featureType = catalog.getFeatureTypeByName(typeName);
        return load(featureType);
    }

    private static GSSLayerInfo load(FeatureTypeInfo featureType) {
        final Name featureTypeName = featureType.getQualifiedName();
        final GSS gss = GSS.get();
        final boolean published = gss.isReplicated(featureTypeName);
        GSSLayerInfo gssLayerInfo = new GSSLayerInfo(featureType);
        gssLayerInfo.setPublished(published);
        return gssLayerInfo;
    }

    public static List<GSSLayerInfo> getItems() {
        List<Name> syncedNames;
        try {
            syncedNames = GSS.get().listLayers();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return getItems(syncedNames);
    }

    public static List<GSSLayerInfo> getItems(final List<Name> typeNames) {
        Catalog catalog = GeoServerApplication.get().getCatalog();
        List<GSSLayerInfo> types = new ArrayList<GSSLayerInfo>();
        for (Name typeName : typeNames) {
            FeatureTypeInfo featureType = catalog.getFeatureTypeByName(typeName);
            GSSLayerInfo gssLayerInfo = load(featureType);
            types.add(gssLayerInfo);
        }
        return types;
    }

}