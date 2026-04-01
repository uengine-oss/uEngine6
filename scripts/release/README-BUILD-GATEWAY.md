# keycloak-gateway 이미지에서 supabase가 나올 때

리다이렉트 URL에 `client_id=supabase`, `.../code/supabase`가 나오면 **이미지가 예전 빌드**이거나 **Docker 빌드 캐시** 때문일 수 있습니다.

## 1. 캐시 없이 다시 빌드

프로젝트 루트에서:

```powershell
# keycloak-gateway만 클린 빌드
mvn clean package -DskipTests -f keycloak-gateway/pom.xml

# 이미지 캐시 없이 빌드 (중요)
docker build --no-cache -t keycloak-gateway:1.1.0 -f keycloak-gateway/Dockerfile keycloak-gateway
```

또는 기존 스크립트 사용 후 gateway만 재빌드:

```powershell
.\scripts\build-release-images.ps1 1.1.0 -SaveTar
# 그 다음 gateway만 캐시 없이
docker build --no-cache -t keycloak-gateway:1.1.0 -f keycloak-gateway/Dockerfile keycloak-gateway
```

## 2. 이미지 안 설정 확인

실제로 uengine/keycloak 설정이 들어갔는지 확인:

```powershell
docker run --rm keycloak-gateway:1.1.0 sh -c "unzip -p /app.jar BOOT-INF/classes/application.yml" | findstr -i "client-id redirect-uri"
```

기대 결과: `client-id: uengine`, `redirect-uri: .../code/keycloak` (supabase 없음)

## 3. 컨테이너 재기동

이미지 재빌드 후 반드시 컨테이너를 다시 띄워야 합니다.

```powershell
cd scripts\release
docker compose down
docker compose up -d
```

## 4. 브라우저

브라우저 캐시는 리다이렉트 URL 생성과 무관합니다(서버가 새 URL을 만들어서 302로 보냄). 시크릿 창으로 한 번 시도해 보면 됩니다.
