use axum::{response::Json, http::StatusCode};
use serde_json::{json, Value};

pub async fn health_check() -> (StatusCode, Json<Value>) {
    (
        StatusCode::OK,
        Json(json!({
            "status": "ok",
            "service": "skylens-backend",
            "version": "0.1.0"
        }))
    )
}
