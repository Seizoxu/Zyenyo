use std::error::Error;

use crate::{models::Prompt, Context};
use actix_web::{get, web, HttpResponse, Responder};
use futures::stream::TryStreamExt;
use mongodb::{
    bson::{self, doc, Document},
    Collection,
};
use serde::Deserialize;

const PAGE_DEFAULT: u32 = 1;
const PAGE_SIZE_DEFAULT: u32 = 20;
const SORT_BY_DEFAULT: &str = "title";
const SORT_ORDER_DEFAULT: i32 = 1;
const SEARCH_QUERY_DEFAULT: &str = "";
fn page_default() -> u32 {
    PAGE_DEFAULT
}
fn page_size_default() -> u32 {
    PAGE_SIZE_DEFAULT
}
fn sort_by_default() -> String {
    SORT_BY_DEFAULT.to_owned()
}
fn sort_order_default() -> i32 {
    SORT_ORDER_DEFAULT
}
fn search_query_default() -> String {
    SEARCH_QUERY_DEFAULT.to_owned()
}

#[derive(Deserialize)]
struct PromptsConfig {
    #[serde(default = "page_default")]
    page: u32,
    #[serde(default = "page_size_default")]
    page_size: u32,
    #[serde(default = "sort_by_default")]
    sort_by: String,
    #[serde(default = "sort_order_default")]
    sort_order: i32,
    #[serde(default = "search_query_default")]
    search_query: String,
}

#[get("/prompts")]
async fn prompts(
    context: web::Data<Context>,
    controls: web::Query<PromptsConfig>,
) -> impl Responder {
    let info = controls.into_inner();

    match prompt_query(context, info).await {
        Ok(prompts_vec) => HttpResponse::Ok().json(prompts_vec),
        Err(e) => HttpResponse::InternalServerError().body(e.to_string()),
    }
}

async fn prompt_query(
    context: web::Data<Context>,
    controls: PromptsConfig,
) -> Result<Vec<Prompt>, Box<dyn Error>> {
    let collection: Collection<Prompt> = context.db.collection("prompts");

    let pipeline = vec![
        doc! {"$match":
            doc! {"$expr":
                doc! {"$or": [
                    doc! { "$regexMatch": doc! {"input": "$title", "regex": &controls.search_query, "options": "i"}},
                    doc! { "$regexMatch": doc! {"input": "$text", "regex": &controls.search_query, "options": "i"}}
                ] },
            }
        },
        doc! {"$sort": doc! {&controls.sort_by: &controls.sort_order}},
        doc! {"$skip": (&controls.page-1)*&controls.page_size},
        doc! {"$limit": &controls.page_size},
    ];

    let results = collection.aggregate(pipeline, None).await?;
    let prompts_vec: Vec<Document> = results.try_collect().await?;
    Ok(prompts_vec
        .iter()
        .filter_map(|doc| bson::from_document::<Prompt>(doc.to_owned()).ok())
        .collect())
}
