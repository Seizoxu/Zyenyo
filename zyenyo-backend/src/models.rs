use serde::{Deserialize, Serialize};

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
    pub updatedAt: String,
}
