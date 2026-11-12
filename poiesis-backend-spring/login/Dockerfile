FROM ghcr.io/graalvm/native-image-community:21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY domain/pom.xml ./domain/pom.xml
COPY spring/pom.xml ./spring/pom.xml
COPY domain/src ./domain/src
COPY spring/src ./spring/src

RUN mvn -q -pl spring -am native:compile -DskipTests

FROM debian:bookworm-slim
WORKDIR /app

COPY --from=build /workspace/spring/target/login /app/app

EXPOSE 8081

ENTRYPOINT ["/app/app"]
