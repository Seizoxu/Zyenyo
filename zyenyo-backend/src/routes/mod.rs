mod prompts;
mod botstats;

use actix_web::web;

pub fn api_config(cfg: &mut web::ServiceConfig) {
    cfg
        .service(prompts::prompts)
        .service(prompts::prompt)
        .service(botstats::botstats);
}
