package org.geoserver.data.versioning.simple;

import java.util.NoSuchElementException;

import org.geogit.api.ObjectId;
import org.geoserver.data.versioning.ResourceIdAssigningFeatureCollection;
import org.geoserver.data.versioning.VersioningFeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.ResourceId;
import org.opengis.filter.sort.SortBy;

/**
 * FeatureCollectionDecorator that assigns as {@link ResourceId} as each Feature
 * {@link Feature#getIdentifier() identifier} from the {@link ObjectId} of the current state of the
 * Feature.
 * 
 * @author groldan
 * 
 */
public class SimpleResourceIdAssigningFeatureCollection extends
        ResourceIdAssigningFeatureCollection<SimpleFeatureType, SimpleFeature> implements
        SimpleFeatureCollection {

    public SimpleResourceIdAssigningFeatureCollection(SimpleFeatureCollection delegate,
            VersioningFeatureSource source, ObjectId commitId) {
        super(delegate, source, commitId);
    }

    /**
     * @see SimpleFeatureCollection#features()
     */
    @Override
    public SimpleFeatureIterator features() {
        return new SimpleResourceIdAssigningFeatureIterator(delegate.features());
    }

    /**
     * @see SimpleFeatureCollection#subCollection(Filter)
     */
    @Override
    public SimpleFeatureCollection subCollection(Filter filter) {
        return (SimpleFeatureCollection) super.subCollection(filter);
    }

    /**
     * @see SimpleFeatureCollection#sort(SortBy)
     */
    @Override
    public SimpleFeatureCollection sort(SortBy order) {
        return (SimpleFeatureCollection) super.sort(order);
    }

    class SimpleResourceIdAssigningFeatureIterator implements SimpleFeatureIterator {

        private ResourceIdAssigningFeatureIterator<SimpleFeature> features;

        SimpleResourceIdAssigningFeatureIterator(FeatureIterator<SimpleFeature> features) {
            this.features = new ResourceIdAssigningFeatureIterator<SimpleFeature>(features, store,
                    commitId);
        }

        @Override
        public boolean hasNext() {
            return features.hasNext();
        }

        @Override
        public SimpleFeature next() throws NoSuchElementException {
            return features.next();
        }

        @Override
        public void close() {
            features.close();
        }

    }
}