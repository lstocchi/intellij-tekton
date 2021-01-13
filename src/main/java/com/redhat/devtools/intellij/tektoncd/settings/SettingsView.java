/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.settings;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import javax.swing.JPanel;

public class SettingsView {
    private final JPanel myMainPanel;
    private final JBCheckBox chkDisplayPipelineRunResultAsNotification = new JBCheckBox("Display PipelineRuns result as notification");
    private final JBCheckBox chkEnableDeleteAllRelatedResourcesAsDefault = new JBCheckBox("Enable delete all related resource as default");
    private final JBCheckBox chkShowStartWizardWithNoInputs = new JBCheckBox("Show start wizard when a pipeline/task have no inputs (this allows to set up service accounts)");

    public SettingsView() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(chkDisplayPipelineRunResultAsNotification, 1)
                .addComponent(chkEnableDeleteAllRelatedResourcesAsDefault, 1)
                .addComponent(chkShowStartWizardWithNoInputs, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public boolean getDisplayPipelineRunResultAsNotification() {
        return chkDisplayPipelineRunResultAsNotification.isSelected();
    }

    public void setDisplayPipelineRunResultAsNotification(boolean newStatus) {
        chkDisplayPipelineRunResultAsNotification.setSelected(newStatus);
    }

    public boolean getEnableDeleteAllRelatedResourcesAsDefault() {
        return chkEnableDeleteAllRelatedResourcesAsDefault.isSelected();
    }

    public void setEnableDeleteAllRelatedResourcesAsDefault(boolean newStatus) {
        chkEnableDeleteAllRelatedResourcesAsDefault.setSelected(newStatus);
    }

    public boolean getShowStartWizardWithNoInputs() {
        return chkShowStartWizardWithNoInputs.isSelected();
    }

    public void setShowStartWizardWithNoInputs(boolean newStatus) {
        chkShowStartWizardWithNoInputs.setSelected(newStatus);
    }
}
