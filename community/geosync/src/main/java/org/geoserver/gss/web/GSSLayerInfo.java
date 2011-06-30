/* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gss.web;

import java.io.IOException;
import java.io.Serializable;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;

public class GSSLayerInfo implements Comparable<GSSLayerInfo>, Serializable {

    private static final long serialVersionUID = 7737632010950106656L;

    public static final String GSS_PUBLISHED_METADATA_KEY = "gss.published";

    private final String ns;

    private final String name;

    private boolean published;

    private boolean readOnly;

    private Class<?> geometryType;

    @SuppressWarnings("rawtypes")
    public GSSLayerInfo(final FeatureTypeInfo featureType) {
        final Name qualifiedName = featureType.getQualifiedName();
        this.ns = qualifiedName.getNamespaceURI();
        this.name = qualifiedName.getLocalPart();

        GeometryDescriptor geometryDescriptor;
        try {
            geometryDescriptor = featureType.getFeatureType().getGeometryDescriptor();
            FeatureSource featureSource = featureType.getFeatureSource(null, null);
            this.readOnly = !(featureSource instanceof FeatureStore);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (geometryDescriptor != null) {
            this.geometryType = geometryDescriptor.getType().getBinding();
        }
    }

    public Name getName() {
        return new NameImpl(ns, name);
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    /**
     * Read only feature types (those that don't resolve to a {@link FeatureStore}) can't be
     * versioned.
     * 
     * @return whether it's a read only FeatureType
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * @param readOnly
     *            the readOnly to set
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * @return the geometryType
     */
    public Class<?> getGeometryType() {
        return geometryType;
    }

    public int compareTo(GSSLayerInfo o) {
        // unpublished resources first
        if (published && !o.published)
            return -1;
        else if (!published && o.published)
            return 1;
        // the compare by local name, as it's unlikely the users will see the
        // namespace URI (and the prefix is not available in Name)
        return name.compareTo(o.name);
    }

    public String toString() {
        return getClass().getSimpleName() + "[" + name + "]";
    }
}