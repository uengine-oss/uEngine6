#!/usr/bin/env bash
# release 폴더에서 실행: 같은 폴더의 tar 파일을 docker load
# 사용: ./load-images.sh  (또는 release 폴더에 이 파일 복사 후 실행)

set -e
cd "$(dirname "$0")"

echo "Loading tar images from current directory..."
for f in keycloak-bundle-1.1.0.tar uengine-release-1.1.0.tar process-gpt-vue3-1.4.0.tar; do
  if [ -f "$f" ]; then
    echo "  load: $f"
    docker load -i "$f"
  else
    echo "  skip (not found): $f"
  fi
done
echo "Done. Run: docker compose up -d"
