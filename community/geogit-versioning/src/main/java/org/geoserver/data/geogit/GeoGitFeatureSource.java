package org.geoserver.data.geogit;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.geogit.api.GeoGIT;
import org.geogit.api.ObjectId;
import org.geogit.api.RevCommit;
import org.geogit.repository.Repository;
import org.geogit.storage.ObjectDatabase;
import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultResourceInfo;
import org.geotools.data.FeatureListener;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.EmptyFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.spatial.DefaultCRSFilterVisitor;
import org.geotools.filter.spatial.ReprojectingFilterVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.google.common.base.Throwables;

public class GeoGitFeatureSource implements SimpleFeatureSource {

    protected final SimpleFeatureType type;

    protected final GeoGitDataStore dataStore;

    public GeoGitFeatureSource(final SimpleFeatureType type, final GeoGitDataStore dataStore) {
        this.type = type;
        this.dataStore = dataStore;
    }

    /**
     * @return the object id of the current HEAD's commit
     */
    public ObjectId getCurrentVersion() {
        // assume HEAD is at MASTER
        try {
            Repository repository = dataStore.getRepository();
            Iterator<RevCommit> lastCommit = new GeoGIT(repository).log().setLimit(1).call();
            if (lastCommit.hasNext()) {
                return lastCommit.next().getId();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * @see org.geotools.data.FeatureSource#getName()
     */
    @Override
    public Name getName() {
        return type.getName();
    }

    /**
     * @see org.geotools.data.FeatureSource#getInfo()
     */
    @Override
    public ResourceInfo getInfo() {
        DefaultResourceInfo info = new DefaultResourceInfo();
        ReferencedEnvelope bounds;
        try {
            bounds = getBounds();
            if (bounds != null) {
                info.setBounds(bounds);
                info.setCRS(bounds.getCoordinateReferenceSystem());
            }
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        info.setName(getName().getLocalPart());
        info.setDescription("GeoGit backed Feature Source");
        return info;
    }

    /**
     * @see org.geotools.data.FeatureSource#getDataStore()
     */
    @Override
    public GeoGitDataStore getDataStore() {
        return dataStore;
    }

    /**
     * @see org.geotools.data.FeatureSource#getQueryCapabilities()
     */
    @Override
    public QueryCapabilities getQueryCapabilities() {
        QueryCapabilities caps = new QueryCapabilities() {
            /**
             * @see org.geotools.data.QueryCapabilities#isUseProvidedFIDSupported()
             * @return {@code true}
             */
            @Override
            public boolean isUseProvidedFIDSupported() {
                return true;
            }

            /**
             * @see org.geotools.data.QueryCapabilities#supportsSorting(org.opengis.filter.sort.SortBy[])
             * @return {@code false}
             */
            @Override
            public boolean supportsSorting(SortBy[] sortAttributes) {
                return false;
            }
        };

        return caps;
    }

    @Override
    public void addFeatureListener(FeatureListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeFeatureListener(FeatureListener listener) {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.geotools.data.FeatureSource#getSchema()
     */
    @Override
    public SimpleFeatureType getSchema() {
        return type;
    }

    /**
     * @see org.geotools.data.FeatureSource#getBounds()
     * @see #getBounds(Query) getBounds(Query.ALL)
     */
    @Override
    public ReferencedEnvelope getBounds() throws IOException {
        return getBounds(Query.ALL);
    }

    /**
     * @see org.geotools.data.FeatureSource#getBounds(org.geotools.data.Query)
     */
    @Override
    public ReferencedEnvelope getBounds(final Query query) throws IOException {
        // TODO optimize, please
        SimpleFeatureCollection features = getFeatures(query);
        ReferencedEnvelope bounds = features.getBounds();
        return bounds;
    }

    @Override
    public int getCount(Query query) throws IOException {
        // TODO optimize, please
        SimpleFeatureCollection features = getFeatures(query);
        int size = features.size();
        return size;
    }

    /**
     * @see org.geotools.data.FeatureSource#getSupportedHints()
     */
    @Override
    public Set<Key> getSupportedHints() {
        return Collections.emptySet();
    }

    /**
     * @see org.geotools.data.simple.SimpleFeatureSource#getFeatures()
     */
    @Override
    public SimpleFeatureCollection getFeatures() throws IOException {
        return getFeatures(Query.ALL);
    }

    @Override
    public SimpleFeatureCollection getFeatures(final Filter filter) throws IOException {
        return getFeatures(new Query(getName().getLocalPart(), filter));
    }

    @Override
    public SimpleFeatureCollection getFeatures(final Query query) throws IOException {
        Filter filter = query.getFilter();
        if (filter == null) {
            filter = Filter.INCLUDE;
        }
        if (Filter.EXCLUDE.equals(filter)) {
            return new EmptyFeatureCollection(type);
        }

        Query query2 = reprojectFilter(query);
        filter = query2.getFilter();

        final ObjectDatabase objectDatabase = dataStore.getRepository().getIndex().getDatabase();
        final ObjectId rootTreeId = getRootTreeId();
        return new GeoGitSimpleFeatureCollection(type, filter, objectDatabase, rootTreeId);
    }

    /**
     * @return the id of the root tree. Defaults to the repository's root, but
     *         {@link GeoGitFeatureStore} shall override to account for whether there's a
     *         transaction in progress
     */
    protected ObjectId getRootTreeId() {
        Repository repository = dataStore.getRepository();
        ObjectId rootTreeId = repository.getRootTreeId();
        return rootTreeId;
    }

    private Query reprojectFilter(Query query) throws IOException {
        Filter filter = query.getFilter() != null ? query.getFilter() : Filter.INCLUDE;
        if (Filter.INCLUDE.equals(filter)) {
            return query;
        }

        final SimpleFeatureType nativeFeatureType = getSchema();
        final GeometryDescriptor geom = nativeFeatureType.getGeometryDescriptor();
        // if no geometry involved, no reprojection needed
        if (geom == null) {
            return query;
        }

        final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

        try {
            CoordinateReferenceSystem nativeCRS = geom.getCoordinateReferenceSystem();

            // now we apply a default to all geometries and bbox in the filter
            DefaultCRSFilterVisitor defaultCRSVisitor = new DefaultCRSFilterVisitor(ff, nativeCRS);
            Filter defaultedFilter = (Filter) filter.accept(defaultCRSVisitor, null);

            // and then we reproject all geometries so that the datastore receives
            // them in the native projection system (or the forced one, in case of force)
            ReprojectingFilterVisitor reprojectingVisitor = new ReprojectingFilterVisitor(ff,
                    nativeFeatureType);
            Filter reprojectedFilter = (Filter) defaultedFilter.accept(reprojectingVisitor, null);

            Query reprojectedQuery = new Query(query);
            reprojectedQuery.setFilter(reprojectedFilter);
            return reprojectedQuery;
        } catch (Exception e) {
            throw new DataSourceException("Had troubles handling filter reprojection...", e);
        }
    }

}
