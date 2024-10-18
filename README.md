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
http :9094/instance processDefinitionId="sales/testProcess.xml" "roleMappings[0][name]=initiator" "roleMappings[0][endpoints][0]=initiator@uengine.org" "roleMappings[0][resourceNames][0]=Initiator" "Authorization:Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJhdXRoZW50aWNhdGVkIiwiZXhwIjoxNzEzMTUzODU0LCJpYXQiOjE3MTMxNTAyNTQsImlzcyI6Imh0dHA6Ly8xMjcuMC4wLjE6NTQzMjEvYXV0aC92MSIsInN1YiI6IjY3NjFhZTEyLTlkZjYtNDg5Mi1iMGZiLTFlNTNlYjJhMzljNiIsImVtYWlsIjoieWFuZzE3NjJAdWVuZ2luZS5vcmciLCJwaG9uZSI6IiIsImFwcF9tZXRhZGF0YSI6eyJwcm92aWRlciI6ImVtYWlsIiwicHJvdmlkZXJzIjpbImVtYWlsIl19LCJ1c2VyX21ldGFkYXRhIjp7Im5hbWUiOiJ5YW5nMTc2MiJ9LCJyb2xlIjoiYXV0aGVudGljYXRlZCIsImFhbCI6ImFhbDEiLCJhbXIiOlt7Im1ldGhvZCI6InBhc3N3b3JkIiwidGltZXN0YW1wIjoxNzEzMTUwMjU0fV0sInNlc3Npb25faWQiOiIwMzg0ODFkYy03Y2JmLTRiODQtYWNmYS00NzMzOTZhMWI0MzIifQ.izL0kiUzAUh1HNsCl4ahm_UiindR2CSr5iMtSyCKPhU"
```

Confirm the RoleMapping:

```
http http://localhost:9094/instance/1/role-mapping/
```

> must return the initiator's endpoint value as "initiator@uengine.org"

http POST :9094/instance 'processDefinitionId=sales/testLaneProcess.xml' 'roleMappings[0][name]=initiator' 'roleMappings[0][endpoints][0]=manager' 'roleMappings[0][resourceNames][0]=Initiator' 'Authorization:Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJJUmdaSjhxSW9EV3RNcDMteGJhUVIwcFVhYVUxZFhlNThYNW1PVmh6NkFJIn0.eyJleHAiOjE3MTI5MDA3NzEsImlhdCI6MTcxMjkwMDQ3MSwiYXV0aF90aW1lIjoxNzEyODk3MjI1LCJqdGkiOiIzNmQ1NDVlMS01MDVjLTQ1ZWItYWYzNi1hMjg5MzMyOTRmZDUiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwOTAvcmVhbG1zL3VlbmdpbmU2IiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjU1MzRmYmMwLWZmNWYtNDk3MS1hNzEwLWRhNjk5NWFkNGM2YiIsInR5cCI6IkJlYXJlciIsImF6cCI6InVlbmdpbmUiLCJub25jZSI6IjUzZTJkMTc2LWY0MGEtNDM2YS1hYjdhLWQ5NWU0OTBjYWY0ZSIsInNlc3Npb25fc3RhdGUiOiJmOWNlMjhjMy05YjY4LTQyYWItOTQ5Zi1jZmYxNTNlMGJmZGEiLCJhY3IiOiIwIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm1hbmFnZXIiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy11ZW5naW5lNiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUiLCJzaWQiOiJmOWNlMjhjMy05YjY4LTQyYWItOTQ5Zi1jZmYxNTNlMGJmZGEiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6InRlc3RlciIsImVtYWlsIjoiaW5pdGlhdG9yQHVlbmdpbmUub3JnIn0.BAkPxT1EmuSshUlvzkD6jTUKWUb8HgSbX6iqdEok1ou478g1IBA0UEqa3pdcYeVMnYS0153P4XgG1gnaQRVCNSqHqjxFXlQrWNDU2V5Y71rJmn8AkvTCHojJbo0cM5kNTc3HwMaJPE1C3Ira8kBUPqJTtspUh7RWI1WADmV59JYdlI_eaAlnFJdvrEAxErK3vgWPoSO2gJSYCL0_nH1TGTcTRENm8wOSvQjCu98aJLey_pQhdTJ1bM8jUlOVhJlXBKcz-zmPJmq2cKa0qA4s8Y8r8DfrdMgQsdUU339E2GZ9fhCmvEhRQ0023D3GHZgiibRIEKZO_35a1iRaaQDIzg'


http POST :9094/instance processDefinitionId="sales/testLaneProcess.xml" roleMappings[0][name]="initiator" roleMappings[0][endpoints][0]="manager" roleMappings[0][resourceNames][0]="Initiator" 'Authorization:Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJJUmdaSjhxSW9EV3RNcDMteGJhUVIwcFVhYVUxZFhlNThYNW1PVmh6NkFJIn0.eyJleHAiOjE3MTI5MDA3NzEsImlhdCI6MTcxMjkwMDQ3MSwiYXV0aF90aW1lIjoxNzEyODk3MjI1LCJqdGkiOiIzNmQ1NDVlMS01MDVjLTQ1ZWItYWYzNi1hMjg5MzMyOTRmZDUiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwOTAvcmVhbG1zL3VlbmdpbmU2IiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjU1MzRmYmMwLWZmNWYtNDk3MS1hNzEwLWRhNjk5NWFkNGM2YiIsInR5cCI6IkJlYXJlciIsImF6cCI6InVlbmdpbmUiLCJub25jZSI6IjUzZTJkMTc2LWY0MGEtNDM2YS1hYjdhLWQ5NWU0OTBjYWY0ZSIsInNlc3Npb25fc3RhdGUiOiJmOWNlMjhjMy05YjY4LTQyYWItOTQ5Zi1jZmYxNTNlMGJmZGEiLCJhY3IiOiIwIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm1hbmFnZXIiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy11ZW5naW5lNiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUiLCJzaWQiOiJmOWNlMjhjMy05YjY4LTQyYWItOTQ5Zi1jZmYxNTNlMGJmZGEiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6InRlc3RlciIsImVtYWlsIjoiaW5pdGlhdG9yQHVlbmdpbmUub3JnIn0.BAkPxT1EmuSshUlvzkD6jTUKWUb8HgSbX6iqdEok1ou478g1IBA0UEqa3pdcYeVMnYS0153P4XgG1gnaQRVCNSqHqjxFXlQrWNDU2V5Y71rJmn8AkvTCHojJbo0cM5kNTc3HwMaJPE1C3Ira8kBUPqJTtspUh7RWI1WADmV59JYdlI_eaAlnFJdvrEAxErK3vgWPoSO2gJSYCL0_nH1TGTcTRENm8wOSvQjCu98aJLey_pQhdTJ1bM8jUlOVhJlXBKcz-zmPJmq2cKa0qA4s8Y8r8DfrdMgQsdUU339E2GZ9fhCmvEhRQ0023D3GHZgiibRIEKZO_35a1iRaaQDIzg'






# WorkItem Test Script

 $KEY="eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJJUmdaSjhxSW9EV3RNcDMteGJhUVIwcFVhYVUxZFhlNThYNW1PVmh6NkFJIn0.eyJleHAiOjE3MTI5MDA3NzEsImlhdCI6MTcxMjkwMDQ3MSwiYXV0aF90aW1lIjoxNzEyODk3MjI1LCJqdGkiOiIzNmQ1NDVlMS01MDVjLTQ1ZWItYWYzNi1hMjg5MzMyOTRmZDUiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwOTAvcmVhbG1zL3VlbmdpbmU2IiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjU1MzRmYmMwLWZmNWYtNDk3MS1hNzEwLWRhNjk5NWFkNGM2YiIsInR5cCI6IkJlYXJlciIsImF6cCI6InVlbmdpbmUiLCJub25jZSI6IjUzZTJkMTc2LWY0MGEtNDM2YS1hYjdhLWQ5NWU0OTBjYWY0ZSIsInNlc3Npb25fc3RhdGUiOiJmOWNlMjhjMy05YjY4LTQyYWItOTQ5Zi1jZmYxNTNlMGJmZGEiLCJhY3IiOiIwIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm1hbmFnZXIiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy11ZW5naW5lNiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUiLCJzaWQiOiJmOWNlMjhjMy05YjY4LTQyYWItOTQ5Zi1jZmYxNTNlMGJmZGEiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6InRlc3RlciIsImVtYWlsIjoiaW5pdGlhdG9yQHVlbmdpbmUub3JnIn0.BAkPxT1EmuSshUlvzkD6jTUKWUb8HgSbX6iqdEok1ou478g1IBA0UEqa3pdcYeVMnYS0153P4XgG1gnaQRVCNSqHqjxFXlQrWNDU2V5Y71rJmn8AkvTCHojJbo0cM5kNTc3HwMaJPE1C3Ira8kBUPqJTtspUh7RWI1WADmV59JYdlI_eaAlnFJdvrEAxErK3vgWPoSO2gJSYCL0_nH1TGTcTRENm8wOSvQjCu98aJLey_pQhdTJ1bM8jUlOVhJlXBKcz-zmPJmq2cKa0qA4s8Y8r8DfrdMgQsdUU339E2GZ9fhCmvEhRQ0023D3GHZgiibRIEKZO_35a1iRaaQDIzg"

http POST :9094/instance processDefinitionId="sales/TroubleAlertTest" "roleMappings[0][name]=alert" "roleMappings[0][endpoints][0]=manager"  "roleMappings[1][name]=manager" "roleMappings[1][endpoints][0]=manager" 'Authorization:Bearer $KEY'

http POST :9094/work-item/2/complete desiredState="complete" 'Authorization:Bearer $KEY'





# MultiVariable Test Script

 $KEY="eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJJUmdaSjhxSW9EV3RNcDMteGJhUVIwcFVhYVUxZFhlNThYNW1PVmh6NkFJIn0.eyJleHAiOjE3MTI5MDA3NzEsImlhdCI6MTcxMjkwMDQ3MSwiYXV0aF90aW1lIjoxNzEyODk3MjI1LCJqdGkiOiIzNmQ1NDVlMS01MDVjLTQ1ZWItYWYzNi1hMjg5MzMyOTRmZDUiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjkwOTAvcmVhbG1zL3VlbmdpbmU2IiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjU1MzRmYmMwLWZmNWYtNDk3MS1hNzEwLWRhNjk5NWFkNGM2YiIsInR5cCI6IkJlYXJlciIsImF6cCI6InVlbmdpbmUiLCJub25jZSI6IjUzZTJkMTc2LWY0MGEtNDM2YS1hYjdhLWQ5NWU0OTBjYWY0ZSIsInNlc3Npb25fc3RhdGUiOiJmOWNlMjhjMy05YjY4LTQyYWItOTQ5Zi1jZmYxNTNlMGJmZGEiLCJhY3IiOiIwIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm1hbmFnZXIiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy11ZW5naW5lNiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUiLCJzaWQiOiJmOWNlMjhjMy05YjY4LTQyYWItOTQ5Zi1jZmYxNTNlMGJmZGEiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6InRlc3RlciIsImVtYWlsIjoiaW5pdGlhdG9yQHVlbmdpbmUub3JnIn0.BAkPxT1EmuSshUlvzkD6jTUKWUb8HgSbX6iqdEok1ou478g1IBA0UEqa3pdcYeVMnYS0153P4XgG1gnaQRVCNSqHqjxFXlQrWNDU2V5Y71rJmn8AkvTCHojJbo0cM5kNTc3HwMaJPE1C3Ira8kBUPqJTtspUh7RWI1WADmV59JYdlI_eaAlnFJdvrEAxErK3vgWPoSO2gJSYCL0_nH1TGTcTRENm8wOSvQjCu98aJLey_pQhdTJ1bM8jUlOVhJlXBKcz-zmPJmq2cKa0qA4s8Y8r8DfrdMgQsdUU339E2GZ9fhCmvEhRQ0023D3GHZgiibRIEKZO_35a1iRaaQDIzg"

http POST :9094/instance processDefinitionId="test/test" "roleMappings[0][name]=reporter" "roleMappings[0][endpoints][0]=manager"  "roleMappings[1][name]=manager" "roleMappings[1][endpoints][0]=manager" "roleMappings[2][name]=worker" "roleMappings[2][endpoints][0]=manager" 'Authorization:Bearer $KEY'




 $formJson='{"formDefId":"troubleReport","filePath":"troubleReport.form","valueMap":{"_type":"java.util.HashMap","TroubleReport":[{"_type":"java.util.HashMap","TroubleName":"test1","TroubleContent":"test2","troubleReporter":"test3","TroubleType":"고장","files":"data:text/plain;base64,"},{"_type":"java.util.HashMap","TroubleName":"test4","TroubleContent":"test5","troubleReporter":"test6","TroubleType":"민원","files":"data:text/plain;base64,"}]},"_type":"org.uengine.contexts.HtmlFormContext"}'

 $formConfig="Content-Type:application/json"

http POST :9094/instance/1/variable/TroubleReportForm json="$formJson" $formConfig 'Authorization:Bearer $KEY'


# uEngine을 Docker에 올려서 실행시키기

Docker에 올릴 때는 큰 변경사항이 생기거나 동작을 불가능하게 하는 치명적인 버그가 생겼을 때 사용합니다.

## Docker Hub에 이미지를 올리는 과정

1. **uEngine 루트 디렉토리에서 mvn build를 진행합니다.**

    ```sh
    mvn install -DskipTests
    ```

    그러면 각 `definition-service`, `process-service`에 `SNAPSHOT.jar` 파일이 생성됩니다.

    ![alt text](mdmanual/definition-service.png)

    ![alt text](mdmanual/process-service.png)

2. **각 서비스에서 다음을 진행합니다.**

    ```sh
    # definition-service
    docker build -t definition-service:latest .
    docker tag definition-service:latest {dockerID}/definition-service:1.0.0
    docker push {dockerID}/definition-service:1.0.0
    ```

    ```sh
    # process-service
    docker build -t process-service:latest .
    docker tag process-service:latest {dockerID}/process-service:1.0.0
    docker push {dockerID}/process-service:1.0.0
    ```

    > `{dockerID}`는 자신의 Docker 계정명입니다.

3. **이렇게 올려진 이미지를 `docker-compose` 파일에 적용합니다.**

    ```yaml
    definition-service:
      image: {dockerID}/definition-service:1.0.0

    process-service:
      image: {dockerID}/process-service:1.0.0
    ```
4. 그 후 실행은 루트 디렉토리/infra에서 다음 명령어로 Docker Compose를 사용하여 쉽게 시작하거나 종료시킬 수 있습니다:

    ```sh
    # Docker Compose UP
        docker-compose up

    ```

![alt text](mdmanual/docker.png)


