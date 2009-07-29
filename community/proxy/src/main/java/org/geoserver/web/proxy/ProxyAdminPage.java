package org.geoserver.web.proxy;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.geoserver.proxy.ProxyConfig;
import org.geoserver.web.GeoServerSecuredPage;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;

public class ProxyAdminPage extends GeoServerSecuredPage {
    /*NOTE & DANGER:
     * There is nothing here that will guarantee any form of consistency in case of multiple people
     * editing the proxy's configuration. Probably don't do that.
     */
    
    GeoServerTablePanel<String> hostnameFilterTable;
    HostRemovalLink removal;


    @SuppressWarnings("serial")
    public ProxyAdminPage() {
        HostnameProvider hostnameProvider = new HostnameProvider(); 
        
        ProxyConfig config = ProxyConfig.loadConfFromDisk();
        
//        ProxyForm proxyForm = new ProxyForm("proxyForm");
//        add(proxyForm);
        // Add radio buttons for mode
//        RadioChoice modeChoices = new RadioChoice("modes", new PropertyModel(config, "mode"), Mode.modeNames());
//        proxyForm.add(modeChoices);
       
        hostnameFilterTable = 
            new GeoServerTablePanel<String>("hostnameTable", hostnameProvider, true) {
            @Override
            protected Component getComponentForProperty(String id, IModel itemModel,
                    Property<String> property) {
                return new Label(id, property.getModel(itemModel));
            }
            //tell the table to enable the remove button when items are selected
            @Override
            protected void onSelectionUpdate(AjaxRequestTarget target) {
                removal.setEnabled(hostnameFilterTable.getSelection().size() > 0);
                target.addComponent(removal);
            }  
        };
        hostnameFilterTable.setOutputMarkupId(true);
        add(hostnameFilterTable);
        
        // the add button
        add(new BookmarkablePageLink("addNew", HostnameNewPage.class));
        // the removal button
        removal = new HostRemovalLink("removeSelected", hostnameFilterTable, config);
        add(removal);        
        removal.setOutputMarkupId(true);
        removal.setEnabled(false);

    }
    
    @SuppressWarnings("serial")
    public final class ProxyForm extends Form{
        public ProxyForm(final String componentName)
        {
            super(componentName);
        }
        
        public void onSubmit()
        {
            // TODO: Add a submit button, and make it somehow call this vvv.
            
            //ProxyConfig.writeConfigToDisk(config);
        }
    }
 
    @SuppressWarnings("serial")
    public class HostRemovalLink extends AjaxLink {    
        GeoServerTablePanel<String> tableObjects;
        ProxyConfig config;

        public HostRemovalLink(String id, GeoServerTablePanel<String> tableObjects, ProxyConfig config) {
            super(id);
            this.tableObjects = tableObjects;
            this.config = config;
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            // see if the user selected anything
            final List<String> selection = tableObjects.getSelection();
            if(selection.size() == 0)
                return;
            
            //remove selected hostnames from list
            for (String hostname : selection) {
                config.hostnameWhitelist.remove(hostname);
            }
            //write changes to disk
            ProxyConfig.writeConfigToDisk(config);
            
            //disable the removal link, since nothing is selected any more
            setEnabled(false);
            target.addComponent(HostRemovalLink.this);
            target.addComponent(tableObjects);
        }
    }
}