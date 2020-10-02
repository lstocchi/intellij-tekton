/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.ui.wizard;

import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.utils.model.actions.ActionToRunModel;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;


import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.BORDER_LABEL_NAME;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.MARGIN_TOP_35;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.RED_BORDER_SHOW_ERROR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROW_DIMENSION;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.ROW_DIMENSION_ERROR;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_10;
import static com.redhat.devtools.intellij.tektoncd.ui.UIConstants.TIMES_PLAIN_14;

public class ParametersStep extends BaseStep {

    Map<String, JTextField> textFields;

    public ParametersStep(ActionToRunModel model) {
        super("Parameters", model);
    }

    @Override
    public boolean isComplete() {
        if (textFields == null) return false;
        AtomicBoolean isComplete = new AtomicBoolean(true);
        final int[] row = {1};
        textFields.keySet().stream().forEach(param -> {
            JTextField field = textFields.get(param);
            if (!isValid(field)) {
                field.setBorder(RED_BORDER_SHOW_ERROR);
                JLabel lblErrorText = new JLabel("Please enter a value.");
                lblErrorText.setForeground(Color.red);
                addComponent(lblErrorText, TIMES_PLAIN_10, MARGIN_TOP_35, ROW_DIMENSION_ERROR, 0, row[0], GridBagConstraints.PAGE_END);
                errorFieldsByRow.put(row[0], lblErrorText);
                isComplete.set(false);
            } else {
                setInputValue(model.getParams(), param, field.getText());
            }
            row[0] += 2;
        });
        return isComplete.get();
    }

    @Override
    public String getHelpId() {
        return "https://github.com/tektoncd/pipeline/blob/master/docs/pipelines.md#specifying-parameters";
    }

    public void setContent() {
        textFields = new LinkedHashMap<>();
        final int[] row = {0};

        model.getParams().stream().filter(input -> input.kind() == Input.Kind.PARAMETER).forEach(input -> {
            String label = "<html><span style=\\\"font-family:serif;font-size:10px;font-weight:bold;\\\">" + input.name() + "</span>  <span style=\\\"font-family:serif;font-size:10;font-weight:normal;font-style:italic;\\\">(" + input.type() + ")</span></html>";
            String tooltip = input.description().isPresent() ? input.description().get() + "\n" : "";
            if (input.type().equals("string")) {
                tooltip += "The parameter " + input.name() + " expects a string value.";
            } else {
                tooltip += "The parameter " + input.name() + " expects an array value (e.g. val1,val2,val3 ...).";
            }
            JLabel lblNameParam = new JLabel(label);
            addComponent(lblNameParam, null, BORDER_LABEL_NAME, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);
            addTooltip(lblNameParam, tooltip);
            row[0] += 1;

            JTextField txtValueParam = new JTextField(input.defaultValue().orElse(""));
            textFields.put(input.name(), txtValueParam);
            txtValueParam = (JTextField) addComponent(txtValueParam, TIMES_PLAIN_14, null, ROW_DIMENSION, 0, row[0], GridBagConstraints.NORTH);
            addListener(input.name(), txtValueParam, txtValueParam.getBorder(), row[0]);
            row[0] += 1;
        });
    }

    private void addListener(String idParam, JTextField txtValueParam, Border defaultBorder, int row) {
        // listen to when the focus is lost by the textbox and update the model so the preview shows the updated value
        txtValueParam.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                setInputValue(model.getParams(), idParam, txtValueParam.getText());
                // reset error graphics if an error occurred earlier
                if (isValid(txtValueParam)) {
                    txtValueParam.setBorder(defaultBorder);
                    if (errorFieldsByRow.containsKey(row)) {
                        deleteComponent(errorFieldsByRow.get(row));
                    }

                }
                fireStateChanged();
            }
        });
    }

    private boolean isValid(JTextField component) {
        return !component.getText().isEmpty();
    }
}
