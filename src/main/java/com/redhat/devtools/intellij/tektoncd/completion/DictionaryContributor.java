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
package com.redhat.devtools.intellij.tektoncd.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLScalar;

public class DictionaryContributor extends CompletionContributor {
    public DictionaryContributor() {
        // conditions
        extend(CompletionType.BASIC,
                YamlElementPatternHelper.getSingleLineScalarKey("conditionRef"),
                new ConditionCompletionProvider());
        // tasks
        extend(CompletionType.BASIC,
                YamlElementPatternHelper.getAfterParentScalarKey("taskRef", "name"),
                new TaskCompletionProvider());
        // runAfter
        extend(CompletionType.BASIC,
                YamlElementPatternHelper.getMultipleLineScalarKey("runAfter"),
                new RunAfterCompletionProvider());
        // resource in pipeline
        extend(CompletionType.BASIC,
                YamlElementPatternHelper.getAfterParentScalarKeyInSequence("resource", "resources", "inputs", "outputs"),
                new ResourceInPipelineCompletionProvider());
        // code completion not related to a specific place in a file. It doesn't depend on any tag
        extend(CompletionType.BASIC,
                PlatformPatterns
                        .psiElement(YAMLTokenTypes.TEXT)
                        .withParent(
                                PlatformPatterns
                                        .psiElement(YAMLScalar.class)),
                new GeneralCompletionProvider());
    }
}
