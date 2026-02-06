package org.uengine.five;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.uengine.five.overriding.ActivityQueue;
import org.uengine.five.overriding.EventMappingDeployFilter;
import org.uengine.five.service.IAMCompanyRoleMapping;
import org.uengine.five.overriding.InstanceNameFilter;
import org.uengine.five.overriding.PayloadFilter;
import org.uengine.five.overriding.ServiceRegisterDeployFilter;
import org.uengine.five.overriding.SpringComponentFactory;
import org.uengine.five.service.DefinitionServiceUtil;
import org.uengine.five.service.LocalFileDefinitionServiceUtil;
import org.uengine.kernel.DeployFilter;
import org.uengine.kernel.GlobalContext;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@SpringBootApplication
@EnableBinding(Streams.class)
@EnableFeignClients
@ComponentScan(basePackages = { "org.uengine.kernel.bpmn", "org.uengine.five" })

public class ProcessServiceApplication {

    public static ApplicationContext applicationContext;

    public static ObjectMapper objectMapper = createTypedJsonObjectMapper();

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(ProcessServiceApplication.class, args);
        GlobalContext.setComponentFactory(new SpringComponentFactory());

        // Application 쪽에서 RoleMapping 구현/캐시 설정을 주입
        // (GlobalContext는 uengine.properties 기반이므로 Spring 부팅 시점에 override)
        GlobalContext.getProperties().setProperty("rolemapping.class", IAMCompanyRoleMapping.class.getName());
    }

    /**
     * RoleMapping 구현체를 Application에서 prototype으로 등록.
     * RoleMapping.create() -> GlobalContext.componentFactory 경유 시 매번 새 인스턴스를 받습니다.
     */
    @Bean
    @Scope("prototype")
    public IAMCompanyRoleMapping roleMapping() {
        return new IAMCompanyRoleMapping();
    }

    // @EventListener(ApplicationReadyEvent.class)
    // public void onApplicationEvent() {
    // // 트랜잭션 동기화 로직을 여기서 처리
    // if (TransactionSynchronizationManager.isSynchronizationActive()) {
    // TransactionSynchronizationManager.registerSynchronization(
    // new TransactionSynchronizationAdapter() {
    // @Override
    // public void afterCompletion(int status) {
    // System.out.println("afterCompletion");
    // }
    // });
    // } else {
    // System.out.println("트랜잭션 동기화가 활성화되지 않았습니다.");
    // }
    // }

    // @Bean
    // public ServiceRegisterDeployFilter serviceRegisterDeployFilter() {
    // return new ServiceRegisterDeployFilter();
    // }

    @Bean
    public DeployFilter serviceRegisterDeployFilter() {
        return new ServiceRegisterDeployFilter();
    }

    @Bean
    public DeployFilter eventMappingDeployFilter() {
        return new EventMappingDeployFilter();
    }

    @Bean
    public InstanceNameFilter instanceNameFilter() {
        return new InstanceNameFilter();
    }

    @Value("${filter.payload.enabled:true}")
    private boolean isPayloadFilterEnabled;

    @Bean
    @ConditionalOnProperty(name = "filter.payload.enabled", havingValue = "true", matchIfMissing = true)
    public PayloadFilter payloadFilter() {
        return new PayloadFilter();
    }

    @Bean
    public ActivityQueue activityQueue() {
        return new ActivityQueue();
    }

    @Bean
    @Primary
    public DefinitionServiceUtil definitionServiceUtil() {
        // return new SupabaseDefinitionServiceUtil();
        return new LocalFileDefinitionServiceUtil();
    }

    public static ObjectMapper createTypedJsonObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setVisibilityChecker(objectMapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // ignore null
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT); // ignore zero and false when it is int
                                                                                 // or boolean
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS, "_type");
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        return objectMapper;
    }

}