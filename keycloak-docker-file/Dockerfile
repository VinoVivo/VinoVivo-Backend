FROM quay.io/keycloak/keycloak:24.0.2 AS builder

ARG KC_HEALTH_ENABLED KC_METRICS_ENABLED KC_FEATURES KC_DB KC_HTTP_ENABLED PROXY_ADDRESS_FORWARDING QUARKUS_TRANSACTION_MANAGER_ENABLE_RECOVERY KC_HOSTNAME KC_LOG_LEVEL KC_DB_POOL_MIN_SIZE

COPY /theme/keywind /opt/keycloak/themes/keywind

RUN /opt/keycloak/bin/kc.sh build

FROM fedora AS bins

RUN curl -fsSL https://github.com/caddyserver/caddy/releases/download/v2.7.4/caddy_2.7.4_linux_amd64.tar.gz | tar -zxvf - caddy
RUN curl -fsSL https://github.com/nicolas-van/multirun/releases/download/1.1.3/multirun-x86_64-linux-gnu-1.1.3.tar.gz | tar -zxvf - multirun

FROM quay.io/keycloak/keycloak:24.0.2

COPY --from=builder /opt/keycloak/ /opt/keycloak/

COPY --from=bins --chmod=0755 /multirun /usr/bin/multirun
COPY --from=bins --chmod=0755 /caddy /usr/bin/caddy

WORKDIR /app

COPY Caddyfile ./

COPY vino-vivo-realm.json /opt/keycloak/data/import/vino-vivo-realm.json

ENTRYPOINT ["multirun"]

CMD ["/opt/keycloak/bin/kc.sh start --optimized --import-realm", "caddy run 2>&1"]