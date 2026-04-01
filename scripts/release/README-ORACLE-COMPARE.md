# oracle/ Docker vs release 배포 비교

**기준**: `oracle/` 쪽 Docker 설정은 **게이트웨이만 따로 실행**하면 정상 동작한다고 보고, release 구성을 그에 맞춰 맞춤.

---

## Oracle 쪽 (정상 동작 기준)

| 구분 | 내용 |
|------|------|
| **oracle/docker-compose.yml** | Oracle XE, Zookeeper, Kafka(서비스명 `kafka`), Keycloak |
| **Keycloak** | `quay.io/keycloak/keycloak:18.0.1`, realm: `../infra/keycloak/realm-export.json`, `start-dev --import-realm` |
| **Kafka** | `PLAINTEXT_INTERNAL://kafka:29092` → 컨테이너 간 연결은 `kafka:29092` |
| **definition-service** | IDE 등에서 **호스트에서** 실행, `SPRING_PROFILES_ACTIVE=oracle`, 포트 **9093**, Kafka `localhost:9092`, Oracle `localhost:1521` |
| **keycloak-gateway** | **따로 실행** (호스트), default 프로필, 라우트: definition `localhost:9093`, process `localhost:9094`, frontend `localhost:5173`, Keycloak `localhost:8080` |

---

## Release 쪽 (Oracle과 동일하게 동작하도록 맞춘 부분)

| 구분 | release 구성 |
|------|--------------|
| **Keycloak** | 커스텀 이미지 `keycloak:1.1.0` (동일 18.0.1 베이스 + realm 포함), 볼륨 `./keycloak/realm-export.json` 마운트, healthcheck 추가 |
| **Kafka** | **동일** Zookeeper + Kafka 추가, 서비스명 `kafka`, `kafka:29092` 사용 (oracle과 동일) |
| **definition-service** | **docker 프로필** 사용, 포트 **9093**, `KAFKA_BOOTSTRAP_SERVERS=kafka:29092` (oracle의 컨테이너 간 주소와 동일), Kafka healthy 후 기동 |
| **keycloak-gateway** | **docker 프로필** 사용, 라우트: `definition-service:9093`, `frontend:5173`, execution, instance (호스트 주소는 그대로). Keycloak은 `http://keycloak:8080` |

---

## 수정·정리한 내용 요약

1. **keycloak-gateway docker 프로필**  
   - 기존: execution 라우트만 있음 → `/definition/**` 등 404.  
   - 수정: Oracle 쪽 default 프로필과 동일하게 **definition-service**(`http://definition-service:9093`), **frontend**(`http://frontend:5173`), execution, instance 라우트 추가.

2. **definition-service Kafka**  
   - 기존: docker 프로필 `my-kafka:9092` (oracle 서비스명과 불일치), release compose에 Kafka 없음 → 기동 실패.  
   - 수정: `KAFKA_BOOTSTRAP_SERVERS` 기본값 `kafka:29092` (oracle과 동일), release 전체/분리 compose 모두 **Zookeeper + Kafka** 포함, definition-service는 **docker 프로필** + Kafka healthy 후 기동.

3. **definition-service 프로필**  
   - 기존: release compose에서 기본값 `default` → 컨테이너 안에서 `localhost:9092` 접속 시도로 Kafka 실패.  
   - 수정: 기본값 **docker** 로 통일, Kafka 주소는 `kafka:29092` 사용.

4. **docker-compose.uengine.yml**  
   - 기존: definition-service + gateway 만 있음 → Kafka 없어 definition-service 실패.  
   - 수정: Oracle 쪽과 동일하게 **zookeeper + kafka** 포함, definition-service는 docker 프로필 + `kafka:29092`.

---

## Realm / Keycloak

- **infra/keycloak/realm-export.json** ↔ **scripts/release/keycloak/realm-export.json** 내용 동일하게 유지 (client uengine, redirectUris, secret 등).
- release에서는 위 realm을 `./keycloak/realm-export.json` 으로 마운트해 사용.

이렇게 맞춰두면 **게이트웨이만 따로 두고** Oracle 쪽에서 하던 것처럼, release에서도 Keycloak + Kafka + definition-service + gateway가 동일한 방식으로 동작합니다.
