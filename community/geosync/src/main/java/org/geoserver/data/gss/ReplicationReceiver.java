package org.geoserver.data.gss;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.wfs.DeleteElementType;
import net.opengis.wfs.InsertElementType;
import net.opengis.wfs.PropertyType;
import net.opengis.wfs.UpdateElementType;

import org.eclipse.emf.common.util.EList;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.data.versioning.GeoToolsCommitStateResolver;
import org.geoserver.gss.internal.atom.ContentImpl;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.geoserver.gss.internal.atom.FeedImpl;
import org.geoserver.gss.internal.atom.PersonImpl;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.Hints;
import org.geotools.feature.NameImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.springframework.util.Assert;

import com.google.common.base.Preconditions;

@SuppressWarnings("rawtypes")
public class ReplicationReceiver {

    /**
     * Grabs the list of a GSS replication feed entries and applies them to the repository.
     * 
     * @param replicationFeed
     * @param targetRepo
     * @throws Exception
     */
    public void receive(final FeedImpl replicationFeed, final Catalog catalog) throws Exception {
        final Iterator<EntryImpl> entries = replicationFeed.getEntry();

        EntryImpl lastEntry = null;

        Transaction transaction = new DefaultTransaction();
        Map<Name, FeatureStore> stores = new HashMap<Name, FeatureStore>();

        while (entries.hasNext()) {
            EntryImpl entry = entries.next();
            if (lastEntry == null) {
                lastEntry = entry;
            }
            String currentCommitMessage = entry.getSummary();
            String previousCommitMessage = lastEntry.getSummary();
            if (!previousCommitMessage.equals(currentCommitMessage)) {
                transaction = commit(lastEntry, transaction);
            }
            FeatureStore store = findStore(entry, catalog, stores);
            store.setTransaction(transaction);
            apply(store, entry);
        }
        commit(lastEntry, transaction);
    }

    protected Transaction commit(EntryImpl lastEntry, Transaction transaction) throws IOException {
        String user = author(lastEntry);
        String commitMessage = lastEntry.getSummary();
        transaction.putProperty(GeoToolsCommitStateResolver.GEOGIT_USER_NAME, user);
        transaction.putProperty(GeoToolsCommitStateResolver.GEOGIT_COMMIT_MESSAGE, commitMessage);
        transaction.commit();
        transaction.close();
        transaction = new DefaultTransaction();
        return transaction;
    }

    private FeatureStore findStore(EntryImpl entry, Catalog catalog, Map<Name, FeatureStore> stores)
            throws IOException {
        Name typeName = typeName(entry.getContent().getValue());

        FeatureStore store = stores.get(typeName);
        if (store == null) {
            FeatureTypeInfo typeInfo = catalog.getFeatureTypeByName(typeName);
            store = (FeatureStore) typeInfo.getFeatureSource(null, null);
            // TODO: uncomment the following line once we integrate with versioning
            // Preconditions.checkState(store instanceof VersioningFeatureStore);
            stores.put(typeName, store);
        }
        return store;
    }

    private Name typeName(Object value) {
        Assert.notNull(value);
        if (value instanceof InsertElementType) {
            InsertElementType insertElement = (InsertElementType) value;
            Feature feature = (Feature) insertElement.getFeature().get(0);
            return feature.getType().getName();
        }

        if (value instanceof UpdateElementType) {
            QName qName = ((UpdateElementType) value).getTypeName();
            return new NameImpl(qName);
        }

        if (value instanceof DeleteElementType) {
            QName qName = ((DeleteElementType) value).getTypeName();
            return new NameImpl(qName);
        }

        return null;
    }

    private void apply(FeatureStore store, EntryImpl entry) throws IOException {
        ContentImpl content = entry.getContent();
        if (content.getValue() instanceof UpdateElementType) {
            update(store, (UpdateElementType) content.getValue());
        }

        if (content.getValue() instanceof DeleteElementType) {
            delete(store, (DeleteElementType) content.getValue());
        }

        if (content.getValue() instanceof InsertElementType) {
            insert(store, (InsertElementType) content.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void insert(FeatureStore store, InsertElementType insertElement) throws IOException {
        if (!store.getQueryCapabilities().isUseProvidedFIDSupported()) {
            throw new UnsupportedOperationException(
                    "The underlying data store '"
                            + store.getSchema().getName().getLocalPart()
                            + "' does not support the USE_PROVIDED_FID Hint and inserts won't preserve Feature IDs");
        }
        List<SimpleFeature> features = insertElement.getFeature();
        for (SimpleFeature f : features) {
            f.getUserData().put(Hints.USE_PROVIDED_FID, Boolean.TRUE);
        }
        SimpleFeatureCollection fc = DataUtilities.collection(features);
        List addedFeatures = store.addFeatures(fc);
        int size = fc.size();
        Preconditions.checkState(size == addedFeatures.size());
        store.addFeatures(fc);
    }

    private void delete(FeatureStore store, DeleteElementType deleteElement) throws IOException {
        store.removeFeatures(deleteElement.getFilter());
    }

    private void update(FeatureStore store, UpdateElementType updateElement) throws IOException {
        EList properties = updateElement.getProperty();

        Name[] attributeNames = new NameImpl[properties.size()];
        Object[] attributeValues = new Object[properties.size()];
        int i = 0;
        for (Object object : properties) {
            PropertyType propertyType = (PropertyType) object;
            attributeNames[i] = new NameImpl(propertyType.getName().getLocalPart());
            attributeValues[i] = propertyType.getValue();
            i++;
        }

        Filter filter = updateElement.getFilter();
        FeatureType schema = store.getSchema();
        schema.getDescriptor(attributeNames[0]);
        store.modifyFeatures(attributeNames, attributeValues, filter);
    }

    private String committer(EntryImpl entry) {
        return name(entry.getContributor());
    }

    private String author(EntryImpl entry) {
        return name(entry.getAuthor());
    }

    private String name(List<PersonImpl> person) {
        if (person != null && person.size() > 0) {
            return person.get(0).getName();
        }
        return null;
    }

}