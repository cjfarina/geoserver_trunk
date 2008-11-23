package org.geoserver.web.wicket;

import org.apache.wicket.model.IModel;
import org.apache.wicket.Component;

public class DelegatingModel implements IModel {
    Component myComponent; 

    public DelegatingModel(Component c){
        myComponent = c;
    }

    public Object getObject(){
        return myComponent.getModel().getObject();
    }

    public void setObject(Object o){
        myComponent.getModel().setObject(o);
    }

    public void detach(){
        myComponent.getModel().detach();
    }
}


