/* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gss.web;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.template.TextTemplateHeaderContributor;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.gss.impl.GSS;
import org.geoserver.gss.internal.atom.EntryImpl;
import org.geoserver.gss.internal.atom.FeedImpl;
import org.geoserver.task.LongTask;
import org.geoserver.task.web.LongTasksPanel;
import org.geoserver.web.CatalogIconFactory;
import org.geoserver.web.GeoServerSecuredPage;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortOrder;

import com.vividsolutions.jts.geom.Geometry;

public class GSSLayersPage extends GeoServerSecuredPage implements IHeaderContributor {

    private final GSSLayerProvider provider;

    private GeoServerTablePanel<GSSLayerInfo> publishedLayersTable;

    private GeoServerDialog removalDialog;

    private GSSLayerSelectionRemovalLink removalLink;

    /**
     * @see org.apache.wicket.markup.html.IHeaderContributor#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
     */
    public void renderHead(IHeaderResponse header) {
        header.renderJavascriptReference("http://static.simile.mit.edu/timeline/api-2.3.0/timeline-api.js?bundle=true");
        // header.renderJavascriptReference(new ResourceReference(GSSLayersPage.class,
        // "timeline.js"));
        header.renderOnLoadJavascript("onLoad()");
        header.renderOnEventJavascript("window", "resize", "onResize()");
    }

    public GSSLayersPage() {
        provider = new GSSLayerProvider();

        // get the date of the latest change to position the timeline at
        final Date lastChange;
        {
            FeedImpl feed = GSS.get().queryResolutionFeed(null, Filter.INCLUDE, 1L, 1L,
                    SortOrder.DESCENDING);
            Iterator<EntryImpl> iterator = feed.getEntry();
            if (iterator.hasNext()) {
                lastChange = iterator.next().getUpdated();
            } else {
                lastChange = new Date();
            }
        }
        // map model for javascript parameter substitution
        IModel<Map<String, Object>> variablesModel = new AbstractReadOnlyModel<Map<String, Object>>() {
            private static final long serialVersionUID = 1L;

            private Map<String, Object> variables;

            @Override
            public Map<String, Object> getObject() {
                if (variables == null) {
                    variables = new HashMap<String, Object>();
                    variables.put("date",
                            new SimpleDateFormat("MMM dd yyyy HH:mm:ss 'GMT'").format(lastChange));
                }
                return variables;
            }
        };
        add(TextTemplateHeaderContributor.forJavaScript(GSSLayersPage.class, "timeline.js",
                variablesModel));

        publishedLayersTable = new GeoSynchronizedTypesTablePanel("table", provider);
        publishedLayersTable.setOutputMarkupId(true);
        publishedLayersTable.setItemsPerPage(10);
        add(publishedLayersTable);

        LongTasksPanel longTasksPanel = new LongTasksPanel("importingPanel") {
            private static final long serialVersionUID = 1L;

            /**
             * Override to also update {@link #publishedLayersTable} so it reflects finished tasks
             * 
             * @see org.geoserver.task.web.LongTasksPanel#onTimerInternal(org.apache.wicket.ajax.AjaxRequestTarget)
             */
            @Override
            protected void onTimerInternal(final AjaxRequestTarget target) {
                target.addComponent(publishedLayersTable);
            }
        };
        longTasksPanel.setItemsPerPage(10);
        longTasksPanel.setFilter(new LongTasksPanel.TaskFilter() {
            private static final long serialVersionUID = 1L;

            /**
             * @see org.geoserver.task.web.LongTasksPanel.TaskFilter#accept(org.geoserver.task.LongTask)
             */
            public boolean accept(LongTask<?> task) {
                return task instanceof Object;
            }

        });
        add(longTasksPanel);

        // traded this panel in favour of the timeline widget
        // add(new ChangesPanel("changesPanel"));

        // the confirm dialog
        add(removalDialog = new GeoServerDialog("dialog"));
        setHeaderPanel(headerPanel());
    }

    /**
     * Overrides to return {@code null}, as the default ajax indicator gets annoying very quickly on
     * the home page if there's some ajax timer to refresh some status, and it's not like we're
     * going to have any "save" button on the home page that could be pressed twice anyways.
     * 
     * @see IAjaxIndicatorAware#getAjaxIndicatorMarkupId()
     */
    @Override
    public String getAjaxIndicatorMarkupId() {
        return null;
    }

    private Component cachedLayerLink(String id, IModel<GSSLayerInfo> itemModel) {
        IModel<Name> nameModel = GSSLayerProvider.NAME.getModel(itemModel);
        Name layerName = nameModel.getObject();
        FeatureTypeInfo featureType = getCatalog().getFeatureTypeByName(layerName);
        Label link = new Label(id, featureType.getPrefixedName());
        return link;
    }

    protected Component headerPanel() {
        Fragment header = new Fragment(HEADER_PANEL, "header", this);

        // the add button
        header.add(new BookmarkablePageLink("addNew", NewGSSLayerPage.class));

        // the removal button
        header.add(removalLink = new GSSLayerSelectionRemovalLink("removeSelected",
                publishedLayersTable, removalDialog));
        removalLink.setEnabled(false);
        // removal.setOutputMarkupId(true);
        // removal.setEnabled(false);

        return header;
    }

    private final class GeoSynchronizedTypesTablePanel extends GeoServerTablePanel<GSSLayerInfo> {
        private static final long serialVersionUID = 1L;

        private GeoSynchronizedTypesTablePanel(final String id,
                final GeoServerDataProvider<GSSLayerInfo> dataProvider) {
            super(id, dataProvider, true);
        }

        @Override
        protected void onSelectionUpdate(AjaxRequestTarget target) {
            GSSLayersPage.this.removalLink
                    .setEnabled(publishedLayersTable.getSelection().size() > 0);
            target.addComponent(removalLink);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Component getComponentForProperty(String id, IModel itemModel,
                Property<GSSLayerInfo> property) {

            if (property == GSSLayerProvider.TYPE) {
                final CatalogIconFactory icons = CatalogIconFactory.get();
                Fragment f = new Fragment(id, "iconFragment", GSSLayersPage.this);
                GSSLayerInfo layerInfo = (GSSLayerInfo) itemModel.getObject();
                Name typeName = layerInfo.getName();
                String ns = typeName.getNamespaceURI();
                String name = typeName.getLocalPart();
                Catalog catalog = getGeoServer().getCatalog();
                FeatureTypeInfo info = catalog.getResourceByName(ns, name, FeatureTypeInfo.class);
                ResourceReference layerIcon;
                try {
                    FeatureType featureType;
                    featureType = info.getFeatureType();
                    GeometryDescriptor gd = featureType.getGeometryDescriptor();
                    layerIcon = icons.getVectoryIcon(gd);
                } catch (IOException e) {
                    layerIcon = CatalogIconFactory.UNKNOWN_ICON;
                }
                f.add(new Image("layerIcon", layerIcon));
                return f;
            } else if (property == GSSLayerProvider.NAME) {
                return cachedLayerLink(id, itemModel);
            }
            throw new IllegalArgumentException("Don't know a property named " + property.getName());
        }
    }

    private static class GSSLayerSelectionRemovalLink extends AjaxLink<GSSLayerInfo> {

        private static final long serialVersionUID = 1L;

        public GSSLayerSelectionRemovalLink(String string, GeoServerTablePanel<GSSLayerInfo> table,
                GeoServerDialog dialog) {
            super(string);
        }

        @Override
        public void onClick(final AjaxRequestTarget target) {

        }
    }

    private static class GSSLayerProvider extends GeoServerDataProvider<GSSLayerInfo> {

        private static final long serialVersionUID = 4641819017764643297L;

        static final Property<GSSLayerInfo> TYPE = new BeanProperty<GSSLayerInfo>("type",
                "geometryType") {

            private static final long serialVersionUID = 1L;

            @Override
            public Comparator<GSSLayerInfo> getComparator() {
                return new Comparator<GSSLayerInfo>() {
                    @Override
                    public int compare(GSSLayerInfo o1, GSSLayerInfo o2) {
                        Class<? extends Geometry> gt1 = o1.getGeometryType();
                        Class<? extends Geometry> gt2 = o2.getGeometryType();
                        if (gt1 == null) {
                            return gt2 == null ? 0 : -1;
                        }
                        if (gt2 == null) {
                            return 1;
                        }
                        return gt1.getName().compareTo(gt2.getName());
                    }
                };
            }
        };

        static final Property<GSSLayerInfo> NAME = new BeanProperty<GSSLayerInfo>("name", "name");

        @SuppressWarnings("unchecked")
        static final List<Property<GSSLayerInfo>> PROPERTIES = Arrays.asList(TYPE, NAME);

        /**
         * @see org.geoserver.web.wicket.GeoServerDataProvider#getItems()
         */
        @Override
        protected List<GSSLayerInfo> getItems() {
            return GSSLayerDetachableModel.getItems();
        }

        /**
         * @see org.geoserver.web.wicket.GeoServerDataProvider#getProperties()
         */
        @Override
        protected List<Property<GSSLayerInfo>> getProperties() {
            return PROPERTIES;
        }

        /**
         * @see org.geoserver.web.wicket.GeoServerDataProvider#newModel(java.lang.Object)
         */
        public IModel<GSSLayerInfo> newModel(final Object GSSLayerInfo) {
            return new GSSLayerDetachableModel((GSSLayerInfo) GSSLayerInfo);
        }

        /**
         * @see org.geoserver.web.wicket.GeoServerDataProvider#getComparator
         */
        @Override
        protected Comparator<GSSLayerInfo> getComparator(SortParam sort) {
            return super.getComparator(sort);
        }
    }

}
