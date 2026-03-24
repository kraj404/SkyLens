use axum::{
    extract::State,
    response::Json,
    http::StatusCode,
};
use serde::{Deserialize, Serialize};
use sqlx::PgPool;
use uuid::Uuid;

#[derive(Deserialize)]
pub struct CreateTripRequest {
    pub departure_airport: String,
    pub arrival_airport: String,
    pub start_time: Option<String>,
    pub route_geojson: serde_json::Value,
}

#[derive(Serialize)]
pub struct TripResponse {
    pub trip_id: String,
}

pub async fn create_trip(
    State(pool): State<PgPool>,
    Json(req): Json<CreateTripRequest>,
) -> Result<Json<TripResponse>, StatusCode> {
    let trip_id = Uuid::new_v4();

    sqlx::query!(
        r#"
        INSERT INTO trips (id, departure_airport, arrival_airport, start_time, route_geojson)
        VALUES ($1, $2, $3, $4, $5)
        "#,
        trip_id,
        req.departure_airport,
        req.arrival_airport,
        req.start_time,
        req.route_geojson
    )
    .execute(&pool)
    .await
    .map_err(|e| {
        tracing::error!("Database error: {}", e);
        StatusCode::INTERNAL_SERVER_ERROR
    })?;

    Ok(Json(TripResponse {
        trip_id: trip_id.to_string(),
    }))
}
