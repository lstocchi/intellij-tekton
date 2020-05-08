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
package com.redhat.devtools.intellij.tektoncd.tkn;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.List;


public class ConditionDeserializer extends StdNodeBasedDeserializer<Condition> {
    public ConditionDeserializer() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, Condition.class));
    }

    @Override
    public Condition convert(JsonNode root, DeserializationContext ctxt) {
        String apiVersion = root.get("apiVersion").asText();
        String name = root.get("metadata").get("name").asText();
        return new Condition(apiVersion, name);
    }
}
