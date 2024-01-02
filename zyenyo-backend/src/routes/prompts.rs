use std::error::Error;

use actix_web::{get, Responder, HttpResponse, web};
use mongodb::{Collection, bson::{doc, Document, self}};
use serde::Deserialize;
use crate::{Context, models::Prompt};
use futures::stream::TryStreamExt;

const PAGE_DEFAULT: u32 = 1;
const PAGE_SIZE_DEFAULT: u32 = 20;
const SORT_BY_DEFAULT: &str = "title";
const SORT_ORDER_DEFAULT: i32 = 1;
fn page_default() -> u32 { PAGE_DEFAULT }
fn page_size_default() -> u32 { PAGE_SIZE_DEFAULT }
fn sort_by_default() -> String { SORT_BY_DEFAULT.to_owned() }
fn sort_order_default() -> i32 { SORT_ORDER_DEFAULT }

#[derive(Deserialize)]
struct PromptsConfig {
    #[serde(default = "page_default")]
    page:  u32,
    #[serde(default = "page_size_default")]
    page_size: u32,
    #[serde(default = "sort_by_default")]
    sort_by: String,
    #[serde(default = "sort_order_default")]
    sort_order: i32,

}


#[get("/prompts")]
async fn prompts(context: web::Data<Context>, controls: web::Query<PromptsConfig>) -> impl Responder {
    let info = controls.into_inner();

    match prompt_query(context, info).await {
        Ok(prompts_vec) => HttpResponse::Ok().json(prompts_vec),
        Err(e) => HttpResponse::InternalServerError().body(e.to_string())
    }
}

async fn prompt_query(context: web::Data<Context>, controls: PromptsConfig) -> Result<Vec<Prompt>, Box<dyn Error>> {
    let collection: Collection<Prompt> = context.db.collection("prompts");
    
    let pipeline = vec![
        doc! {"$sort": doc! {&controls.sort_by: &controls.sort_order}},
        doc! {"$skip": (&controls.page-1)*&controls.page_size},
        doc! {"$limit": &controls.page_size}
    ];

    let results = collection.aggregate(pipeline, None).await?;
    let prompts_vec: Vec<Document> = results.try_collect().await?;
    Ok(prompts_vec.iter().filter_map(|doc| bson::from_document::<Prompt>(doc.to_owned()).ok()).collect())
}
