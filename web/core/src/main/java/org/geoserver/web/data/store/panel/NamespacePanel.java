/* Copyright (c) 2001 - 2009 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.data.store.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.validation.FormComponentFeedbackBorder;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.geoserver.web.data.namespace.NamespaceChoiceRenderer;
import org.geoserver.web.data.namespace.NamespacesModel;

/**
 * A label + namespace dropdown form panel
 * 
 * @author Gabriel Roldan
 */
@SuppressWarnings("serial")
public class NamespacePanel extends Panel {

    private final DropDownChoice choice;

    public NamespacePanel(final String componentId, final IModel selectedItemModel,
            final IModel paramLabelModel, final boolean required) {
        // make the value of the combo field the model of this panel, for easy
        // value retrieval
        super(componentId, selectedItemModel);

        // the label
        Label label = new Label("paramName", paramLabelModel);
        add(label);

        // the drop down field, with a decorator for validations
        choice = new DropDownChoice("paramValue", selectedItemModel, new NamespacesModel(),
                new NamespaceChoiceRenderer());
        choice.setRequired(required);
        // set the label to be the paramLabelModel otherwise a validation error would look like
        // "Parameter 'paramValue' is required"
        choice.setLabel(paramLabelModel);
        choice.setOutputMarkupId(true);

        FormComponentFeedbackBorder feedback = new FormComponentFeedbackBorder("border");
        feedback.add(choice);
        add(feedback);
    }

    public DropDownChoice getFormComponent(){
        return choice;
    }
}