package org.geoserver.data.geogit;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevTree;
import org.geogit.api.SpatialRef;
import org.geogit.storage.FeatureReader;
import org.geogit.storage.ObjectDatabase;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.spatial.BBOX;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.ProgressListener;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;

public class GeoGitSimpleFeatureCollection implements SimpleFeatureCollection {

    private final SimpleFeatureType type;

    private final Filter filter;

    private final ObjectDatabase odb;

    private final ObjectId rootTreeId;

    public GeoGitSimpleFeatureCollection(final SimpleFeatureType type, final Filter filter,
            final ObjectDatabase odb, final ObjectId rootTreeId) {
        this.type = type;
        this.filter = filter;
        this.odb = odb;
        this.rootTreeId = rootTreeId;
    }

    /**
     * @see org.geotools.feature.FeatureCollection#getSchema()
     */
    @Override
    public SimpleFeatureType getSchema() {
        return type;
    }

    /**
     * @see org.geotools.feature.FeatureCollection#getID()
     */
    @Override
    public String getID() {
        return null;
    }

    /**
     * @see org.geotools.feature.FeatureCollection#purge()
     */
    @Override
    public void purge() {
    }

    /**
     * @return the tree that's the root of this feature type features, or an empty tree if such tree
     *         does not exist as child of the root one given in this class' constructor.
     */
    private RevTree getTypeTree() {
        RevTree root = odb.getTree(rootTreeId);
        Name typeName = type.getName();
        Ref typeTreeRef = odb.getTreeChild(root, getTypePath(typeName));
        RevTree typeTree;
        if (null == typeTreeRef) {
            typeTree = odb.newTree();
        } else {
            typeTree = odb.getTree(typeTreeRef.getObjectId());
        }
        return typeTree;
    }

    /**
     * @see org.geotools.feature.FeatureCollection#getBounds()
     */
    @Override
    public ReferencedEnvelope getBounds() {
        final RevTree typeTree = getTypeTree();
        final CoordinateReferenceSystem crs = type.getCoordinateReferenceSystem();
        ReferencedEnvelope bounds = new ReferencedEnvelope(crs);

        if (BigInteger.ZERO.equals(typeTree.size())) {
            return bounds;
        }

        final FeatureRefIterator refs = new FeatureRefIterator(typeTree, filter);

        BoundingBox featureBounds;
        if (refs.isFullySupported()) {
            while (refs.hasNext()) {
                Ref ref = refs.next();
                if (ref instanceof SpatialRef) {
                    SpatialRef sp = (SpatialRef) ref;
                    featureBounds = sp.getBounds();
                    expandToInclude(bounds, featureBounds);
                }
            }
        } else {
            Iterator<SimpleFeature> features = new GeoGitFeatureIterator(refs, type, filter, odb);
            while (features.hasNext()) {
                featureBounds = features.next().getBounds();
                expandToInclude(bounds, featureBounds);
            }
        }

        return bounds;
    }

    private void expandToInclude(ReferencedEnvelope bounds, BoundingBox featureBounds) {
        final CoordinateReferenceSystem crs = type.getCoordinateReferenceSystem();
        final CoordinateReferenceSystem featureCrs = featureBounds.getCoordinateReferenceSystem();
        if (!CRS.equalsIgnoreMetadata(crs, featureCrs)) {
            try {
                featureBounds = featureBounds.toBounds(crs);
            } catch (TransformException e) {
                Throwables.propagate(e);
            }
        }
        bounds.include(featureBounds);
    }

    /**
     * @see org.geotools.feature.FeatureCollection#size()
     */
    @Override
    public int size() {
        final RevTree typeTree = getTypeTree();

        if (Filter.INCLUDE.equals(filter)) {
            final BigInteger size = typeTree.size();
            return size.intValue();
        }

        final FeatureRefIterator refs = new FeatureRefIterator(typeTree, filter);
        int size;
        if (refs.isFullySupported()) {
            size = Iterators.size(refs);
        } else {
            Iterator<SimpleFeature> features = new GeoGitFeatureIterator(refs, type, filter, odb);
            size = Iterators.size(features);
        }

        return size;
    }

    private static List<String> getTypePath(final Name name) {
        String namespaceURI = name.getNamespaceURI();
        if (null == namespaceURI) {
            namespaceURI = "";
        }
        String localPart = name.getLocalPart();

        return Arrays.asList(namespaceURI, localPart);
    }

    /**
     * @see org.geotools.feature.FeatureCollection#iterator()
     */
    @Override
    public Iterator<SimpleFeature> iterator() {
        final RevTree typeTree = getTypeTree();

        final FeatureRefIterator refs = new FeatureRefIterator(typeTree, filter);
        Iterator<SimpleFeature> features = new GeoGitFeatureIterator(refs, type, filter, odb);
        return features;
    }

    /**
     * @see org.geotools.data.simple.SimpleFeatureCollection#features()
     * @see #iterator()
     */
    @Override
    public SimpleFeatureIterator features() {
        final Iterator<SimpleFeature> iterator = iterator();
        return new SimpleFeatureIterator() {

            @Override
            public SimpleFeature next() throws NoSuchElementException {
                return iterator.next();
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public void close() {
                // nothing to do?
            }
        };
    }

    /**
     * @author groldan
     * 
     */
    private static class BBOXPredicate implements Predicate<Ref> {

        private final BBOX filter;

        public BBOXPredicate(final BBOX filter) {
            this.filter = filter;
        }

        @Override
        public boolean apply(final Ref featureRef) {
            if (!(featureRef instanceof SpatialRef)) {
                return false;
            }
            SpatialRef sr = (SpatialRef) featureRef;
            BoundingBox bounds = sr.getBounds();
            final boolean apply = filter.evaluate(bounds);
            return apply;
        }
    }

    /**
     * @author groldan
     * 
     */
    private static class FeatureRefIterator extends AbstractIterator<Ref> {

        private final Iterator<Ref> refs;

        private final Filter filter;

        public FeatureRefIterator(final RevTree typeTree, final Filter filter) {
            this.filter = filter;

            if (Filter.INCLUDE.equals(filter)) {
                refs = typeTree.iterator(null);
                return;
            }
            if (filter instanceof BBOX) {
                refs = typeTree.iterator(new BBOXPredicate((BBOX) filter));
                return;
            }

            // can't optimize here
            refs = typeTree.iterator(null);
        }

        public boolean isFullySupported() {
            return Filter.INCLUDE.equals(filter) || filter instanceof BBOX;
        }

        @Override
        protected Ref computeNext() {
            if (!refs.hasNext()) {
                return endOfData();
            }
            return refs.next();
        }

    }

    private static class GeoGitFeatureIterator extends AbstractIterator<SimpleFeature> {

        private final Iterator<Ref> featureRefs;

        private final SimpleFeatureType type;

        private final Filter filter;

        private final ObjectDatabase odb;

        public GeoGitFeatureIterator(final Iterator<Ref> featureRefs, final SimpleFeatureType type,
                final Filter filter, final ObjectDatabase odb) {
            this.featureRefs = featureRefs;
            this.type = type;
            this.filter = filter;
            this.odb = odb;
        }

        @Override
        protected SimpleFeature computeNext() {
            try {
                while (featureRefs.hasNext()) {
                    Ref featureRef = featureRefs.next();
                    String featureId = featureRef.getName();
                    ObjectId contentId = featureRef.getObjectId();
                    Feature feature;
                    feature = odb.get(contentId, new FeatureReader(type, featureId));
                    if (filter.evaluate(feature)) {
                        return (SimpleFeature) feature;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return endOfData();
        }

    }

    @Override
    public void accepts(FeatureVisitor visitor, ProgressListener progress) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void close(FeatureIterator<SimpleFeature> close) {
        // TODO Auto-generated method stub

    }

    @Override
    public void close(Iterator<SimpleFeature> close) {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.geotools.feature.FeatureCollection#addListener(org.geotools.feature.CollectionListener)
     */
    @Override
    public void addListener(CollectionListener listener) throws NullPointerException {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.geotools.feature.FeatureCollection#removeListener(org.geotools.feature.CollectionListener)
     */
    @Override
    public void removeListener(CollectionListener listener) throws NullPointerException {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.geotools.feature.FeatureCollection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean add(SimpleFeature obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends SimpleFeature> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(
            FeatureCollection<? extends SimpleFeatureType, ? extends SimpleFeature> resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <O> O[] toArray(O[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleFeatureCollection subCollection(Filter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleFeatureCollection sort(SortBy order) {
        throw new UnsupportedOperationException();
    }

}
