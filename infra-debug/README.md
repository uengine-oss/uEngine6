# infra-debug

Separated debug Docker stack for uEngine6.

Ports:
- Keycloak: http://localhost:18080
- Gateway: http://localhost:18088
- Definition service: http://localhost:19093
- Process service: http://localhost:19094
- Kafka: localhost:19092
- Zookeeper: localhost:12281

Run:

```powershell
cd D:\uEngineProject\uEngine6\infra-debug
docker compose up -d --build
```

The gateway serves the local frontend dev server at `http://host.docker.internal:5175`.