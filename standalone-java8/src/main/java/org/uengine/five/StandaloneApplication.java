package org.uengine.five;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.uengine.five.overriding.ActivityQueue;
import org.uengine.five.overriding.EventMappingDeployFilter;
import org.uengine.five.overriding.InstanceNameFilter;
import org.uengine.five.overriding.ServiceRegisterDeployFilter;
import org.uengine.five.overriding.SpringComponentFactory;
import org.uengine.kernel.DeployFilter;
import org.uengine.kernel.GlobalContext;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.cloud.stream.annotation.EnableBinding;
// import org.uengine.five.config.kafka.KafkaProcessor;

@SpringBootApplication
@EnableBinding(Streams.class)
@EnableFeignClients
public class StandaloneApplication {

    public static ApplicationContext applicationContext;

    public static ObjectMapper objectMapper = createTypedJsonObjectMapper();

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(StandaloneApplication.class, args);
        GlobalContext.setComponentFactory(new SpringComponentFactory());
    }

    // @Bean
    // public ServiceRegisterDeployFilter serviceRegisterDeployFilter() {
    //     return new ServiceRegisterDeployFilter();
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

    @Bean
    public ActivityQueue activityQueue() {
        return new ActivityQueue();
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