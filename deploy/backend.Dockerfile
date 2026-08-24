FROM maven:3.9.16-eclipse-temurin-25-noble AS builder

WORKDIR /workspace

COPY backend/travis-dependencies ./backend/travis-dependencies
RUN mvn -f backend/travis-dependencies/pom.xml install -DskipTests -Dspotless.apply.skip=true

COPY backend/travis-infrastructure ./backend/travis-infrastructure
RUN mvn -f backend/travis-infrastructure/pom.xml install -DskipTests -Dspotless.apply.skip=true

COPY backend/travis-monolith ./backend/travis-monolith
RUN mvn -f backend/travis-monolith/pom.xml package \
        -pl travis-server -am -DskipTests -Dspotless.apply.skip=true \
    && mvn -f backend/travis-monolith/pom.xml spring-boot:repackage \
        -pl travis-server -DskipTests -Dspotless.apply.skip=true \
    && jar tf backend/travis-monolith/travis-server/target/travis-server-*.jar \
        | grep -q '^BOOT-INF/' \
    && cp backend/travis-monolith/travis-server/target/travis-server-*.jar /workspace/app.jar

FROM eclipse-temurin:25.0.3_9-jre-noble

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 travis \
    && useradd --uid 10001 --gid 10001 --no-create-home --shell /usr/sbin/nologin travis \
    && mkdir -p /app /data/logs /home/travis/data/uploads \
    && chown -R travis:travis /app /data /home/travis

WORKDIR /app
COPY --from=builder --chown=travis:travis /workspace/app.jar /app/app.jar

USER 10001:10001
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/urandom", "-jar", "/app/app.jar"]
