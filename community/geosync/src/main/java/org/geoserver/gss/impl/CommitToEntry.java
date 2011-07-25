package org.geoserver.gss.impl;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.geogit.api.DiffEntry;
import org.geogit.api.GeoGIT;
import org.geogit.api.ObjectId;
import org.geogit.api.RevCommit;
import org.geogit.repository.Repository;
import org.geoserver.gss.internal.atom.ContentImpl;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.geoserver.gss.internal.atom.PersonImpl;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Adapts {@link RevCommit} to an Atom {@link EntryImpl}.
 * 
 */
class CommitToEntry implements Function<RevCommit, EntryImpl> {

    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools
            .getDefaultHints());

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    private final GSS gss;

    private final GeoGIT geoGit;

    public CommitToEntry(final GSS gss, final GeoGIT geoGit) {
        this.gss = gss;
        this.geoGit = geoGit;
    }

    @Override
    public EntryImpl apply(final RevCommit commit) {

        EntryImpl atomEntry = new EntryImpl();

        // NOTE: this is not really what the atom:entry should be, as if someone requested an entry
        // by it this wouldn't indicate whether it's a feature insert,update,or delete. But this is
        // a concept of GSS exclusively, as we can't use the commit id to refer to a single feature
        // change neither, so the mapping from entry id to DiffEntry should be in the GSS database,
        // and a new atom:entry id should be automatically generated as stated in the spec
        ObjectId objectId = commit.getId();

        atomEntry.setId(objectId.toString());// TODO: convert to UUID
        atomEntry.setTitle(title(commit));
        atomEntry.setSummary(commit.getMessage());
        atomEntry.setUpdated(new Date(commit.getTimestamp()));
        atomEntry.setAuthor(author(commit));
        atomEntry.setContributor(contributor(commit));
        atomEntry.setContent(content());
        atomEntry.setWhere(where(commit));

        // atomEntry.setCategory(category);
        // atomEntry.setLink(link);
        // atomEntry.setPublished(published);
        // atomEntry.setRights(rights);
        // atomEntry.setSource(source);

        return atomEntry;
    }

    private Object where(final RevCommit commit) {
        final ObjectId parentId = commit.getParentIds().get(0);
        ReferencedEnvelope where = null;
        Iterator<DiffEntry> diff;
        try {
            diff = geoGit.diff().setOldVersion(parentId).setNewVersion(commit.getId()).call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        while (diff.hasNext()) {
            DiffEntry next = diff.next();
            BoundingBox diffEnv = computeWhere(next);
            if (diffEnv != null && where == null) {
                try {
                    where = new ReferencedEnvelope(CRS.decode("urn:ogc:def:crs:EPSG::4326"));
                } catch (Exception e) {
                    Throwables.propagate(e);
                }
            }
            expandToInclude(where, diffEnv);
        }
        return where;
    }

    private void expandToInclude(ReferencedEnvelope where, final BoundingBox diffEnv) {
        if (diffEnv == null) {
            return;
        }
        CoordinateReferenceSystem targetCrs = where.getCoordinateReferenceSystem();
        CoordinateReferenceSystem sourceCrs = diffEnv.getCoordinateReferenceSystem();
        try {
            Envelope env = diffEnv;
            if (!CRS.equalsIgnoreMetadata(targetCrs, sourceCrs)) {
                MathTransform mathTransform = CRS.findMathTransform(sourceCrs, targetCrs);
                env = CRS.transform(mathTransform, diffEnv);
            }
            where.expandToInclude(env.getMinimum(0), env.getMinimum(0));
            where.expandToInclude(env.getMaximum(1), env.getMaximum(1));
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    private BoundingBox computeWhere(final DiffEntry diff) {
        final String namespace = diff.getPath().get(0);
        final String typeName = diff.getPath().get(1);
        final String featureId = diff.getPath().get(2);

        final FeatureType featureType = gss.getFeatureType(namespace, typeName);
        final GeoGIT ggit = gss.getGeoGit();
        final Repository repository = ggit.getRepository();
        switch (diff.getType()) {
        case ADD: {
            ObjectId contentId = diff.getNewObjectId();
            Feature addedFeature = repository.getFeature(featureType, featureId, contentId);
            return addedFeature.getBounds();
        }
        case DELETE: {
            ObjectId oldStateId = diff.getOldObjectId();
            Feature oldState = repository.getFeature(featureType, featureId, oldStateId);
            return oldState.getBounds();
        }
        case MODIFY: {

            ObjectId oldStateId = diff.getOldObjectId();
            ObjectId newStateId = diff.getNewObjectId();

            Feature oldState = repository.getFeature(featureType, featureId, oldStateId);
            Feature newState = repository.getFeature(featureType, featureId, newStateId);

            BoundingBox oldBounds = oldState.getBounds();
            BoundingBox newBounds = newState.getBounds();

            if (newBounds == null) {
                return oldBounds;
            }
            if (oldBounds == null) {
                return newBounds;
            }
            ReferencedEnvelope where = new ReferencedEnvelope(
                    oldBounds.getCoordinateReferenceSystem());
            where.expandToInclude(newBounds.getMinX(), newBounds.getMinY());
            where.expandToInclude(newBounds.getMaxX(), newBounds.getMaxY());
            return where;
        }
        default:
            throw new IllegalStateException();
        }
    }

    private ContentImpl content() {
        return null;// TODO
    }

    /**
     * @param commit
     * @return committer
     */
    private List<PersonImpl> contributor(final RevCommit commit) {
        PersonImpl contributor = new PersonImpl();
        contributor.setName(commit.getCommitter());
        return Collections.singletonList(contributor);
    }

    /**
     * @param commit
     * @return commit author
     */
    private List<PersonImpl> author(final RevCommit commit) {
        PersonImpl author = new PersonImpl();
        author.setName(commit.getAuthor());
        return Collections.singletonList(author);
    }

    private String title(final RevCommit commit) {
        return commit.getMessage();
    }

}
