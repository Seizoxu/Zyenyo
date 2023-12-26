use std::env;

mod models;


use models::User;

use actix_web::{get, post, web, App, HttpResponse, HttpServer, Responder, http};
use actix_cors::Cors;
use mongodb::{Client, Collection, bson::doc};
use serde_json::json;

#[derive(Clone)]
pub struct Context {
    db: Client,
    environment: String,
}

#[get("/")]
async fn hello() -> impl Responder {
    HttpResponse::Ok().json(json!({"ping": "hello world!"}))
}

#[post("/echo")]
async fn echo(req_body: String) -> impl Responder {
    HttpResponse::Ok().body(req_body)
}

#[get("/get_user/{discordId}")]
async fn get_user(context: web::Data<Context>, discordId: web::Path<String>) -> HttpResponse {
    let discord_id = discordId.into_inner();
    let collection: Collection<User> = context.db.database("ZyenyoStaging").collection("usersv2");
    println!("{discord_id}");

    match collection.find_one(doc! { "discordId": &discord_id }, None).await {
        Ok(Some(user)) => HttpResponse::Ok().json(user),
        Ok(None) => {
            HttpResponse::NotFound().body(format!("No user found with discord ID {discord_id}"))
        }
        Err(err) => HttpResponse::InternalServerError().body(err.to_string()),
    }
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    env::set_var("RUST_LOG", "debug");
    let uri = env::var("MONGO_URI").expect("database URI not provided");
    let client = Client::with_uri_str(uri).await.expect("failed to connect to database");
    let environment = env::var("ZYENYO_ENVIRONMENT").expect("ENVIRONMENT not provided");
    
    let context = Context {
        db: client,
        environment,
    };

    HttpServer::new(move || {
        let cors = match context.environment.as_str() {
            "development" => Cors::permissive(),
            "production" => Cors::default().allowed_origin("http://localhost:80").allowed_methods(vec!["GET", "POST"]),
            _ => panic!()
        };

        App::new()
            .wrap(cors)
            .app_data(web::Data::new(context.clone()))
            .service(hello)
            .service(echo)
            .service(get_user)
    })
    .bind("0.0.0.0:8000")?
    .run()
    .await
}
