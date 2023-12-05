package org.example;

import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesJobService {

    public static final String NAMESPACE = "default";
    private static final Logger logger = LoggerFactory.getLogger(KubernetesJobService.class);

    private final KubernetesClient client;

    public KubernetesJobService(KubernetesClient client) {
        this.client = client;
    }

    public void create() {
        Job job = new JobBuilder()
                .withNewMetadata()
                .withName("test")
                .endMetadata()
                .withNewSpec()
                .withNewTemplate()
                .withNewSpec()
                .addNewContainer()
                .withName("hello-world")
                .withImage("bash")
                .addToCommand("/usr/local/bin/bash")
                .addToCommand("-c")
                .addToCommand("echo Hello World")
                .endContainer()
                .withRestartPolicy("Never")
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();

        client.resource(job).inNamespace(NAMESPACE).create();
    }

    public void delete() {
        try {
            client.batch().v1().jobs()
                    .inNamespace(NAMESPACE)
                    .withName("test")
                    .withGracePeriod(0)
                    .withPropagationPolicy(DeletionPropagation.FOREGROUND)
                    .delete();

            waitForDeletion();
        } catch (Exception e) {
            logger.warn("exception encountered for delete", e);
            // noop for cleanup at start
        }
    }

    private void waitForDeletion() {
        // Wait for deletion
        while (client.batch().v1().jobs().inNamespace(NAMESPACE).withName("test").get() != null) {
            // job still exists, wait and check again
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
