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
package com.redhat.devtools.intellij.tektoncd.actions.editor;

public class RemoveYAMLClutterAction extends YAMLClutterAction {
    protected RemoveYAMLClutterAction() {
        super(new RemoveYAMLClutterActionHandler());
    }

    @Override
    public boolean isDisabled(boolean isAlreadyCleaned) {
        return isAlreadyCleaned;
    }
}
