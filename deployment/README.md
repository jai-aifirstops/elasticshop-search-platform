# ElasticShop Production Deployment

The local `docker-compose.yml` remains unchanged for development.

Production uses:

- `docker-compose.prod.yml`
- `deployment/.env.production`
- `deployment/Caddyfile`

Only Caddy publishes host ports.

Public ports:

- 80
- 443

Internal-only services:

- React/Nginx
- Spring Boot
- PostgreSQL
- Redis
- Kafka
- Elasticsearch
- Prometheus
- Grafana

## Server recommendation

For the complete portfolio stack, start with approximately:

- Ubuntu 24.04 LTS
- 4 vCPU
- 8 GB RAM
- 80+ GB SSD

## DNS

Create an A record for the chosen application hostname and point it to the
server's public IPv4 address.

Example:

`search.example.com -> SERVER_IP`

Caddy will request and renew HTTPS certificates automatically once DNS
points to the server and TCP ports 80/443 are reachable.

## Server environment

On the server:

```bash
cp deployment/.env.production.example deployment/.env.production
```

Replace every placeholder with production values.

Never commit `deployment/.env.production`.

## Start

```bash
docker compose \
  --env-file deployment/.env.production \
  -f docker-compose.prod.yml \
  up -d --build
```

## Check

```bash
docker compose \
  --env-file deployment/.env.production \
  -f docker-compose.prod.yml \
  ps
```

## Stop without deleting data

```bash
docker compose \
  --env-file deployment/.env.production \
  -f docker-compose.prod.yml \
  down
```

Do not add `-v` unless persistent data is intentionally being deleted.

## Firewall

Allow only the ports required from the Internet:

- 22/tcp for SSH
- 80/tcp for HTTP/ACME
- 443/tcp for HTTPS
- 443/udp for HTTP/3 (optional)

Do not publicly expose PostgreSQL, Redis, Kafka, Elasticsearch,
Prometheus, Grafana, or the Spring Boot port.

## Grafana

Grafana is intentionally internal-only in the production Compose file.

For administrative access, use an SSH tunnel rather than exposing port
3000 publicly.