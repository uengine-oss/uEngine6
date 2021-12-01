# uEngine5-bpm

## Running on Kubernetes

```
mvn clean package -B -Dmaven.test.skip=true

cd definition-service
mvn clean package -B
docker build -t gcr.io/my-project-1531888882785/uengine-definition:v5 .
docker push gcr.io/my-project-1531888882785/uengine-definition:v5

kubectl create -f Deployment.yaml
(Note:  you must change the image name as your one inside the yaml file)
(kubectl run --image="gcr.io/my-project-1531888882785/uengine-definition:v1")

kubectl create -f Service.yaml
(kubectl expose deploy uengine-defintion --port="9093" --type="LoadBalancer")
(kubectl get svc -w)

cd ..

cd process-service
mvn clean package -B -Dmaven.test.skip=true
docker build -t gcr.io/my-project-1531888882785/uengine-process:v11 .
docker push gcr.io/my-project-1531888882785/uengine-process:v11


kubectl set image deploy/uengine-process uengine-process=gcr.io/my-project-1531888882785/uengine-process:v8
cd ..

cd proxy
mvn clean package -B
docker build -t gcr.io/my-project-1531888882785/uengine-proxy:v2 .
docker push gcr.io/my-project-1531888882785/uengine-proxy:v2

kubectl create -f Deployment.yaml
(Note:  you must change the image name as your one inside the yaml file)
(kubectl run --image="gcr.io/my-project-1531888882785/uengine-definition:v1")

kubectl create -f Service.yaml
(kubectl expose deploy uengine-defintion --port="9093" --type="LoadBalancer")
(kubectl get svc -w)

cd ..
```



Run kafka:
```
$ helm repo add incubator http://storage.googleapis.com/kubernetes-charts-incubator
$ helm install --name kafka incubator/kafka


You can connect to Kafka by running a simple pod in the K8s cluster like this with a configuration like this:

  apiVersion: v1
  kind: Pod
  metadata:
    name: testclient
    namespace: default
  spec:
    containers:
    - name: kafka
      image: confluentinc/cp-kafka:5.0.0-2
      command:
        - sh
        - -c
        - "exec tail -f /dev/null"

Once you have the testclient pod above running, you can list all kafka
topics with:

  kubectl -n default exec testclient -- /usr/bin/kafka-topics --zookeeper my-kafka-zookeeper:2181 --list

To create a new topic:

  kubectl -n default exec testclient -- /usr/bin/kafka-topics --zookeeper my-kafka-zookeeper:2181 --topic test1 --create --partitions 1 --replication-factor 1

To listen for messages on a topic:

  kubectl -n default exec -ti testclient -- /usr/bin/kafka-console-consumer --bootstrap-server my-kafka:9092 --topic test1 --from-beginning

To stop the listener session above press: Ctrl+C

To start an interactive message producer session:
  kubectl -n default exec -ti testclient -- /usr/bin/kafka-console-producer --broker-list my-kafka-headless:9092 --topic test1

To create a message in the above session, simply type the message and press "enter"
```



## Local development environment

Edit your hosts file to mimic the Kubernetes DNS service
```
127.0.0.1 bpm.uengine.io
127.0.0.1 iam.uengine.io
127.0.0.1 definition-service
127.0.0.1 process-service
127.0.0.1 back-office
127.0.0.1 workspace
127.0.0.1 login-handler
127.0.0.1 my-kafka.kafka.svc.cluster.local
127.0.0.1 my-kafka-zookeeper.kafka.svc.cluster.local
```

Download and Run kafka firstly:

- download:  https://www.apache.org/dyn/closer.cgi?path=/kafka/2.1.0/kafka_2.12-2.1.0.tgz

- run:

```
(new shell)
cd ~/Downloads/kafka_2.12-2.1.0
bin/zookeeper-server-start.sh config/zookeeper.properties

(new shell)
cd ~/Downloads/kafka_2.12-2.1.0
bin/kafka-server-start.sh config/server.properties
```

Run KeyCloak for IAM Services:
```
cd keycloak-15.0.2/bin
./standalone.sh
```

Run each service with mvn:

[Note] Before running the services, make sure that you use JDK1.8 rather than higher.
```
export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
java -version  # must tells us it is 1.8

or

sudo apt-get update
sudo apt-get install openjdk-8-jdk
sudo update-java-alternatives --jre-headless --jre --set java-1.8.0-openjdk-amd64

java -version  # must tells us it is 1.8
```


```
(new shell)
cd definition-service
mvn spring-boot:run -Dserver.port=9093

(new shell)
cd process-service
mvn spring-boot:run -Dserver.port=9094

(new shell)
cd gateway
mvn spring-boot:run -Dspring-boot.run.profiles=dev

(new shell)
cd login-handler
mvn spring-boot:run -Dspring-boot.run.profiles=dev

(new shell)
cd back-office
npm install
npm run dev

(new shell)
cd workspace
npm install
npm run serve
```

Applying uEngine Core Libraries
```
git clone https://github.com/uengine-oss/uengine-bpm
cd uengine-core
mvn install -Dmaven.test.skip=true
```



# Deploy with Helm

Test the helm chart:

```
cd helm-chart/prod
helm install --dry-run --debug uengine --namespace uengine .
```

Real deploy:
```
cd helm-chart/prod
helm dependency update
helm init
helm install uengine --namespace uengine .
```


# Deploy Example Process Definitions
```
cd definitions
http POST bpm.uengine.io/definition/raw/keyboard.json < keyboard.json
http POST bpm.uegnine.io/definition/raw/class-design.json < class-design.json
```
* bpm.uengine.io is where your backend is redirected. Set your etc/hosts file for this.
