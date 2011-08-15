package org.geogit.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevTree;
import org.geogit.storage.FeatureWriter;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.PropertyName;
import org.opengis.util.ProgressListener;
import org.springframework.util.Assert;

/**
 * A working tree is the collection of Features for a single FeatureType in GeoServer that has a
 * repository associated with it (and hence is subject of synchronization).
 * <p>
 * It represents the set of Features tracked by some kind of geospatial data repository (like the
 * GeoServer Catalog). It is essentially a "tree" with various roots and only one level of nesting,
 * since the FeatureTypes held in this working tree are the equivalents of files in a git working
 * tree.
 * </p>
 * <p>
 * <ul>
 * <li>A WorkingTree represents the current working copy of the versioned feature types
 * <li>A WorkingTree has a Repository
 * <li>A Repository holds commits and branches
 * <li>You perform work on the working tree (insert/delete/update features)
 * <li>Then you commit to the current Repository's branch
 * <li>You can checkout a different branch from the Repository and the working tree will be updated
 * to reflect the state of that branch
 * </ul>
 * 
 * @author Gabriel Roldan
 * @see Repository
 */
@SuppressWarnings("rawtypes")
public class WorkingTree {

    private final Index index;

    private final Repository repository;

    public WorkingTree(final Repository repository) {
        Assert.notNull(repository);
        this.repository = repository;
        this.index = repository.getIndex();
        Assert.notNull(index);
    }

    public void init(final FeatureType featureType) throws Exception {

        final Name typeName = featureType.getName();
        index.created(Arrays.asList(typeName.getNamespaceURI(), typeName.getLocalPart()));
    }

    public void delete(final Name typeName) throws Exception {
        index.deleted(typeName.getNamespaceURI(), typeName.getLocalPart());
    }

    /**
     * @param typeName
     * @param features
     * @param listener
     * @return the list of feature ids inserted, or {@code null} if
     *         {@link ProgressListener#isCanceled() listener.isCanceled()}
     * @throws Exception
     */
    public List<String> insert(final Name typeName, final FeatureCollection features,
            final ProgressListener listener) throws Exception {

        List<String> fids = new ArrayList<String>();
        final String nsUri = typeName.getNamespaceURI();
        final String localPart = typeName.getLocalPart();
        final float size = features.size();

        long t = System.currentTimeMillis();
        // be careful to preserve feature ids. MemoryDataStore does, but when changing it by
        // something production ready....
        FeatureIterator iterator = features.features();
        try {
            repository.beginTransaction();
            int count = 0;
            while (iterator.hasNext()) {
                if (listener.isCanceled()) {
                    repository.rollbackTransaction();
                    return null;
                }
                Feature next = iterator.next();
                String id = next.getIdentifier().getID();
                FeatureWriter persister = new FeatureWriter(next);
                index.inserted(persister, nsUri, localPart, id);
                fids.add(id);
                count++;
                if (listener.isCanceled()) {
                    repository.rollbackTransaction();
                    return null;
                }
                if (size > 0) {
                    listener.progress((count * 100) / size);
                }
            }
            repository.commitTransaction();
            listener.complete();
        } catch (Exception e) {
            repository.rollbackTransaction();
            throw e;
        } finally {
            iterator.close();
        }
        // t = System.currentTimeMillis() - t;
        // System.err.println("Imported " + size + " features from " + typeName.getLocalPart()
        // + " in " + t + "ms");
        return fids;
    }

    public void update(final Name typeName, Filter filter,
            final List<PropertyName> updatedProperties, List<Object> newValues2,
            final FeatureCollection newValues, final ProgressListener listener) throws Exception {

        insert(typeName, newValues, listener);
    }

    public boolean hasRoot(final Name typeName) {
        String namespaceURI = typeName.getNamespaceURI() == null ? "" : typeName.getNamespaceURI();
        String localPart = typeName.getLocalPart();
        ObjectId typeNameTreeId = repository.getTreeChildId(namespaceURI, localPart);
        return typeNameTreeId != null;
    }

    public void delete(Name typeName, Filter filter, FeatureCollection affectedFeatures) {
        final Index index = repository.getIndex();
        String namespaceURI = typeName.getNamespaceURI();
        String localPart = typeName.getLocalPart();
        FeatureIterator iterator = affectedFeatures.features();
        try {
            while (iterator.hasNext()) {
                String id = iterator.next().getIdentifier().getID();
                index.deleted(namespaceURI, localPart, id);
            }
        } finally {
            iterator.close();
        }
    }

    /**
     * @return
     */
    public List<Name> getFeatureTypeNames() {
        List<Name> names = new ArrayList<Name>();
        RevTree root = repository.getRootTree();
        if (root != null) {
            Iterator<Ref> namespaces = root.iterator(null);
            while (namespaces.hasNext()) {
                final Ref nsRef = namespaces.next();
                final String nsUri = nsRef.getName();
                final ObjectId nsTreeId = nsRef.getObjectId();
                final RevTree nsTree = repository.getTree(nsTreeId);
                final Iterator<Ref> typeNameRefs = nsTree.iterator(null);
                while (typeNameRefs.hasNext()) {
                    Name typeName = new NameImpl(nsUri, typeNameRefs.next().getName());
                    names.add(typeName);
                }
            }
        }
        return names;
    }
}
