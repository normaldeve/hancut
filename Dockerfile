FROM gradle:8.14.3-jdk17 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN addgroup --system appgroup && adduser --system --group appuser
USER appuser

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]