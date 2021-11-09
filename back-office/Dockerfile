FROM webratio/nodejs-http-server
VOLUME /tmp
ADD dist /opt/www/admin/
ADD run.sh /opt/run.sh
ARG CI_PROJECT_NAME
ARG CI_COMMIT_SHA
ENV CI_PROJECT_NAME=$CI_PROJECT_NAME
ENV CI_COMMIT_SHA=$CI_COMMIT_SHA
ARG CACHEBUST=202111051114
EXPOSE 8082
ENTRYPOINT ["sh","/opt/run.sh"]


# FROM node:14

# # 앱 디렉터리 생성
# WORKDIR /usr/src/app

# # 앱 의존성 설치
# # 가능한 경우(npm@5+) package.json과 package-lock.json을 모두 복사하기 위해
# # 와일드카드를 사용
# COPY package*.json ./

# RUN npm install
# # 프로덕션을 위한 코드를 빌드하는 경우
# # RUN npm ci --only=production

# # 앱 소스 추가
# COPY . .

# EXPOSE 8080
# CMD [ "node", "server.js" ]