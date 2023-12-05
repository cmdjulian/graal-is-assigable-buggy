package org.example;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

import static org.example.KubernetesJobService.NAMESPACE;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Config config = new ConfigBuilder()
                .withNamespace(NAMESPACE)
                .withRequestTimeout(10_000)
                .build();

        try (var client = new KubernetesClientBuilder().withConfig(config).build()) {
            KubernetesJobService deleter = new KubernetesJobService(client);
            deleter.create();
            Thread.sleep(2000);
            deleter.delete();
        }
    }
}
