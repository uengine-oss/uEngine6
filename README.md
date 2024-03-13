# uEngine5-bpm



## Deploy Example Process Definitions

Firstly, run the definition-service:
```
cd definition-service
mvn spring-boot:run
```

And then, register the process definition to the definition-service with the following command:
```

 http :9093/definition/raw/sales/simpleProcess.xml < definition-samples/simpleProcess.xml 

```


## Execute Process Instance

Run the process-service:
```
cd process-service
mvn spring-boot:run
```

Start a process:
```
http :9094/instance processDefinitionId="sales/simpleProcess.bpmn" roleMappings[0][name]="initiator" roleMappings[0][endpoint]="initiator@uengine.org" roleMappings[0][resourceName]="Initiator"
```

