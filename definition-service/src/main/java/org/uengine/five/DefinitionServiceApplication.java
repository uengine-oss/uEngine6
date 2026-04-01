package org.uengine.five;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.uengine.five.overriding.EventSendingDeployFilter;
import org.uengine.five.repository.ProcDefRepository;
import org.uengine.five.repository.ProcDefVersionRepository;
import org.uengine.five.service.DefinitionActorNameProvider;
import org.uengine.five.service.OracleStorage;
import org.uengine.kernel.DeployFilter;
import org.uengine.kernel.GlobalContext;
import org.uengine.modeling.resource.DefaultResource;
import org.uengine.modeling.resource.LocalFileStorage;
import org.uengine.modeling.resource.ResourceManager;
import org.uengine.modeling.resource.SimpleVersionManager;
import org.uengine.modeling.resource.Storage;
import org.uengine.modeling.resource.VersionManager;


@EnableBinding(Streams.class)
@SpringBootApplication
@EnableFeignClients
public class DefinitionServiceApplication {

    public static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void main(String[] args) {
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
        applicationContext = SpringApplication.run(DefinitionServiceApplication.class, args);
        GlobalContext.setComponentFactory(new SpringComponentFactory());
    }

    @Bean
    @Primary
    public ResourceManager resourceManager(Storage storage) {
        ResourceManager resourceManager = new ResourceManager();
        resourceManager.setStorage(storage);
        return resourceManager;
    }

    @Value("${uengine.definition.basePath}")
    String basePath;

    @Bean
    @Profile("!oracle")
    public LocalFileStorage localFileStorage() {
        LocalFileStorage local = new LocalFileStorage();
        local.setBasePath(basePath);
        try {
            local.exists(new DefaultResource("."));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return local;
    }

    @Bean
    @Profile("oracle")
    public OracleStorage oracleStorage(ProcDefRepository procDefRepository,
            ProcDefVersionRepository procDefVersionRepository,
            DefinitionActorNameProvider definitionActorNameProvider) {
        OracleStorage oracle = new OracleStorage(procDefRepository, procDefVersionRepository,
                definitionActorNameProvider);
        oracle.setBasePath(basePath);
        try {
            oracle.exists(new DefaultResource("."));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return oracle;
    }

    @Bean
    @Primary
    public Storage storage(Environment environment,
            ObjectProvider<LocalFileStorage> localFileStorageProvider,
            ObjectProvider<OracleStorage> oracleStorageProvider) {
        Storage storage = (environment != null && environment.acceptsProfiles(Profiles.of("oracle")))
                ? oracleStorageProvider.getIfAvailable()
                : localFileStorageProvider.getIfAvailable();
        if (storage == null) {
            throw new IllegalStateException("No Storage bean available for current profile");
        }
        return storage;
    }

    @Bean
    @Scope("prototype")
    public VersionManager versionManager() {
        SimpleVersionManager simpleVersionManager = new SimpleVersionManager();
        simpleVersionManager.setAppName("codi");
        return simpleVersionManager;
    }

    @Bean
    public DeployFilter serviceRegisterDeployFilter() {
        return new EventSendingDeployFilter();
    }
}