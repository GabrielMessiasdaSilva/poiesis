
# Etapa 1: Build nativo com GraalVM
FROM ghcr.io/graalvm/native-image-community:21 AS build

# Garante permissão para instalar pacotes
USER root

WORKDIR /workspace

# Instala somente o Maven e os certificados
RUN microdnf install -y maven ca-certificates \
    && microdnf clean all

# Copia os arquivos do projeto
COPY pom.xml ./
COPY domain/pom.xml ./domain/pom.xml
COPY spring/pom.xml ./spring/pom.xml

COPY domain/src ./domain/src
COPY spring/src ./spring/src

# Compila a aplicação como executável nativo
RUN mvn -B -pl spring -am native:compile -DskipTests

# Etapa 2: Imagem final
FROM debian:bookworm-slim

WORKDIR /app

COPY --from=build /workspace/spring/target/login-spring /app/app

EXPOSE 8081

ENTRYPOINT ["/app/app"]
