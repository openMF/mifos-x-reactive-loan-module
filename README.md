# mifos-x-reactive-loan-module
This repo contains the reactive loan risk assessment module for Mifos X. This repo is currently work in progress.

branch main is for release/stable versions

branch dev is the development branch all PRs should be raised for this branch

dev--> main will be undertaken in a release.

# Setup

- Avro schemas available in local Maven repository
  *(make sure to run `./gradlew publishToMavenLocal` in fineract-avro-schemas of Fineract repo for avro schemas)*

## Start PostgreSQL via Docker

```bash
docker compose up -d loanrisk_db
```

## Start Mifos X Reactive Loan Risk Assessment Module

```bash
./gradlew bootRun --args='--spring.profiles.active=kafka'
```
