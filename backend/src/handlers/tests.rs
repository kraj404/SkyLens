#[cfg(test)]
mod tests {
    use super::*;
    use axum::{
        body::Body,
        http::{Request, StatusCode},
    };
    use tower::ServiceExt;

    #[tokio::test]
    async fn test_health_check_returns_ok() {
        let (status, response) = health_check().await;

        assert_eq!(status, StatusCode::OK);

        let body = response.0;
        assert_eq!(body["status"], "ok");
        assert_eq!(body["service"], "skylens-backend");
        assert_eq!(body["version"], "0.1.0");
    }

    #[tokio::test]
    async fn test_health_check_response_structure() {
        let (_, response) = health_check().await;

        let body = response.0;

        // Verify all required fields are present
        assert!(body.get("status").is_some());
        assert!(body.get("service").is_some());
        assert!(body.get("version").is_some());
    }
}
