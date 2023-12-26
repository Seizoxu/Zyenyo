# syntax=docker/dockerfile:1.4
FROM rust:buster AS base
WORKDIR /code

FROM base AS development
COPY . .
EXPOSE 8000
CMD [ "cargo", "run" ]

FROM base AS builder

COPY dummy.rs .
COPY Cargo.toml .
RUN sed -i 's#src/main.rs#dummy.rs#' Cargo.toml
RUN cargo build --release
RUN sed -i 's#dummy.rs#src/main.rs#' Cargo.toml
COPY src ./src
RUN cargo build --release

FROM debian:buster-slim AS production
EXPOSE 8000
COPY --from=builder /code/target/release/zyenyo-backend /zyenyo-backend
CMD [ "/zyenyo-backend" ]
