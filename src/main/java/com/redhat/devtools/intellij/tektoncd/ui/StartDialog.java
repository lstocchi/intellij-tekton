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
package com.redhat.devtools.intellij.tektoncd.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Input;
import com.redhat.devtools.intellij.tektoncd.tkn.component.field.Output;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;

public class StartDialog extends DialogWrapper {
    Logger logger = LoggerFactory.getLogger(StartDialog.class);
    private JPanel contentPane;
    private JTextField inputParamValueTxt;
    private JComboBox inputResourceValuesCB;
    private JComboBox inputParamsCB;
    private JComboBox outputsCB;
    private JComboBox outputsResourcesCB;
    private JTextArea previewTextArea;
    private JButton previewRefreshBtn;
    private JLabel outInfoMessage;
    private JLabel inInfoMessage;
    private JLabel errorLbl;
    private JComboBox inputResourcesCB;
    private JPanel inputParamsPanel;
    private JPanel inputResourcesPanel;
    private JPanel outputsPanel;
    private JLabel inputParamsLbl;
    private JLabel inputParamValueLbl;
    private JLabel outputsLbl;
    private JLabel outputsResourceLbl;
    private JLabel inputResourceValuesLbl;
    private JLabel inputResourcesLbl;
    private JLabel outputsTitle;
    private List<Input> inputs;
    private List<Resource> resources;
    private List<Output> outputs;

    private String namespace;
    private String name;

    private Map<String, String> parameters, inputResources, outputResources;

    public StartDialog(Component parent, String namespace, String name, String kind, List<Input> inputs, List<Output> outputs, List<Resource> resources) {
        super(null, parent, false, IdeModalityType.IDE);

        this.namespace = namespace;
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        this.resources = resources;
        setTitle("Start " + name);
        setOKButtonText("Start");
        init();

        boolean isPipeline = KIND_PIPELINE.equalsIgnoreCase(kind);

        // init dialog
        initInputsArea();
        initOutputsArea(isPipeline);
        updatePreview();
        registerListeners();
    }

    public static void main(String[] args) {
        StartDialog dialog = new StartDialog(null, "",  "", "", null, null, null);
        dialog.pack();
        dialog.show();
        System.exit(0);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public Map<String, String> getInputResources() {
        return inputResources;
    }

    public Map<String, String> getOutputResources() {
        return outputResources;
    }

    private void calculateArgs() {
        if (inputs != null) {
            for (Input input : inputs) {
                if (input.kind() == Input.Kind.PARAMETER) {
                    if (parameters == null) parameters = new HashMap<>();
                    String value = input.value() == null ? input.defaultValue().orElse("") : input.value();
                    parameters.put(input.name(), value);
                } else {
                    if (inputResources == null) inputResources = new HashMap<>();
                    inputResources.put(input.name(), input.value());
                }
            }
        }

        if (outputs != null) {
            for (Output output : outputs) {
                if (outputResources == null) outputResources = new HashMap<>();
                outputResources.put(output.name(), output.value());
            }
        }
    }

    private String validateInputs() {
        if (inputs == null) {
            return null;
        }

        for (Input input: inputs) {
            if (input.value() == null && !input.defaultValue().isPresent()) {
                return input.name();
            }
        }
        return null;
    }

    private String validateOutputs() {
        if (outputs == null) {
            return null;
        }

        for (Output output: outputs) {
            if (output.value() == null) {
                return output.name();
            }
        }
        return null;
    }

    @Override
    protected void doOKAction() {
        // check if all inputs/outputs have been initialized otherwise show an error
        String invalidInput = validateInputs();
        String invalidOutput = validateOutputs();
        if (invalidInput == null && invalidOutput == null) {
            calculateArgs();
            super.doOKAction();
        } else {
            String msg = "Error - No value inserted for ";
            if (invalidInput != null) {
                msg += invalidInput;
            } else {
                msg += invalidOutput;
            }
            errorLbl.setText(msg);

            errorLbl.setVisible(true);
            java.util.Timer timer = new java.util.Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    errorLbl.setVisible(false);
                }
            }, 4000);
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }


    private void initInputsArea() {
        if (inputs == null) {
            changeInputComponentVisibility(false, false, false);
            return;
        }
        for (Input input: inputs) {
            if (input.kind() == Input.Kind.PARAMETER) {
                inputParamsCB.addItem(input);
            } else {
                inputResourcesCB.addItem(input);
            }
        }

        if (inputParamsCB.getItemCount() > 0) {
            inputParamValueTxt.setText(((Input)inputParamsCB.getItemAt(0)).defaultValue().orElse(""));
        }

        if (inputResourcesCB.getItemCount() > 0) {
            fillInputResourceComboBox((Input)inputResourcesCB.getItemAt(0));
        }

        changeInputComponentVisibility(true, inputParamsCB.getItemCount() > 0, inputResourcesCB.getItemCount() > 0);
    }

    private void changeInputComponentVisibility(boolean hasInputs, boolean hasParams, boolean hasResources) {
        if (!hasInputs) {
            inInfoMessage.setVisible(true);
            return;
        }
        if (hasResources) {
            inputResourcesPanel.setVisible(true);
        }
        if (hasParams) {
            inputParamsPanel.setVisible(true);
        }
    }

    private void fillInputResourceComboBox(Input inputSelected) {
        inputResourceValuesCB.removeAll();
        for (Resource resource: resources) {
            if (resource.type().equals(inputSelected.type())) {
                inputResourceValuesCB.addItem(resource);
            }
        }
        if (inputSelected.value() != null) {
            inputResourceValuesCB.setSelectedItem(inputSelected.value());
        }
    }

    private void initOutputsArea(boolean isPipeline) {
        changeOutputComponentVisibility(outputs != null, isPipeline);
        if (outputs == null) return;

        for (Output output: outputs) {
            outputsCB.addItem(output);
        }
        fillOutResourcesComboBox(outputs.get(0));
    }

    private void changeOutputComponentVisibility(boolean hasOutput, boolean isPipeline) {
        outputsTitle.setVisible(!isPipeline);
        outInfoMessage.setVisible(!hasOutput && !isPipeline);
        outputsPanel.setVisible(hasOutput && !isPipeline);
    }

    private void fillOutResourcesComboBox(Output outputSelected) {
        outputsResourcesCB.removeAll();
        for (Resource resource: resources) {
            if (resource.type().equals(outputSelected.type())) {
                outputsResourcesCB.addItem(resource);
            }
        }
        if (outputSelected.value() != null) {
            outputsResourcesCB.setSelectedItem(outputSelected.value());
        }
    }

    private void updatePreview() {
        String preview = "";
        try {
            preview = YAMLBuilder.createPreview(inputs, outputs);
        } catch (IOException e) {
            logger.warn("Error: " + e.getLocalizedMessage());
        }
        previewTextArea.setText(preview);
    }

    private void setInputValue(String inputName, String value) {
        for (Input input: inputs) {
            if (input.name().equals(inputName)) {
                input.setValue(value);
                break;
            }
        }
    }

    private void registerListeners() {
        // listener for when preview refresh button is clicked
        previewRefreshBtn.addActionListener(actionEvent -> updatePreview());

        // listener for when value in parameters input combo box changes
        inputParamsCB.addItemListener(itemEvent -> {
            // when combo box value change update input value textbox
            if (itemEvent.getStateChange() == 1) {
                Input currentInput = (Input) itemEvent.getItem();
                String value = currentInput.value() != null ? currentInput.value() : currentInput.defaultValue().orElse("");
                inputParamValueTxt.setText(value);
            }
        });

        // listener for when focus is lost in textbox with parameter input's value
        inputParamValueTxt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                // when focus is lost, value in textbox is saved in input's value and preview is updated
                Input inputSelected = (Input) inputParamsCB.getSelectedItem();
                setInputValue(inputSelected.name(), inputParamValueTxt.getText());
                updatePreview();
            }
        });

        // listener for when value in resources input combo box changes
        inputResourcesCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when inputResourcesCB combo box value changes, inputResourceValuesCB combo box is filled with all resources of the same type as the currentInput
                Input currentInput = (Input) itemEvent.getItem();
                fillInputResourceComboBox(currentInput);
                if (currentInput.value() == null && inputResourceValuesCB.getItemCount() > 0) {
                    setInputValue(currentInput.name(), ((Resource) inputResourceValuesCB.getItemAt(0)).name());
                }
            }
        });

        // listener for when value in resources value input combo box changes
        inputResourceValuesCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when inputResourceValuesCB combo box value changes, the new value is saved and preview is updated
                Input inputSelected = (Input) inputResourcesCB.getSelectedItem();
                Resource resourceSelected = (Resource) itemEvent.getItem();
                setInputValue(inputSelected.name(), resourceSelected.name());
                updatePreview();
            }
        });

        // listener for when value in outputs combo box changes
        outputsCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when outputsCB combo box value changes, outputsResourcesCB combo box is filled with all resources of the same type as the currentOutput
                Output currentOutput = (Output) itemEvent.getItem();
                fillOutResourcesComboBox(currentOutput);
                if (currentOutput.value() == null && outputsResourcesCB.getItemCount() > 0) {
                    outputs.get(outputsCB.getSelectedIndex()).setValue(((Resource) outputsResourcesCB.getItemAt(0)).name());
                }
            }
        });

        // listener for when value in output resources combo box changes
        outputsResourcesCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == 1) {
                // when outputsResourcesCB combo box value changes, the new value is saved and preview is updated
                int outputSelectedIndex = outputsCB.getSelectedIndex();
                Resource resourceSelected = (Resource) itemEvent.getItem();
                outputs.get(outputSelectedIndex).setValue(resourceSelected.name());
                updatePreview();
            }
        });
    }
}
