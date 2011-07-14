/* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gss.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.geogit.api.DiffEntry;
import org.geogit.api.DiffEntry.ChangeType;
import org.geogit.api.GeoGIT;
import org.geogit.api.LogOp;
import org.geogit.api.ObjectId;
import org.geogit.api.RevCommit;
import org.geogit.repository.Index;
import org.geogit.repository.Repository;
import org.geogit.repository.WorkingTree;
import org.geogit.storage.RepositoryDatabase;
import org.geogit.storage.bdbje.EntityStoreConfig;
import org.geogit.storage.bdbje.EnvironmentBuilder;
import org.geogit.storage.bdbje.JERepositoryDatabase;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.gss.config.GSSInfo;
import org.geoserver.gss.impl.query.AbstractGetEntriesResponse;
import org.geoserver.gss.internal.atom.FeedImpl;
import org.geoserver.gss.internal.storage.GeoSyncDatabase;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.ServiceException;
import org.geoserver.task.LongTaskMonitor;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.util.NullProgressListener;
import org.geotools.util.logging.Logging;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortOrder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;

import com.sleepycat.je.Environment;

/**
 * Service facade to GeoServer
 * 
 * @author Gabriel Roldan
 * 
 */
public class GSS implements DisposableBean {

    private static final NullProgressListener NULL_PROGRESS_LISTENER = new NullProgressListener();

    private static final Logger LOGGER = Logging.getLogger(GSS.class);

    private static final String GSS_DATA_ROOT = "gss_data";

    private static final String GSS_GEOGIT_REPO = "geogit_repo";

    private static final String GSS_REPO = "gss_repo";

    private AuthenticationResolver authResolver;

    private final Catalog catalog;

    private final GeoGIT geoGit;

    private final GeoSyncDatabase gssDb;

    public GSS(final Catalog catalog, final GeoServerDataDirectory dataDir) throws IOException {
        this.catalog = catalog;
        this.authResolver = new AuthenticationResolver();
        final File geogitRepo = dataDir.findOrCreateDataDir(GSS_DATA_ROOT, GSS_GEOGIT_REPO);

        EnvironmentBuilder esb = new EnvironmentBuilder(new EntityStoreConfig());

        Properties bdbEnvProperties = null;
        Environment geogitEnvironment = esb.buildEnvironment(geogitRepo, bdbEnvProperties);
        RepositoryDatabase ggitRepoDb = new JERepositoryDatabase(geogitEnvironment);

        // RepositoryDatabase ggitRepoDb = new FileSystemRepositoryDatabase(geogitRepo);

        Repository repository = new Repository(ggitRepoDb);
        repository.create();

        this.geoGit = new GeoGIT(repository);

        // StatsConfig config = new StatsConfig();
        // config.setClear(true);
        // System.err.println(geogitEnvironment.getStats(config));

        final File gssRepo = dataDir.findOrCreateDataDir(GSS_DATA_ROOT, GSS_REPO);
        Environment gssEnvironment = esb.buildEnvironment(gssRepo, bdbEnvProperties);
        gssDb = new GeoSyncDatabase(geoGit, gssEnvironment);
        gssDb.create();
    }

    public static GSS get() {
        GSS singleton = GeoServerExtensions.bean(GSS.class);
        return singleton;
    }

    /**
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy() throws Exception {
        try {
            Repository repository = geoGit.getRepository();
            repository.close();
        } finally {
            gssDb.close();
        }
    }

    public List<Name> listLayers() throws Exception {
        geoGit.checkout().setName("master").call();
        WorkingTree workingTree = geoGit.getRepository().getWorkingTree();
        List<Name> typeNames = workingTree.getFeatureTypeNames();
        return typeNames;
    }

    /**
     * Adds the GeoSever Catalog's FeatureType named after {@code featureTypeName} to the master
     * branch.
     * 
     * @param featureTypeName
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public Future<?> initialize(final Name featureTypeName) throws Exception {
        final String user = getCurrentUserName();
        Assert.notNull(user, "This operation shall be invoked by a logged in user");

        final FeatureTypeInfo featureTypeInfo = catalog.getFeatureTypeByName(featureTypeName);
        Assert.notNull(featureTypeInfo, "No FeatureType named " + featureTypeName
                + " found in the Catalog");

        final FeatureSource featureSource = featureTypeInfo.getFeatureSource(null, null);
        if (featureSource == null) {
            throw new NullPointerException(featureTypeInfo + " didn't return a FeatureSource");
        }
        if (!(featureSource instanceof FeatureStore)) {
            throw new IllegalArgumentException("Can't version "
                    + featureTypeInfo.getQualifiedName() + " because it is read only");
        }

        ImportVersionedLayerTask importTask;
        importTask = new ImportVersionedLayerTask(user, featureSource, geoGit);
        LongTaskMonitor monitor = GeoServerExtensions.bean(LongTaskMonitor.class);
        Future<RevCommit> future = monitor.dispatch(importTask);
        return future;
    }

    public boolean isReplicated(final Name featureTypeName) {
        return geoGit.getRepository().getWorkingTree().hasRoot(featureTypeName);
    }

    public void initChangeSet(final String gssTransactionID) throws Exception {
        // branch master
        geoGit.checkout().setName("master").call();
        geoGit.branchCreate().setName(gssTransactionID).call();
    }

    /**
     * @param gssTransactionID
     * @param typeName
     * @param affectedFeatures
     * @return the list of feature ids of the inserted features, in the order they were added
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public List<String> stageInsert(final String gssTransactionID, final Name typeName,
            FeatureCollection affectedFeatures) throws Exception {

        // geoGit.checkout().setName(gssTransactionID).call();
        WorkingTree workingTree = geoGit.getRepository().getWorkingTree();
        List<String> insertedFids = workingTree.insert(typeName, affectedFeatures,
                NULL_PROGRESS_LISTENER);
        geoGit.add().call();

        return insertedFids;
    }

    @SuppressWarnings("rawtypes")
    public void stageUpdate(final String gssTransactionID, final Name typeName,
            final Filter filter, final List<PropertyName> updatedProperties,
            final List<Object> newValues, final FeatureCollection affectedFeatures)
            throws Exception {

        geoGit.checkout().setName(gssTransactionID).call();
        WorkingTree workingTree = geoGit.getRepository().getWorkingTree();
        workingTree.update(typeName, filter, updatedProperties, newValues, affectedFeatures,
                NULL_PROGRESS_LISTENER);
        geoGit.add().call();
    }

    @SuppressWarnings("rawtypes")
    public void stageDelete(final String gssTransactionID, Name typeName, Filter filter,
            FeatureCollection affectedFeatures) throws Exception {

        geoGit.checkout().setName(gssTransactionID).call();
        WorkingTree workingTree = geoGit.getRepository().getWorkingTree();
        workingTree.delete(typeName, filter, affectedFeatures);
        geoGit.add().call();
    }

    public void stageRename(final Name typeName, final String oldFid, final String newFid) {

        Index index = geoGit.getRepository().getIndex();

        final String namespaceURI = typeName.getNamespaceURI();
        final String localPart = typeName.getLocalPart();

        List<String> from = Arrays.asList(namespaceURI, localPart, oldFid);
        List<String> to = Arrays.asList(namespaceURI, localPart, newFid);

        index.renamed(from, to);
    }

    /**
     * Merges branch named after {@code gssTransactionID} back to master and commits.
     * 
     * @param gssTransactionID
     * @param commitMsg
     * @return 
     * @throws Exception
     */
    public RevCommit commitChangeSet(final String gssTransactionID, final String commitMsg)
            throws Exception {
        String userName = getCurrentUserName();
        LOGGER.info("Committing changeset " + gssTransactionID + " by user " + userName);

        // final Ref branch = geoGit.checkout().setName(gssTransactionID).call();
        // commit to the branch
        RevCommit commit;
        // checkout master
        // final Ref master = geoGit.checkout().setName("master").call();
        // merge branch to master
        // MergeResult mergeResult = geoGit.merge().include(branch).call();
        // TODO: check mergeResult is success?
        // geoGit.branchDelete().setName(gssTransactionID).call();
        commit = geoGit.commit().setAuthor(userName).setCommitter("geoserver")
                .setMessage(commitMsg).call();
        return commit;
    }

    /**
     * Discards branch named after {@code gssTransactionID}.
     * 
     * @param gssTransactionID
     * @throws Exception
     */
    public void rollBackChangeSet(final String gssTransactionID) throws Exception {
        String userName = getCurrentUserName();
        System.err.println("Rolling back changeset " + gssTransactionID + " by user " + userName);

        // TODO: implement ResetOp instead?!
        geoGit.getRepository().getIndex().reset();

        String deletedBranch = geoGit.branchDelete().setName(gssTransactionID).setForce(true)
                .call();
        if (deletedBranch == null) {
            LOGGER.info("Tried to delete branch " + gssTransactionID + " but it didn't exist");
        }
    }

    /**
     * @return {@code null} if annonymous, the name of the current user otherwise
     */
    public String getCurrentUserName() {
        return authResolver.getCurrentUserName();
    }

    /**
     * Set an alternate auth resolver, mainly used to aid in unit testing code that depends on this
     * class.
     */
    public void setAuthenticationResolver(AuthenticationResolver resolver) {
        this.authResolver = resolver;
    }

    public LogOp log() {
        return this.geoGit.log();
    }

    public GSSInfo getGssInfo() {
        return GeoServerExtensions.bean(GeoServer.class).getService(GSSInfo.class);
    }

    public GeoGIT getGeoGit() {
        return this.geoGit;
    }

    /**
     * <p>
     * The response of a query to the {@code REPLICATIONFEED} is comprised of a single
     * {@code <atom:entry>} per {@link Feature} between (<i>to points in time</i> CORRECTION: every
     * two consecutive commits or changes).<br>
     * <p>
     * Each {@code entry} represents either the addition, deletion, or modification of a single
     * {@code Feature}.
     * <p>
     * The replication feed is mapped to the GeoGit's "master" branch. I.e. it represents the
     * current state of a dataset.
     * 
     * 
     * @param searchTerms
     * @param filter
     * @param startPosition
     * @param maxEntries
     * @return
     * @throws Exception
     * @see {@link DiffEntryListBuilder}
     */
    public FeedImpl queryReplicationFeed(final List<String> searchTerms, final Filter filter,
            final Long startPosition, final Long maxEntries, final SortOrder sortOrder)
            throws ServiceException {

        DiffEntryListBuilder diffEntryListBuilder = new DiffEntryListBuilder(this, geoGit);
        diffEntryListBuilder.setSearchTerms(searchTerms);
        diffEntryListBuilder.setFilter(filter);
        diffEntryListBuilder.setStartPosition(startPosition);
        diffEntryListBuilder.setMaxEntries(maxEntries);
        diffEntryListBuilder.setSortOrder(sortOrder);

        FeedImpl feed;
        try {
            feed = diffEntryListBuilder.buildFeed();
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        return feed;
    }

    public FeedImpl queryChangeFeed(List<String> searchTerms, Filter filter, Long startPosition,
            Long maxEntries) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @param searchTerms
     * @param filter
     * @param startPosition
     * @param maxEntries
     * @param sortOrder
     * @return
     */
    public FeedImpl queryResolutionFeed(final List<String> searchTerms, final Filter filter,
            final Long startPosition, final Long maxEntries, final SortOrder sortOrder) {

        CommitsEntryListBuilder commitEntryListBuilder = new CommitsEntryListBuilder(this, geoGit);
        commitEntryListBuilder.setSearchTerms(searchTerms);
        commitEntryListBuilder.setFilter(filter);
        commitEntryListBuilder.setStartPosition(startPosition);
        commitEntryListBuilder.setMaxEntries(maxEntries);
        commitEntryListBuilder.setSortOrder(sortOrder);

        FeedImpl feed;
        try {
            feed = commitEntryListBuilder.buildFeed();
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        return feed;
    }

    public FeatureType getFeatureType(String namespace, String typeName) {
        FeatureTypeInfo featureType = catalog.getFeatureTypeByName(namespace, typeName);
        try {
            return featureType.getFeatureType();
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * @return the list of supported {@code GetEntries} output formats by inspecting the application
     *         context for instances of {@link AbstractGetEntriesResponse}
     */
    public Set<String> getGetEntriesOutputFormats() {
        final List<AbstractGetEntriesResponse> getEntriesResponses;
        getEntriesResponses = GeoServerExtensions.extensions(AbstractGetEntriesResponse.class);

        Set<String> supportedFormats = new HashSet<String>();
        for (AbstractGetEntriesResponse r : getEntriesResponses) {
            supportedFormats.addAll(r.getOutputFormats());
        }

        return supportedFormats;
    }

    /**
     * Returns a UUID for a GSS atom entry for the replication feed, creating it if needed.
     * 
     * @param diffEnty
     * @return
     * @see #getEntryByUUID
     */
    public String getGssEntryId(final DiffEntry diffEnty) {
        // NOTE: this is not really what the atom:entry should be, as if someone requested an entry
        // by it this wouldn't indicate whether it's a feature insert,update,or delete. But this is
        // a concept of GSS exclusively, as we can't use the commit id to refer to a single feature
        // change neither, so the mapping from entry id to DiffEntry should be in the GSS database,
        // and a new atom:entry id should be automatically generated as stated in the spec
        ObjectId objectId = diffEnty.getType() == ChangeType.DELETE ? diffEnty.getOldObjectId()
                : diffEnty.getNewObjectId();

        // return gssDb.getOrCreateEntryUUID();
        return objectId.toString();
    }

    /**
     * @param uuid
     * @return
     * @see #getGssEntryId
     */
    public DiffEntry getEntryByUUID(final String uuid) {
        return null;
    }

}
