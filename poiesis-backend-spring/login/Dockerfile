FROM ghcr.io/graalvm/graalvm-community:21 AS build

WORKDIR /workspace

# Instala o Maven e o Native Image no ambiente GraalVM
RUN microdnf install -y maven ca-certificates && \
    gu install native-image && \
    microdnf clean all

COPY pom.xml ./
COPY domain/pom.xml ./domain/pom.xml
COPY spring/pom.xml ./spring/pom.xml

COPY domain/src ./domain/src
COPY spring/src ./spring/src

# Compila os módulos Java
RUN mvn -B -pl spring -am package -DskipTests

# Gera o executável nativo GraalVM
RUN mvn -B -pl spring native:compile -DskipTests

FROM debian:bookworm-slim
WORKDIR /app

COPY --from=build /workspace/spring/target/login-spring /app/app

EXPOSE 8081

ENTRYPOINT ["/app/app"]
