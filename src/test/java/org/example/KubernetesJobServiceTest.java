package org.example;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Files;
import java.nio.file.Path;

@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KubernetesJobServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesJobServiceTest.class);

    @Container
    private final K3sContainer k3s;

    public KubernetesJobServiceTest() {
        var container = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.25.10-k3s1"))
                .withLogConsumer(new Slf4jLogConsumer(logger));

        // we have to make sure here, that we mount mapper for btrfs on linux
        // https://k3d.io/v5.4.6/faq/faq/#issues-with-btrfs
        if (Files.exists(Path.of("/dev/mapper"))) {
            //noinspection deprecation
            k3s = container.withFileSystemBind("/dev/mapper", "/dev/mapper");
        } else {
            k3s = container;
        }
    }

    @Test
    void delete() {
        String kubeConfigYaml = k3s.getKubeConfigYaml();
        Config config = Config.fromKubeconfig(kubeConfigYaml);
        try (var client = new KubernetesClientBuilder().withConfig(config).build()) {
            KubernetesJobService deleter = new KubernetesJobService(client);
            Assertions.assertDoesNotThrow(() -> {
                deleter.create();
                deleter.delete();
            });
        }
    }
}
