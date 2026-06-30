FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace
COPY . .

RUN chmod +x gradlew
RUN ./gradlew installDist --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/build/install/reuam-app/ ./

ENV PORT=8080

CMD ["sh", "-c", "/app/bin/reuam-app -host=0.0.0.0 -port=${PORT}"]
