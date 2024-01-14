use mongodb::bson::DateTime;
use mongodb::bson::serde_helpers::bson_datetime_as_rfc3339_string;
use serde::{Deserialize, Serialize};

#[derive(Clone, Debug, PartialEq, Deserialize, Serialize)]
pub struct Prompt {
    pub title: String,
    pub text: String,
    pub rating: f64,
}

#[derive(Clone, Debug, PartialEq, Deserialize, Serialize)]
pub struct User {
    pub discordId: String,
    pub playtime: f64,
    pub totalTp: f64,
    pub userTag: String,
    pub daily: Daily,
}

#[derive(Clone, Debug, PartialEq, Deserialize, Serialize)]
pub struct Daily {
    pub currentStreak: u32,
    pub maxStreak: u32,
    //rfc3339
    #[serde(with = "bson_datetime_as_rfc3339_string")]
    pub updatedAt: DateTime,
}

#[derive(Clone, Debug, PartialEq, Deserialize, Serialize)]
pub struct Test {
    pub discordId: String,
    pub wpm: f64,
    pub accuracy: f64,
    pub tp: f64,
    pub timeTaken: u64,
    pub prompt: String,
    pub submittedText: String,
    pub date: DateTime
}
