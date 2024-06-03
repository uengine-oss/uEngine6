package org.uengine.five;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
//import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
//import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.uengine.five.overriding.EventSendingDeployFilter;
import org.uengine.kernel.DeployFilter;
import org.uengine.modeling.resource.DefaultResource;
import org.uengine.modeling.resource.LocalFileStorage;
import org.uengine.modeling.resource.ResourceManager;
import org.uengine.modeling.resource.SimpleVersionManager;
import org.uengine.modeling.resource.Storage;
import org.uengine.modeling.resource.VersionManager;

@SpringBootApplication
// @EnableBinding(KafkaProcessor.class)
@EnableFeignClients
public class DefinitionServiceApplication {

    public static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(DefinitionServiceApplication.class, args);
    }

    @Bean
    public ResourceManager resourceManager() {
        ResourceManager resourceManager = new ResourceManager();
        resourceManager.setStorage(storage());
        return resourceManager;
    }

    @Value("${uengine.definition.basePath}")
    String basePath;

    @Bean
    /**
     *
     * <bean class="CouchbaseStorage">
     * <property name="basePath" value="/"/>
     * <property name="bucketName" value="default"/>
     * <property name="serverIp" value="localhost"/>
     * </bean>
     */
    public Storage storage() {
        LocalFileStorage storage = new LocalFileStorage();
        // String userName = System.getenv("USER");
        // storage.setBasePath("/Users/" + userName);
        storage.setBasePath(basePath);
        try {
            System.out.println("-------------------> " + storage.exists(new DefaultResource("."))
                    + " ---> file system is mounted.");
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }
        ;

        return storage;
    }

    @Bean
    @Scope("prototype")
    public VersionManager versionManager() {
        SimpleVersionManager simpleVersionManager = new SimpleVersionManager();
        simpleVersionManager.setAppName("codi");
        // simpleVersionManager.setModuleName("definition");

        return simpleVersionManager;
    }

    // -------------------------------

    @Bean
    public DeployFilter serviceRegisterDeployFilter() {
        return new EventSendingDeployFilter();
    }

}