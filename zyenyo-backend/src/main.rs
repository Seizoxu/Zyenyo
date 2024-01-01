use std::env;

mod models;
mod routes;

use actix_web::{web, App, HttpServer};
use actix_cors::Cors;
use mongodb::{Client, Database};
use routes::api_config;

#[derive(Clone)]
pub struct Context {
    db: Database,
    environment: String,
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    env::set_var("RUST_LOG", "debug");
    let uri = env::var("MONGO_URI").expect("database URI not provided");
    let client = Client::with_uri_str(uri).await.expect("failed to connect to database");
    let environment = env::var("ZYENYO_ENVIRONMENT").expect("ENVIRONMENT not provided");
    
    HttpServer::new(move || {
        let mut cors = Cors::permissive();
        let context = match environment.as_str() {
            "development" => {
                Context {
                    db: client.database("ZyenyoStaging"),
                    environment: environment.to_owned()
                }
            },
            "production" => {
                cors = Cors::default().allowed_origin("https://zyenyobot.com").allowed_methods(vec!["GET", "POST"]);
                Context {
                    db: client.database("MyDatabase"),
                    environment: environment.to_owned()
                }
            },
            _ => panic!()
        };

        App::new()
            .wrap(cors)
            .app_data(web::Data::new(context.clone()))
            .service(web::scope("/api").configure(api_config))
    })
    .bind("0.0.0.0:8000")?
    .run()
    .await
}
