# Zyenyo

This repository includes:
* `zyenyo-discord`: Zyenyo, A discord bot that serves and collects typing tests & statistics
* `zyenyo-backend`: A backend web server written in actix, Rust.
* `zyenyo-frontend`: A frontend NextJS web application.

## Running
To run all services together, use:
```bash
docker compose up --build
```

## Deleting
```bash
docker system prune -a
```

## Environment Variables
All environment variables are stored in `.env` at the root of the project
