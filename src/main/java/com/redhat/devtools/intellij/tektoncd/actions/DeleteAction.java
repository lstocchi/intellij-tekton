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
package com.redhat.devtools.intellij.tektoncd.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;

import com.redhat.devtools.intellij.tektoncd.tree.ClusterTaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTriggerBindingNode;
import com.redhat.devtools.intellij.tektoncd.tree.ConditionNode;
import com.redhat.devtools.intellij.tektoncd.tree.EventListenerNode;
import com.redhat.devtools.intellij.tektoncd.tree.NamespaceNode;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineNode;
import com.redhat.devtools.intellij.tektoncd.tree.ResourceNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerBindingNode;
import com.redhat.devtools.intellij.tektoncd.tree.TriggerTemplateNode;


import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class DeleteAction extends TektonAction {
    public DeleteAction() { super(true, TaskNode.class, PipelineNode.class, ResourceNode.class, ClusterTaskNode.class, ConditionNode.class, TriggerTemplateNode.class, TriggerBindingNode.class, ClusterTriggerBindingNode.class, EventListenerNode.class); }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath[] path, Object[] selected, Tkn tkncli) {
        ParentableNode<? extends ParentableNode<NamespaceNode>>[] elements = Arrays.stream(selected).map(item -> getElement(item)).toArray(ParentableNode[]::new);
        int resultDialog = UIHelper.executeInUI(() -> {
            String name = "";
            String dialogText = "";
            if (elements.length == 1) {
                name = elements[0].getName();
                dialogText = elements[0].getClass().getSimpleName().toLowerCase().replace("node", "") + " " + name + " ?";
            } else {
                dialogText = "the following items?\n";
                for (ParentableNode element: elements) {
                    dialogText += element.getName() + "\n";
                }
            }

            return Messages.showYesNoDialog("Are you sure you want to delete " + dialogText,
                    "Delete " + name,
                    null
            );
        });

        CompletableFuture.runAsync(() -> {
            if (resultDialog == Messages.OK) {
                for(ParentableNode<? extends ParentableNode<NamespaceNode>> element : elements) {
                    try {
                        String namespace = element.getNamespace();
                        if (element instanceof PipelineNode) {
                            tkncli.deletePipeline(namespace, element.getName());
                        } else if (element instanceof ResourceNode) {
                            tkncli.deleteResource(namespace, element.getName());
                        } else if (element instanceof TaskNode) {
                            tkncli.deleteTask(namespace, element.getName());
                        } else if (element instanceof ClusterTaskNode) {
                            tkncli.deleteClusterTask(element.getName());
                        } else if (element instanceof ConditionNode) {
                            tkncli.deleteCondition(namespace, element.getName());
                        } else if (element instanceof TriggerTemplateNode) {
                            tkncli.deleteTriggerTemplate(namespace, element.getName());
                        } else if (element instanceof TriggerBindingNode) {
                            tkncli.deleteTriggerBinding(namespace, element.getName());
                        } else if (element instanceof ClusterTriggerBindingNode) {
                            tkncli.deleteClusterTriggerBinding(namespace, element.getName());
                        } else if (element instanceof EventListenerNode) {
                            tkncli.deleteEventListener(namespace, element.getName());
                        }
                        ((TektonTreeStructure) getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY)).fireModified(element.getParent());
                    } catch (IOException e) {
                        UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
                    }
                }
            }
        });

    }
}