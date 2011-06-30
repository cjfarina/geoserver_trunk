package org.geoserver.gss.impl.query;

import static org.geoserver.gss.service.FeedType.REPLICATIONFEED;

import java.io.IOException;

import org.geoserver.gss.impl.GSS;
import org.geoserver.gss.internal.atom.FeedEncoder;
import org.geoserver.gss.internal.atom.FeedImpl;
import org.geoserver.gss.service.FeedType;
import org.geoserver.gss.service.GetEntries;
import org.geoserver.gss.service.GetEntriesResponse;
import org.geoserver.platform.ServiceException;
import org.gvsig.bxml.stream.BxmlStreamWriter;

/**
 * Encodes the response of a {@code GetEntries} operation against the
 * {@link FeedType#REPLICATIONFEED REPLICATIONFEED} as specified in the <i>OGC 10-069r2 Engineering
 * Report</i>, section 9.2.4 "Resolution feed", page 48.
 */
public class ReplicationFeedResponse extends AbstractGetEntriesResponse {

    public ReplicationFeedResponse(final GSS gss) {
        super(REPLICATIONFEED, gss);
    }

    @Override
    protected void encode(final GetEntries request, final GetEntriesResponse response,
            final BxmlStreamWriter w) throws ServiceException, IOException {

        final FeedImpl feed = response.getResult();
        FeedEncoder encoder = new FeedEncoder(w);
        try {
            encoder.encode(feed);
        } finally {
            w.flush();
        }
    }

}
