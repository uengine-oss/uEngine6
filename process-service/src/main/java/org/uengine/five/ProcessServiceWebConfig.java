package org.uengine.five;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.spel.spi.EvaluationContextExtension;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.uengine.five.overriding.InstanceDataAppendingActivityFilter;
import org.uengine.five.overriding.InstanceServiceLocalImpl;
import org.uengine.five.overriding.JPAProcessInstance;
import org.uengine.five.overriding.JPAWorkList;
import org.uengine.five.spring.TenantSecurityEvaluationContextExtension;
import org.uengine.kernel.ActivityFilter;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.modeling.resource.CachedResourceManager;
import org.uengine.modeling.resource.LocalFileStorage;
import org.uengine.modeling.resource.ResourceManager;
import org.uengine.modeling.resource.Storage;
import org.uengine.processmanager.InstanceServiceLocal;
import org.uengine.webservices.worklist.WorkList;

@EnableWebMvc
@Configuration
public class ProcessServiceWebConfig {

    // @Override
    // public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // registry.setOrder(10);
    // registry.addResourceHandler("/**").addResourceLocations("classpath:/dev/dist/");
    // super.addResourceHandlers(registry);
    // }
    //
    // @Override
    // public void addViewControllers(ViewControllerRegistry registry) {
    // registry.addViewController("/").setViewName("forward:/index.html");
    // super.addViewControllers(registry);
    // }

    @Bean
    public ResourceManager resourceManager() {
        ResourceManager resourceManager = new CachedResourceManager();
        resourceManager.setStorage(storage());
        return resourceManager;
    }

    // @Bean
    // public DataSource dataSource() {
    // final Properties pool = new Properties();
    // pool.put("driverClassName", "com.mysql.jdbc.Driver");
    // pool.put("url",
    // "jdbc:mysql://localhost:3306/uengine?useUnicode=true&characterEncoding=UTF8&useOldAliasMetadataBehavior=true");
    // pool.put("username", "root");
    // pool.put("password", "");
    // pool.put("minIdle", 1);
    // try {
    // return new
    // org.apache.tomcat.jdbc.pool.DataSourceFactory().createDataSource(pool);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }

    // @Bean
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
        // storage.setBasePath("/oce/repository");
        // String UserName = System.getenv("USER");
        // storage.setBasePath("/Users/" + UserName);

        storage.setBasePath(basePath);

        return storage;
    }

    @Value("${uengine.definition.basePath}") // 추가된 부분
    String basePath = "";
    // @Bean
    // public TenantAwareFilter tenantAwareFilter(){
    // return new TenantAwareFilter();
    // }

    // @Bean
    // public MetadataService metadataService() {
    // DefaultMetadataService metadataService = new DefaultMetadataService();
    // metadataService.setResourceManager(resourceManager());

    // return metadataService;
    // }

    @Bean
    @Scope("prototype")
    public ProcessInstance processInstance(ProcessDefinition procDefinition, String instanceId, Map options)
            throws Exception {
        return new JPAProcessInstance(procDefinition, instanceId, options);
    }

    @Bean
    public ActivityFilter instanceDataAppendingFilter() {
        return new InstanceDataAppendingActivityFilter();
    }

    @Bean
    public WorkList workList() {
        return new JPAWorkList();
    }

    // @Bean
    // public Filter webFilter() {
    // return new TenantAwareFilter();
    // }

    // @Bean
    // public ProcessDefinitionFactory processDefinitionFactory() {
    // return new ProcessDefinitionFactory();
    // }

    // @Bean
    // public ProcessManagerRemote processManagerRemote() {
    // return new ProcessManagerBean();
    // }

    // @Bean
    // public TimerEventJob timerEventJob() {
    // return new TimerEventJob();
    // }

    @Bean
    public InstanceServiceLocal instanceServiceLocal() {
        return new InstanceServiceLocalImpl();
    }

    @Bean
    EvaluationContextExtension securityExtension() {
        return new TenantSecurityEvaluationContextExtension();
    }
}
