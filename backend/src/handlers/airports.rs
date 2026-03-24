use axum::{
    extract::{State, Query},
    response::Json,
    http::StatusCode,
};
use serde::{Deserialize, Serialize};
use sqlx::PgPool;

#[derive(Deserialize)]
pub struct SearchQuery {
    search: String,
    #[serde(default = "default_limit")]
    limit: i32,
}

fn default_limit() -> i32 {
    10
}

#[derive(Serialize, Sqlx)]
pub struct Airport {
    pub iata_code: String,
    pub name: String,
    pub city: Option<String>,
    pub country: Option<String>,
    pub latitude: f64,
    pub longitude: f64,
}

pub async fn search_airports(
    State(pool): State<PgPool>,
    Query(params): Query<SearchQuery>,
) -> Result<Json<Vec<Airport>>, StatusCode> {
    let search_pattern = format!("%{}%", params.search.to_uppercase());

    let airports = sqlx::query_as!(
        Airport,
        r#"
        SELECT iata_code, name, city, country, latitude, longitude
        FROM airports
        WHERE
            iata_code ILIKE $1
            OR name ILIKE $1
            OR city ILIKE $1
        ORDER BY
            CASE
                WHEN iata_code = $2 THEN 0
                WHEN iata_code ILIKE $1 THEN 1
                WHEN name ILIKE $1 THEN 2
                ELSE 3
            END,
            name
        LIMIT $3
        "#,
        search_pattern,
        params.search.to_uppercase(),
        params.limit as i64
    )
    .fetch_all(&pool)
    .await
    .map_err(|e| {
        tracing::error!("Database error: {}", e);
        StatusCode::INTERNAL_SERVER_ERROR
    })?;

    Ok(Json(airports))
}
