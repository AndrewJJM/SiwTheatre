# SiwTheatre

[![CI](https://github.com/AndrewJJM/SiwTheatre/actions/workflows/ci.yml/badge.svg)](https://github.com/AndrewJJM/SiwTheatre/actions/workflows/ci.yml)

Applicazione web per la gestione delle prenotazioni di una compagnia teatrale:
catalogo degli spettacoli con artisti e immagini, prenotazione biglietti con
controllo della disponibilità, area amministratore per la gestione di
spettacoli, artisti e prenotazioni.

## Stack tecnologico

- **Java 17**, **Spring Boot 3** (Web MVC, Data JPA, Validation)
- **Spring Security** con form login e OAuth2 (Google, Facebook, GitHub)
- **Thymeleaf** + Bootstrap 5 per il frontend server-side
- **PostgreSQL**
- **MapStruct** per il mapping entity ↔ DTO
- **Testcontainers** per i test di integrazione su PostgreSQL reale
- **Docker / Docker Compose** e **GitHub Actions** per build e CI

## Architettura

Applicazione MVC a livelli, `Controller → Service → Repository`:

- **DTO layer**: i controller espongono alle viste solo DTO
  (`BookingDTO`, `PlayDTO`) mappati con MapStruct; i form bindano oggetti
  dedicati (`CreateBookingRequest`), mai entity JPA.
- **Confine transazionale nel service**: la creazione di una prenotazione e
  il decremento dei biglietti disponibili avvengono in un'unica transazione
  (`BookingService`).
- **Optimistic locking**: `Play` ha un campo `@Version`; se due utenti
  prenotano gli ultimi biglietti nello stesso momento, una sola transazione
  va a buon fine — niente overselling. Lo scenario è coperto da un test di
  concorrenza (`BookingServiceIntegrationTest`).
- **Gestione centralizzata degli errori**: `GlobalExceptionHandler`
  (`@ControllerAdvice`) con eccezioni di dominio e pagine 404/409/500
  dedicate.

## Come avviare il progetto (Docker)

Prerequisito: Docker.

```bash
cp .env.example .env      # opzionale: inserisci le credenziali OAuth reali
docker compose up --build
```

L'applicazione è su <http://localhost:8080>. Al primo avvio il database viene
creato automaticamente e popolato con uno spettacolo di esempio.

> Senza credenziali OAuth reali nel `.env` il login social non funziona, ma
> registrazione e login con username/password sono pienamente operativi.

## Sviluppo locale (senza Docker)

Servono Java 17 e un PostgreSQL su `localhost:5432` con database
`siwTheatre` (utente/password `postgres`, personalizzabili via variabili
d'ambiente `SPRING_DATASOURCE_*`):

```bash
./mvnw spring-boot:run
```

Se riutilizzi un database creato prima dell'introduzione dell'optimistic
locking, esegui una tantum:

```sql
UPDATE play SET version = 0 WHERE version IS NULL;
```

## Test

```bash
./mvnw verify
```

I test di integrazione usano Testcontainers e richiedono Docker in
esecuzione: avviano un PostgreSQL "usa e getta", inclusa la verifica che due
prenotazioni concorrenti sugli stessi biglietti non causino overselling.

## Gestione dei secret

Nessun secret è versionato: `application.properties` legge le credenziali da
variabili d'ambiente (`GOOGLE_CLIENT_SECRET`, ecc.) con default innocui, e il
file `.env` è nel `.gitignore`. Il template `.env.example` documenta le
variabili disponibili.

> **Nota**: i client secret OAuth presenti nella history git precedente a
> questa configurazione vanno considerati compromessi: vanno revocati e
> rigenerati dalle rispettive console developer (Google Cloud, Meta, GitHub).
