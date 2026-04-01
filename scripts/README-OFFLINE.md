# uEngine 오프라인 Docker 이미지 배포

인터넷이 없는 환경에서 Docker로 uEngine을 실행하기 위한 스크립트입니다.

---

## 릴리즈 버전 이미지 빌드 + tar 내보내기

특정 버전(예: 1.1.0)으로 이미지를 빌드한 뒤 tar로 저장해 오프라인으로 가져갈 때 사용합니다.

| 파일 | 포함 이미지 |
|------|-------------|
| `uengine-release-{버전}.tar` | definition-service, keycloak-gateway |
| `keycloak-bundle-{버전}.tar` | keycloak + Zookeeper + Kafka (오프라인용 일괄) |

**Oracle:** tar에는 넣지 않습니다. DB는 설치 측(고객 사이트)에 이미 있으므로, 컨테이너 실행 시 **환경 변수**로 접속 정보만 넘기면 됩니다 (아래 "설치 측 환경 변수" 참고).

### 한 번에: 빌드 + tar 저장 (한 줄)

**결과물:** `uengine-release-{버전}.tar`(앱), `keycloak-bundle-{버전}.tar`(Keycloak + Zookeeper + Kafka)

**Windows PowerShell (-SaveTar 사용 시 위와 동일한 파일명으로 저장):**
```powershell
.\scripts\build-release-images.ps1 1.1.0 -SaveTar
```
→ `offline-images\uengine-release-1.1.0.tar`, `offline-images\keycloak-bundle-1.1.0.tar`(Keycloak+Zookeeper+Kafka) 생성. release 폴더로 복사 후 `load-images.ps1` 사용 가능. `-SaveTar` 시 Zookeeper/Kafka는 자동 pull 후 keycloak-bundle에 포함됨.

**Linux / macOS / Git Bash:**
```bash
V=1.1.0 && ./scripts/build-release-images.sh $V && mkdir -p offline-images && \
  docker save -o offline-images/uengine-release-${V}.tar definition-service:${V} keycloak-gateway:${V} && \
  docker save -o offline-images/keycloak-bundle-${V}.tar keycloak:${V}
```

**Windows (빌드만 하고 tar는 수동 저장):**
```powershell
$V = "1.1.0"; .\scripts\build-release-images.ps1 $V -SaveTar
# (-SaveTar 시 keycloak-bundle에 Keycloak+uengine-zookeeper+uengine-kafka 자동 포함)
```

위에서 `V=1.1.0` / `$V = "1.1.0"`만 원하는 버전으로 바꿔서 실행하면 됩니다. Windows는 `-SaveTar` 사용을 권장합니다.

### Docker 설치가 불가능한 Windows 1809 PC -> Linux에서 최종 tar 생성

대상 PC에 Docker를 설치할 수 없으면, Windows에서는 Docker 빌드 직전 상태를 **압축파일 1개**로 만들고 Linux에서 그 압축파일로 이미지와 tar를 생성합니다.

**Windows / Git Bash:**
```bash
./scripts/package-release-build-context.sh 1.1.0
```

결과물:

- `offline-images/uengine-release-build-context-1.1.0.tgz`

이 파일을 Linux로 복사한 뒤, 아래 스크립트를 압축파일과 **같은 위치**에 두고 실행합니다.

**Linux:**
```bash
./rebuild-release-images-from-package.sh 1.1.0
```

결과물:

- `uengine-release-1.1.0.tar`
- `keycloak-bundle-1.1.0.tar`

이 흐름은 기존 `./scripts/build-release-images.sh 1.1.0` 실행 후 아래 명령으로 저장한 결과와 같은 태그/파일명을 만듭니다.

```bash
mkdir -p offline-images
docker save -o offline-images/uengine-release-1.1.0.tar definition-service:1.1.0 keycloak-gateway:1.1.0
docker save -o offline-images/keycloak-bundle-1.1.0.tar keycloak:1.1.0
```

### 단계별: 1) 이미지 빌드

**Linux / macOS / Git Bash:**
```bash
./scripts/build-release-images.sh 1.1.0
```

**Windows PowerShell:**
```powershell
.\scripts\build-release-images.ps1 1.1.0
```

생성되는 이미지: `definition-service:1.1.0`, `keycloak-gateway:1.1.0`, `keycloak:1.1.0` (tar에는 Oracle 제외)

### 단계별: 2) tar로 저장

앱용 tar와 keycloak용 tar를 **따로** 저장합니다. Oracle 이미지는 넣지 않습니다.

```bash
mkdir -p offline-images
V=1.1.0

# 앱 (definition-service, keycloak-gateway)
docker save -o offline-images/uengine-release-${V}.tar \
  definition-service:${V} \
  keycloak-gateway:${V}

# keycloak + Zookeeper + Kafka (오프라인용, compose와 동일한 이미지명)
docker pull confluentinc/cp-zookeeper:7.5.0 confluentinc/cp-kafka:7.5.0
docker tag confluentinc/cp-zookeeper:7.5.0 uengine-zookeeper:${V}
docker tag confluentinc/cp-kafka:7.5.0 uengine-kafka:${V}
docker save -o offline-images/keycloak-bundle-${V}.tar keycloak:${V} uengine-zookeeper:${V} uengine-kafka:${V}
```

Windows PowerShell:
```powershell
$V = "1.1.0"
New-Item -ItemType Directory -Force -Path offline-images
docker save -o "offline-images/uengine-release-$V.tar" "definition-service:$V", "keycloak-gateway:$V"
docker pull confluentinc/cp-zookeeper:7.5.0 confluentinc/cp-kafka:7.5.0
docker tag confluentinc/cp-zookeeper:7.5.0 uengine-zookeeper:$V
docker tag confluentinc/cp-kafka:7.5.0 uengine-kafka:$V
docker save -o "offline-images/keycloak-bundle-$V.tar" "keycloak:$V" "uengine-zookeeper:$V" "uengine-kafka:$V"
```

### 단계별: 3) 오프라인 환경에서 tar 로드

```bash
docker load -i offline-images/uengine-release-1.1.0.tar
docker load -i offline-images/keycloak-bundle-1.1.0.tar
```

이후 `docker-compose` 등에서 이미지 태그(`definition-service:1.1.0`, `keycloak:1.1.0` 등)를 해당 버전으로 맞춰 사용하면 됩니다.

#### 설치 측 환경 변수 (Oracle 연동)

Oracle은 고객 측에 설치되어 있다고 가정합니다. definition-service(및 process-service) 컨테이너를 띄울 때 아래 환경 변수로 접속 정보를 넘기면 됩니다.

| 변수 | 설명 | 예시 |
|------|------|------|
| `SPRING_PROFILES_ACTIVE` | oracle 프로필 사용 | `oracle` |
| `ORACLE_HOST` | DB 호스트 | `db-server` 또는 `192.168.1.10` |
| `ORACLE_PORT` | 리스너 포트 | `1521` |
| `ORACLE_USER` | DB 사용자 | `uengine` 또는 `system` |
| `ORACLE_PASSWORD` | 비밀번호 | (실제 비밀번호) |

예 (docker run):
```bash
docker run -e SPRING_PROFILES_ACTIVE=oracle -e ORACLE_HOST=db-server -e ORACLE_PORT=1521 -e ORACLE_USER=uengine -e ORACLE_PASSWORD=xxx -p 8080:8080 definition-service:1.1.0
```

docker-compose에서는 서비스에 `environment:` 블록으로 위 변수들을 넣으면 됩니다.

---

## 이미지 구성

| 파일 | 포함 이미지 |
|------|-------------|
| `uengine-app-bundle.tar` | process-service, definition-service, keycloak-gateway, kafka, zookeeper |
| `keycloak-bundle.tar` | keycloak |

## 사용 방법

### 1단계: 인터넷 있는 환경에서 (한 번만)

```bash
./scripts/build-offline-images.sh
```

- Maven 빌드 + Docker 이미지 빌드/풀 + tar 저장
- 출력: `offline-images/uengine-app-bundle.tar`, `offline-images/keycloak-bundle.tar`

### 2단계: 오프라인 환경으로 복사

다음 파일/폴더를 USB 등으로 복사:

```
offline-images/
  uengine-app-bundle.tar
  keycloak-bundle.tar
scripts/
  load-offline-images.sh
infra/
  docker-compose.offline.yml
  keycloak/
    realm-export.json
```

### 3단계: 오프라인 환경에서

```bash
# 이미지 로드
./scripts/load-offline-images.sh

# 서비스 실행 (infra 디렉터리에서)
cd infra && docker compose -f docker-compose.offline.yml up -d
```

## 환경 변수

- `OUTPUT_DIR`: tar 저장 경로 (기본: `프로젝트루트/offline-images`)
- `IMAGES_DIR`: tar 로드 경로 (기본: `프로젝트루트/offline-images`)
