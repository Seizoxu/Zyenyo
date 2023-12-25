# syntax=docker/dockerfile:1.4
FROM rust:buster AS base

WORKDIR /code
RUN cargo init
COPY Cargo.toml /code/Cargo.toml
RUN cargo fetch
COPY . /code

FROM base AS development

EXPOSE 8000

CMD [ "cargo", "run" ]

FROM base AS builder

RUN cargo build --release

FROM debian:buster-slim AS production

ARG MONGO_URI
ENV ZYENYO_ENVIRONMENT=development
ENV MONGO_URI=$MONGO_URI

EXPOSE 8000

COPY --from=builder /code/target/release/zyenyo-backend /zyenyo-backend

CMD [ "/zyenyo-backend" ]
