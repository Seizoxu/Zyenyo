mod prompts;
mod botstats;

use actix_web::web;

pub fn api_config(cfg: &mut web::ServiceConfig) {
    cfg
        .service(prompts::prompts)
        .service(botstats::botstats);
}
