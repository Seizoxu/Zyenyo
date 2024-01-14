use std::error::Error;

use crate::{models::{Test, User, Prompt}, Context};
use actix_web::{get, web, HttpResponse, Responder};
use mongodb::{bson::doc, Collection};
use serde::Serialize;

#[derive(Serialize)]
struct BotStats {
    total_tests: u64,
    total_users: u64,
    total_prompts: u64,
}

#[get("/botstats")]
async fn botstats(context: web::Data<Context>) -> impl Responder {

    match botstats_query(context).await {
        Ok(stats) => HttpResponse::Ok().json(stats),
        Err(e) => HttpResponse::InternalServerError().body(e.to_string()),
    }

}

async fn botstats_query(context: web::Data<Context>) -> Result<BotStats, Box<dyn Error>> {
    let tests: Collection<Test> = context.db.collection("testsv2");
    let users: Collection<User> = context.db.collection("usersv2");
    let prompts: Collection<Prompt> = context.db.collection("prompts");

    let total_tests = tests.count_documents(doc! {}, None).await?;
    let total_users = users.count_documents(doc! {}, None).await?;
    let total_prompts = prompts.count_documents(doc! {}, None).await?;

    Ok(BotStats {
        total_tests,
        total_users,
        total_prompts,
    })
}
