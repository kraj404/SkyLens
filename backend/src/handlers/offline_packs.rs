use axum::{
    extract::{State, Path},
    response::Json,
    http::StatusCode,
};
use serde::{Deserialize, Serialize};
use sqlx::PgPool;
use uuid::Uuid;

#[derive(Deserialize)]
pub struct GeneratePackRequest {
    pub departure: String,
    pub arrival: String,
}

#[derive(Serialize)]
pub struct OfflinePackResponse {
    pub pack_id: String,
    pub route_key: String,
    pub pack_size_mb: f64,
    pub landmark_count: i32,
    pub download_url: String,
    pub created_at: String,
}

pub async fn generate_pack(
    State(pool): State<PgPool>,
    Json(req): Json<GeneratePackRequest>,
) -> Result<Json<OfflinePackResponse>, StatusCode> {
    let route_key = format!("{}-{}", req.departure, req.arrival);

    // Check if pack already exists
    let existing = sqlx::query!(
        r#"
        SELECT id, pack_size_mb, landmark_count, storage_url, created_at
        FROM offline_packs
        WHERE route_key = $1
        ORDER BY created_at DESC
        LIMIT 1
        "#,
        route_key
    )
    .fetch_optional(&pool)
    .await
    .map_err(|e| {
        tracing::error!("Database error: {}", e);
        StatusCode::INTERNAL_SERVER_ERROR
    })?;

    if let Some(pack) = existing {
        return Ok(Json(OfflinePackResponse {
            pack_id: pack.id.to_string(),
            route_key: route_key.clone(),
            pack_size_mb: pack.pack_size_mb.unwrap_or(0.0),
            landmark_count: pack.landmark_count.unwrap_or(0),
            download_url: pack.storage_url.unwrap_or_default(),
            created_at: pack.created_at.unwrap_or_default().to_string(),
        }));
    }

    // Generate new pack
    // TODO: Implement actual pack generation
    // 1. Get airport coordinates
    // 2. Calculate great circle route
    // 3. Buffer corridor ±200km
    // 4. Query landmarks
    // 5. Download map tiles
    // 6. Create ZIP
    // 7. Upload to R2
    // 8. Store metadata

    let pack_id = Uuid::new_v4();
    let pack_size_mb = 487.0; // Mock value
    let landmark_count = 2134; // Mock value
    let storage_url = format!("https://r2.skylens.app/packs/{}.skylens", route_key);

    sqlx::query!(
        r#"
        INSERT INTO offline_packs (id, route_key, departure_airport, arrival_airport, pack_size_mb, landmark_count, storage_url)
        VALUES ($1, $2, $3, $4, $5, $6, $7)
        "#,
        pack_id,
        route_key,
        req.departure,
        req.arrival,
        pack_size_mb,
        landmark_count,
        storage_url
    )
    .execute(&pool)
    .await
    .map_err(|e| {
        tracing::error!("Database error: {}", e);
        StatusCode::INTERNAL_SERVER_ERROR
    })?;

    Ok(Json(OfflinePackResponse {
        pack_id: pack_id.to_string(),
        route_key: route_key.clone(),
        pack_size_mb,
        landmark_count,
        download_url: storage_url,
        created_at: chrono::Utc::now().to_rfc3339(),
    }))
}

pub async fn get_pack(
    State(pool): State<PgPool>,
    Path(route_key): Path<String>,
) -> Result<Json<OfflinePackResponse>, StatusCode> {
    let pack = sqlx::query!(
        r#"
        SELECT id, pack_size_mb, landmark_count, storage_url, created_at
        FROM offline_packs
        WHERE route_key = $1
        ORDER BY created_at DESC
        LIMIT 1
        "#,
        route_key
    )
    .fetch_optional(&pool)
    .await
    .map_err(|e| {
        tracing::error!("Database error: {}", e);
        StatusCode::INTERNAL_SERVER_ERROR
    })?;

    match pack {
        Some(pack) => Ok(Json(OfflinePackResponse {
            pack_id: pack.id.to_string(),
            route_key: route_key.clone(),
            pack_size_mb: pack.pack_size_mb.unwrap_or(0.0),
            landmark_count: pack.landmark_count.unwrap_or(0),
            download_url: pack.storage_url.unwrap_or_default(),
            created_at: pack.created_at.unwrap_or_default().to_string(),
        })),
        None => Err(StatusCode::NOT_FOUND),
    }
}
