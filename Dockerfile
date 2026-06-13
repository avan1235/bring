# syntax=docker/dockerfile:1.7
FROM gradle:9.3.1-jdk21-graal-jammy AS builder

RUN apt-get update && apt-get install -y --no-install-recommends xz-utils curl ca-certificates \
 && rm -rf /var/lib/apt/lists/*
RUN curl -sSL $(curl -s https://api.github.com/repos/upx/upx/releases/latest | \
    grep browser_download_url | \
    grep amd64 | \
    cut -d '"' -f 4) -o upx.tar.xz && \
    tar -xf upx.tar.xz && \
    cd upx-*-amd64_linux && \
    mv upx /bin/upx

WORKDIR /home/gradle/project

COPY .env ./
COPY settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY build-src ./build-src
COPY server/build.gradle.kts ./server/
COPY shared/build.gradle.kts ./shared/
COPY shared-client/build.gradle.kts ./shared-client/
COPY app/build.gradle.kts ./app/

RUN --mount=type=cache,target=/home/gradle/.gradle \
    --mount=type=cache,target=/root/.gradle \
    gradle --no-daemon server:help -q || true

COPY . .

ARG VERSION

RUN --mount=type=cache,target=/home/gradle/.gradle \
    --mount=type=cache,target=/root/.gradle \
    --mount=type=cache,target=/home/gradle/project/.gradle \
    --mount=type=cache,target=/home/gradle/project/server/build/native \
    gradle --no-daemon server:nativeCompile && \
    cp /home/gradle/project/server/build/native/nativeCompile/server /tmp/server-native

RUN /bin/upx --best --lzma /tmp/server-native

FROM gcr.io/distroless/base-nossl-debian12 as runner

ARG POSTGRES_PORT
ARG POSTGRES_DB
ARG POSTGRES_SSL_MODE
ARG POSTGRES_USER
ARG POSTGRES_PASSWORD
ARG POSTGRES_HOST
ARG HOST
ARG PORT
ARG CORS_PORT
ARG CORS_HOST
ARG CORS_SCHEME

ENV POSTGRES_PORT=${POSTGRES_PORT}
ENV POSTGRES_DB=${POSTGRES_DB}
ENV POSTGRES_SSL_MODE=${POSTGRES_SSL_MODE}
ENV POSTGRES_USER=${POSTGRES_USER}
ENV POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
ENV POSTGRES_HOST=${POSTGRES_HOST}
ENV HOST=${HOST}
ENV PORT=${PORT}
ENV CORS_PORT=${CORS_PORT}
ENV CORS_HOST=${CORS_HOST}
ENV CORS_SCHEME=${CORS_SCHEME}

EXPOSE ${PORT}

WORKDIR /
COPY --from=builder /tmp/server-native ./server

ENTRYPOINT ["/server"]
