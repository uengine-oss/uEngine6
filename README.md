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
http :9094/instance processDefinitionId="sales/testProcess.xml" "roleMappings[0][name]=initiator" "roleMappings[0][endpoints][0]=initiator@uengine.org" "roleMappings[0][resourceNames][0]=Initiator" Authorization:Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJhdXRoZW50aWNhdGVkIiwiZXhwIjoxNzEzMTUzODU0LCJpYXQiOjE3MTMxNTAyNTQsImlzcyI6Imh0dHA6Ly8xMjcuMC4wLjE6NTQzMjEvYXV0aC92MSIsInN1YiI6IjY3NjFhZTEyLTlkZjYtNDg5Mi1iMGZiLTFlNTNlYjJhMzljNiIsImVtYWlsIjoieWFuZzE3NjJAdWVuZ2luZS5vcmciLCJwaG9uZSI6IiIsImFwcF9tZXRhZGF0YSI6eyJwcm92aWRlciI6ImVtYWlsIiwicHJvdmlkZXJzIjpbImVtYWlsIl19LCJ1c2VyX21ldGFkYXRhIjp7Im5hbWUiOiJ5YW5nMTc2MiJ9LCJyb2xlIjoiYXV0aGVudGljYXRlZCIsImFhbCI6ImFhbDEiLCJhbXIiOlt7Im1ldGhvZCI6InBhc3N3b3JkIiwidGltZXN0YW1wIjoxNzEzMTUwMjU0fV0sInNlc3Npb25faWQiOiIwMzg0ODFkYy03Y2JmLTRiODQtYWNmYS00NzMzOTZhMWI0MzIifQ.izL0kiUzAUh1HNsCl4ahm_UiindR2CSr5iMtSyCKPhU'
```

Confirm the RoleMapping:
```
http http://localhost:9094/instance/1/role-mapping/
```
> must return the initiator's endpoint value as "initiator@uengine.org"


