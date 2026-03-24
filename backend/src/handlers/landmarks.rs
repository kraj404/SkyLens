use axum::{
    extract::State,
    response::Json,
    http::StatusCode,
};
use serde::{Deserialize, Serialize};
use sqlx::PgPool;

#[derive(Deserialize)]
pub struct NearbyRequest {
    pub latitude: f64,
    pub longitude: f64,
    pub radius_km: f64,
    #[serde(default = "default_limit")]
    pub limit: i32,
}

fn default_limit() -> i32 {
    50
}

#[derive(Serialize)]
pub struct Landmark {
    pub id: String,
    pub name: String,
    pub landmark_type: String,
    pub latitude: f64,
    pub longitude: f64,
    pub elevation_m: Option<i32>,
    pub distance_km: f64,
    pub importance_score: f32,
    pub country: Option<String>,
    pub ai_story: Option<String>,
    pub photo_urls: Vec<String>,
}

pub async fn get_nearby_landmarks(
    State(pool): State<PgPool>,
    Json(req): Json<NearbyRequest>,
) -> Result<Json<Vec<Landmark>>, StatusCode> {
    // Use PostGIS to find landmarks within radius
    let landmarks = sqlx::query!(
        r#"
        SELECT
            id,
            name,
            type as landmark_type,
            latitude,
            longitude,
            elevation_m,
            importance_score,
            country,
            ai_story,
            photo_urls,
            ST_Distance(
                ST_MakePoint(longitude, latitude)::geography,
                ST_MakePoint($1, $2)::geography
            ) / 1000.0 as "distance_km!"
        FROM landmarks
        WHERE ST_DWithin(
            ST_MakePoint(longitude, latitude)::geography,
            ST_MakePoint($1, $2)::geography,
            $3 * 1000
        )
        ORDER BY importance_score DESC, distance_km
        LIMIT $4
        "#,
        req.longitude,
        req.latitude,
        req.radius_km,
        req.limit as i64
    )
    .fetch_all(&pool)
    .await
    .map_err(|e| {
        tracing::error!("Database error: {}", e);
        StatusCode::INTERNAL_SERVER_ERROR
    })?;

    let result = landmarks
        .into_iter()
        .map(|row| Landmark {
            id: row.id,
            name: row.name,
            landmark_type: row.landmark_type,
            latitude: row.latitude,
            longitude: row.longitude,
            elevation_m: row.elevation_m,
            distance_km: row.distance_km,
            importance_score: row.importance_score,
            country: row.country,
            ai_story: row.ai_story,
            photo_urls: row.photo_urls.unwrap_or_default(),
        })
        .collect();

    Ok(Json(result))
}
