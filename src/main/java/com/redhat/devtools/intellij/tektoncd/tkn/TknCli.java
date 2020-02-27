/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.tkn;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.redhat.devtools.intellij.common.utils.DownloadHelper;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.client.OpenShiftClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TknCli implements Tkn {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(new JsonFactory());

    static {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(List.class, new TaskRunDeserializer());
        JSON_MAPPER.registerModule(module);
    }
    /**
     * Home sub folder for the plugin
     */
    public static final String PLUGIN_FOLDER = ".tkn";

    private String command;

    private TknCli() throws IOException {
        command = getCommand();
    }

    private static Tkn INSTANCE;

    public static final Tkn get() throws IOException {
        if (INSTANCE == null) {
            INSTANCE = new TknCli();
        }
        return INSTANCE;
    }

    public String getCommand() throws IOException {
        if (command == null) {
            command = getTknCommand();
        }
        return command;
    }

    private String getTknCommand() throws IOException {
        return DownloadHelper.getInstance().downloadIfRequired("tkn", TknCli.class.getResource("/tkn.json"));
    }

    @Override
    public boolean isTektonAware(KubernetesClient client) {
        return client.rootPaths().getPaths().stream().filter(path -> path.endsWith("tekton.dev")).findFirst().isPresent();
    }

    @Override
    public List<String> getNamespaces(KubernetesClient client) throws IOException {
        if (client.isAdaptable(OpenShiftClient.class)) {
            return client.adapt(OpenShiftClient.class).projects().list().getItems().stream().map(project -> project.getMetadata().getName()).collect(Collectors.toList());
        } else {
            return client.namespaces().list().getItems().stream().map(namespace -> namespace.getMetadata().getName()).collect(Collectors.toList());
        }
    }

    @Override
    public List<String> getTasks(String namespace) throws IOException {
        String output = ExecHelper.execute(command, "task", "ls", "-n", namespace, "-o", "jsonpath={.items[*].metadata.name}");
        return Arrays.asList(output.split("\\s+"));
    }

    @Override
    public List<TaskRun> getTaskRuns(String namespace, String task) throws IOException {
        String json = ExecHelper.execute(command, "taskrun", "ls", task, "-n", namespace, "-o", "json");
        return JSON_MAPPER.readValue(json, new TypeReference<List<TaskRun>>() {});
    }
}
