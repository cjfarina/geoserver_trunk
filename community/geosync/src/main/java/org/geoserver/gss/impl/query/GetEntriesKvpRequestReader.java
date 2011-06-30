package org.geoserver.gss.impl.query;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;

import org.geoserver.gss.internal.atom.Atom;
import org.geoserver.gss.service.FeedType;
import org.geoserver.gss.service.GetEntries;
import org.geoserver.ows.KvpRequestReader;
import org.geoserver.platform.ServiceException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.visitor.SimplifyingFilterVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.helpers.NamespaceSupport;

import com.vividsolutions.jts.geom.Geometry;

public class GetEntriesKvpRequestReader extends KvpRequestReader {

    private FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());

    public GetEntriesKvpRequestReader() {
        super(GetEntries.class);
    }

    @Override
    public GetEntries createRequest() throws Exception {
        return (GetEntries) super.createRequest();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public GetEntries read(Object reqObj, Map kvp, Map rawKvp) throws Exception {
        GetEntries request = (GetEntries) super.read(reqObj, kvp, rawKvp);

        // Mandatory, as of OGC 10-069r2, Table 11, page 63.
        if (null == request.getFeed()) {
            if (rawKvp.get("FEED") == null) {
                throw new ServiceException("FEED parameter was not provided",
                        "MissingParameterValue", "FEED");
            } else {
                throw new ServiceException("Invalid FEED parameter '" + rawKvp.get("FEED")
                        + "'. Expected one of " + Arrays.asList(FeedType.values()),
                        "InvalidParameterValue", "FEED");
            }
        }

        if (rawKvp.containsKey("filter") && request.getFilter() == null) {
            throw new ServiceException("Filter failed to be parsed. Unknown reason",
                    "InvalidParameterValue", "FILTER");
        }

        // Default value as of OGC 10-069r2, Table 11, page 63.
        if (null == request.getOutputFormat()) {
            request.setOutputFormat("application/atom+xml");
        }
        // Default value as of OGC 10-069r2, Table 11, page 63.
        if (null == request.getMaxEntries()) {
            request.setMaxEntries(Long.valueOf(25));
        }

        final Filter entryIdFitler = parseEntryId(kvp, rawKvp);
        final Filter temporalFitler = parseTemporalFilter(kvp, rawKvp);

        // Generalized predicate
        Filter generalizedPredicate = request.getFilter();
        if (generalizedPredicate == null) {
            generalizedPredicate = Filter.INCLUDE;
        }
        // Spatial parameters
        Filter spatialParamsFilter = buildSpatialParamsFilter(kvp, rawKvp);
        if (!Filter.INCLUDE.equals(spatialParamsFilter)
                && !Filter.INCLUDE.equals(generalizedPredicate)) {
            // Spatial Parameters and Generalized Predicate are mutually exclusive, as of OGC
            // 10-069r2, Table 11, page 63.
            throw new ServiceException("Precense of Spatial Parameters and Generalized Predicate "
                    + "are mutually exclusive", "InvalidParameterValue", "FILTER");
        }

        SimplifyingFilterVisitor visitor = new SimplifyingFilterVisitor();
        final Filter filter = (Filter) ff.and(
                Arrays.asList(entryIdFitler, temporalFitler, spatialParamsFilter,
                        generalizedPredicate)).accept(visitor, null);
        request.setFilter(filter);

        return request;
    }

    @SuppressWarnings("rawtypes")
    private Filter parseEntryId(Map kvp, Map rawKvp) {
        final String entryId = (String) kvp.get("ENTRYID");
        if (entryId == null) {
            return Filter.INCLUDE;
        }
        Id entryIdFilter = ff.id(Collections.singleton(ff.featureId(entryId)));
        return entryIdFilter;
    }

    @SuppressWarnings("rawtypes")
    private Filter buildSpatialParamsFilter(final Map kvp, final Map rawKvp) {
        final boolean hasSpatialParameters = kvp.containsKey("BBOX") || kvp.containsKey("GEOM")
                || kvp.containsKey("SPATIALOP") || kvp.containsKey("CRS");
        if (!hasSpatialParameters) {
            return Filter.INCLUDE;
        }

        Geometry geom = (Geometry) kvp.get("GEOM");
        ReferencedEnvelope bbox = (ReferencedEnvelope) kvp.get("BBOX");

        SpatialOp spatialOp = null;
        if (rawKvp.get("SPATIALOP") != null) {
            if (kvp.get("SPATIALOP") instanceof SpatialOp) {
                spatialOp = (SpatialOp) kvp.get("SPATIALOP");
            } else {
                throw new ServiceException("Invalid SPATIALOP parameter value: '"
                        + rawKvp.get("SPATIALOP") + "'. Expected one of "
                        + Arrays.asList(SpatialOp.values()), "InvalidParameterValue", "SPATIALOP");
            }
        }
        String srs = (String) kvp.get("CRS");
        if (srs == null) {
            srs = "urn:ogc:def:crs:EPSG::4326";
        }
        if (geom != null && bbox != null) {
            throw new ServiceException("Parameters GEOM and BBOX" + "are mutually exclusive",
                    "InvalidParameterValue", "GEOM/BBOX");
        }

        CoordinateReferenceSystem crs;
        try {
            crs = CRS.decode(srs);
        } catch (Exception e) {
            throw new ServiceException("Unable to parse CRS parameter value '" + srs + "': "
                    + e.getMessage(), "InvalidParameterValue", "CRS");
        }

        Filter filter = null;
        if (geom != null) {
            if (spatialOp == null) {
                spatialOp = SpatialOp.Intersects;
            }
            geom.setUserData(crs);
            NamespaceSupport nscontext = new NamespaceSupport();
            nscontext.declarePrefix("atom", Atom.NAMESPACE);
            nscontext.declarePrefix("georss", "http://www.georss.org/georss");
            Expression propertyName = ff.property("atom:entry/georss:where", nscontext);
            Expression geometryLiteral = ff.literal(geom);
            switch (spatialOp) {
            case Contains:
                filter = ff.contains(propertyName, geometryLiteral);
                break;
            case Crosses:
                filter = ff.crosses(propertyName, geometryLiteral);
                break;
            case Disjoint:
                filter = ff.disjoint(propertyName, geometryLiteral);
                break;
            case Equals:
                filter = ff.equals(propertyName, geometryLiteral);
                break;
            case Intersects:
                filter = ff.intersects(propertyName, geometryLiteral);
                break;
            case Overlaps:
                filter = ff.overlaps(propertyName, geometryLiteral);
                break;
            case Touches:
                filter = ff.touches(propertyName, geometryLiteral);
                break;
            case Within:
                filter = ff.within(propertyName, geometryLiteral);
                break;
            }
        } else if (bbox != null) {
            if (null != bbox.getCoordinateReferenceSystem()) {
                try {
                    srs = CRS.lookupIdentifier(bbox.getCoordinateReferenceSystem(), true);
                } catch (FactoryException e) {
                    LOGGER.log(Level.INFO, e.getMessage(), e);
                }
            }
            filter = ff.bbox((String) null, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(),
                    bbox.getMaxY(), srs);
        }
        return filter;
    }

    @SuppressWarnings("rawtypes")
    private Filter parseTemporalFilter(Map kvp, Map rawKvp) {
        final Date startTime = (Date) kvp.get("STARTTIME");
        final Date endTime = (Date) kvp.get("ENDTIME");
        TemporalOp temporalOp = (TemporalOp) kvp.get("TEMPORALOP");

        // Check validity of Temporal Parameters, as per OGC 10-069r2, Table 11, page 63.
        if (temporalOp != null && startTime == null && endTime == null) {
            throw new ServiceException(
                    "TEMPORALOP cannot be specified if either STARTTIME and/or ENDTIME are specified",
                    "InvalidParameterValue", "TEMPORALOP");
        }
        if (null != endTime) {
            if (null == startTime) {
                throw new ServiceException(
                        "ENDTIME is only valid if STARTTIME and a period related temporal operation are also provided",
                        "MissingParameterValue", "STARTTIME");
            }
            if (null == temporalOp) {
                throw new ServiceException(
                        "TEMPORALOP is mandatory if either STARTTIME and ENDTIME were provided",
                        "MissingParameterValue", "STARTTIME");
            }
            if (!temporalOp.requiresPeriod()) {
                throw new ServiceException(
                        "STARTTIME and ENDTIME shall only be requested with one of the following values for TEMPORALOP: "
                                + TemporalOp.periodRelated(), "InvalidParameterValue", "TEMPORALOP");
            }
        } else if (null == startTime) {
            return Filter.INCLUDE;
        }

        if (null == temporalOp) {
            // no temportal op provided, assume after
            temporalOp = TemporalOp.After;
        }

        final String updated = "updated";
        final PropertyName property = ff.property(updated);
        Filter temporalFilter;
        switch (temporalOp) {
        case After: {
            temporalFilter = ff.greater(property, ff.literal(startTime));
            break;
        }
        case Before: {
            temporalFilter = ff.less(property, ff.literal(startTime));
            break;
        }
        case During: {
            Expression greaterThan = ff.literal(startTime);
            Expression lowerThan = ff.literal(endTime);
            temporalFilter = ff.between(property, greaterThan, lowerThan);
            break;
        }
        case TEquals: {
            temporalFilter = ff.equal(property, ff.literal(startTime), true);
            break;
        }
        case Begins:
        case OverlappedBy:
        case BegunBy:
        case EndedBy:
        case Ends:
        case Meets:
        case MetBy:
        case TContains:
        case TOverlaps:
        default:
            throw new ServiceException(temporalOp + " predicate not yet supported", "TEMPORALOP");
        }

        return temporalFilter;
    }
}
