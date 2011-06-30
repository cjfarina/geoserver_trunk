package org.geoserver.gss.impl.query;

import static org.geoserver.gss.service.FeedType.CHANGEFEED;

import org.geoserver.gss.impl.GSS;
import org.geoserver.gss.service.FeedType;
import org.geoserver.gss.service.GetEntries;
import org.geoserver.gss.service.GetEntriesResponse;
import org.geoserver.platform.ServiceException;
import org.gvsig.bxml.stream.BxmlStreamWriter;

/**
 * Encodes the response of a {@code GetEntries} operation against the {@link FeedType#CHANGEFEED
 * CHANGEFEED} as specified in the <i>OGC 10-069r2 Engineering Report</i>, section 9.2.2
 * "Proposed change feed", page 45.
 */
public class ChangeFeedResponse extends AbstractGetEntriesResponse {

    public ChangeFeedResponse(final GSS gss) {
        super(CHANGEFEED, gss);
    }

    @Override
    protected void encode(GetEntries request, GetEntriesResponse response, BxmlStreamWriter w)
            throws ServiceException, Exception {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
