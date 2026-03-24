#[cfg(test)]
mod tests {
    use super::*;
    use geo_types::Point;

    #[test]
    fn test_great_circle_path_generates_correct_segments() {
        let start = Point::new(-118.4085, 33.9416); // LAX
        let end = Point::new(140.3864, 35.7647);    // NRT
        let segments = 100;

        let path = great_circle_path(start, end, segments);

        assert_eq!(path.0.len(), segments + 1); // segments + 1 points
        assert_eq!(path.0[0], start);
        assert_eq!(path.0[segments], end);
    }

    #[test]
    fn test_haversine_distance_lax_to_nrt() {
        let lax_lat = 33.9416;
        let lax_lon = -118.4085;
        let nrt_lat = 35.7647;
        let nrt_lon = 140.3864;

        let distance = haversine_distance(lax_lat, lax_lon, nrt_lat, nrt_lon);

        // Should be approximately 8815 km
        assert!((distance - 8815.0).abs() < 100.0);
    }

    #[test]
    fn test_haversine_distance_same_point() {
        let distance = haversine_distance(40.7128, -74.0060, 40.7128, -74.0060);

        assert!(distance < 0.01); // Should be essentially 0
    }

    #[test]
    fn test_haversine_distance_ny_to_london() {
        let distance = haversine_distance(
            40.7128, -74.0060,  // New York
            51.5074, -0.1278    // London
        );

        // Should be approximately 5570 km
        assert!((distance - 5570.0).abs() < 100.0);
    }

    #[test]
    fn test_interpolate_spherical_midpoint() {
        let start = Point::new(0.0, 0.0);
        let end = Point::new(10.0, 10.0);

        let midpoint = interpolate_spherical(start, end, 0.5);

        // Midpoint should be roughly halfway
        assert!((midpoint.x() - 5.0).abs() < 1.0);
        assert!((midpoint.y() - 5.0).abs() < 1.0);
    }

    #[test]
    fn test_interpolate_spherical_at_start() {
        let start = Point::new(-118.0, 34.0);
        let end = Point::new(139.0, 35.0);

        let point = interpolate_spherical(start, end, 0.0);

        assert!((point.x() - start.x()).abs() < 0.001);
        assert!((point.y() - start.y()).abs() < 0.001);
    }

    #[test]
    fn test_interpolate_spherical_at_end() {
        let start = Point::new(-118.0, 34.0);
        let end = Point::new(139.0, 35.0);

        let point = interpolate_spherical(start, end, 1.0);

        assert!((point.x() - end.x()).abs() < 0.001);
        assert!((point.y() - end.y()).abs() < 0.001);
    }

    #[test]
    fn test_great_circle_handles_date_line_crossing() {
        let start = Point::new(179.0, 35.0);
        let end = Point::new(-179.0, 35.0);

        let path = great_circle_path(start, end, 10);

        // Path should go across date line (short way), not around the world
        assert!(path.0.len() == 11);

        // Intermediate points should handle date line properly
        for point in path.0.iter() {
            assert!(point.x() >= -180.0 && point.x() <= 180.0);
        }
    }

    #[test]
    fn test_great_circle_equator_crossing() {
        let start = Point::new(0.0, -10.0); // South of equator
        let end = Point::new(0.0, 10.0);    // North of equator

        let path = great_circle_path(start, end, 20);

        // Should cross equator (have a point near latitude 0)
        let has_equator_point = path.0.iter().any(|p| p.y().abs() < 1.0);
        assert!(has_equator_point);
    }

    #[test]
    fn test_haversine_distance_handles_negative_longitudes() {
        // Los Angeles (negative longitude) to Tokyo (positive longitude)
        let distance = haversine_distance(33.9416, -118.4085, 35.7647, 140.3864);

        assert!(distance > 8000.0 && distance < 9000.0);
    }

    #[test]
    fn test_great_circle_path_consistency() {
        let start = Point::new(0.0, 0.0);
        let end = Point::new(10.0, 10.0);

        let path1 = great_circle_path(start, end, 100);
        let path2 = great_circle_path(start, end, 100);

        // Same inputs should produce same outputs
        assert_eq!(path1.0.len(), path2.0.len());

        for (p1, p2) in path1.0.iter().zip(path2.0.iter()) {
            assert!((p1.x() - p2.x()).abs() < 0.0001);
            assert!((p1.y() - p2.y()).abs() < 0.0001);
        }
    }
}
