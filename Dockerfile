FROM node:20-alpine AS web-build
WORKDIR /app/client
COPY client/package*.json ./
RUN npm ci
COPY client ./
RUN npm run build

FROM maven:3.9.9-eclipse-temurin-17 AS server-build
WORKDIR /app
COPY server ./server
COPY --from=web-build /app/client/dist ./server/src/main/resources/static
RUN mvn -f server/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /data/iesst/uploads
COPY --from=server-build /app/server/target/iesst-demo-server-0.1.0.jar /app/iesst-app.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/iesst-app.jar"]
