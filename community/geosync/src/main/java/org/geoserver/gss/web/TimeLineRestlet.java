package org.geoserver.gss.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.geoserver.gss.impl.GSS;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.geoserver.gss.internal.atom.FeedImpl;
import org.geoserver.gss.internal.atom.PersonImpl;
import org.geotools.feature.type.DateUtil;
import org.gvsig.bxml.adapt.stax.XmlStreamWriterAdapter;
import org.gvsig.bxml.stream.BxmlStreamWriter;
import org.gvsig.bxml.stream.EncodingOptions;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortOrder;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.OutputRepresentation;

public class TimeLineRestlet extends Restlet {

    @Override
    public void handle(Request request, Response response) {
        super.init(request, response);
        if (!Method.GET.equals(request.getMethod())) {
            response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, "Method not allowed");
        }

        final FeedImpl feed;
        {
            List<String> searchTerms = null;
            Filter filter = Filter.INCLUDE;
            Long startPosition = null;
            Long maxEntries = 1000L;
            // order is not significant for presentation, but let's ask for latest first so we
            // display the latest N events
            SortOrder descending = SortOrder.DESCENDING;
            feed = GSS.get().queryResolutionFeed(searchTerms, filter, startPosition, maxEntries,
                    descending);
        }

        response.setEntity(new OutputRepresentation(MediaType.TEXT_XML) {

            @Override
            public void write(final OutputStream out) throws IOException {
                BxmlStreamWriter w;
                {
                    XMLStreamWriter staxWriter;
                    try {
                        staxWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
                    } catch (XMLStreamException e) {
                        throw new RuntimeException(e);
                    } catch (FactoryConfigurationError e) {
                        throw new RuntimeException(e);
                    }
                    w = new XmlStreamWriterAdapter(new EncodingOptions(), staxWriter);
                }

                w.writeStartDocument();
                w.writeStartElement(XMLConstants.NULL_NS_URI, "data");

                for (Iterator<EntryImpl> it = feed.getEntry(); it.hasNext();) {
                    EntryImpl e = it.next();
                    w.writeStartElement(XMLConstants.NULL_NS_URI, "event");
                    {
                        String id = e.getId();
                        List<PersonImpl> author = e.getAuthor();
                        String title = String.valueOf(e.getContent().getValue());// e.getTitle();
                        String summary = e.getSummary();
                        Date updated = e.getUpdated();
                        String feedHref = "../../ows?service=GSS&version=1.0.0&request=GetEntries&feed=REPLICATIONFEED&outputFormat=text/xml&";
                        feedHref += "temporalOp=TEquals&startTime="
                                + DateUtil.serializeDateTime(updated.getTime(), true)
                                + "&startPosition=1&maxEntries=50";

                        w.writeStartAttribute(XMLConstants.NULL_NS_URI, "start");
                        w.writeValue(DateUtil.serializeDateTime(updated.getTime(), true));

                        w.writeStartAttribute(XMLConstants.NULL_NS_URI, "title");
                        w.writeValue(title);

                        w.writeStartAttribute(XMLConstants.NULL_NS_URI, "link");
                        w.writeValue(feedHref);

                        w.writeStartAttribute(XMLConstants.NULL_NS_URI, "image");
                        w.writeValue("../../web/resources/org.geoserver.gss.web.ChangesPanel/feed-icon-14x14.png");

                        // this is the icon of the event in the timeline, might be useful to
                        // distinguish different types of events in the future
                        // w.writeStartAttribute(XMLConstants.NULL_NS_URI, "icon");
                        // w.writeValue("../../web/resources/org.geoserver.gss.web.ChangesPanel/feed-icon-14x14.png");

                        w.writeEndAttributes();

                        w.writeValue("<i>" + summary + "</i>");
                        w.writeValue("<p>Author: ");
                        if (author.size() > 0) {
                            w.writeValue(e.getAuthor().get(0).getName());
                        } else {
                            w.writeValue("<i>unknown</i>");
                        }
                        //
                        // w.writeValue("<p><a href=\""
                        // + feedHref
                        // +
                        // "\"><img src=\"../web/resources/org.geoserver.gss.web.ChangesPanel/feed-icon-14x14.png\"/> Open contents<a>");
                    }
                    w.writeEndElement();
                }

                w.writeEndElement();
                w.writeEndDocument();
                w.flush();
            }
        });
        response.setStatus(Status.SUCCESS_OK);
    }
}
