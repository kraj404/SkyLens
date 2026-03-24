pub mod great_circle;

#[cfg(test)]
mod tests;

use geo_types::{Point, LineString};

/// Calculate great circle path between two points
pub fn great_circle_path(start: Point<f64>, end: Point<f64>, segments: usize) -> LineString<f64> {
    let mut points = vec![start];

    for i in 1..segments {
        let fraction = i as f64 / segments as f64;
        let point = interpolate_spherical(start, end, fraction);
        points.push(point);
    }

    points.push(end);
    LineString::new(points)
}

/// Interpolate point along great circle
fn interpolate_spherical(p1: Point<f64>, p2: Point<f64>, t: f64) -> Point<f64> {
    let lat1 = p1.y().to_radians();
    let lon1 = p1.x().to_radians();
    let lat2 = p2.y().to_radians();
    let lon2 = p2.x().to_radians();

    let d = haversine_angular_distance(lat1, lon1, lat2, lon2);

    if d == 0.0 {
        return p1;
    }

    let a = ((1.0 - t) * d).sin() / d.sin();
    let b = (t * d).sin() / d.sin();

    let x = a * lat1.cos() * lon1.cos() + b * lat2.cos() * lon2.cos();
    let y = a * lat1.cos() * lon1.sin() + b * lat2.cos() * lon2.sin();
    let z = a * lat1.sin() + b * lat2.sin();

    let lat = z.atan2((x.powi(2) + y.powi(2)).sqrt());
    let lon = y.atan2(x);

    Point::new(lon.to_degrees(), lat.to_degrees())
}

fn haversine_angular_distance(lat1: f64, lon1: f64, lat2: f64, lon2: f64) -> f64 {
    let dlat = lat2 - lat1;
    let dlon = lon2 - lon1;

    let a = (dlat / 2.0).sin().powi(2) + lat1.cos() * lat2.cos() * (dlon / 2.0).sin().powi(2);
    2.0 * a.sqrt().atan2((1.0 - a).sqrt())
}

/// Calculate haversine distance in kilometers
pub fn haversine_distance(lat1: f64, lon1: f64, lat2: f64, lon2: f64) -> f64 {
    const EARTH_RADIUS_KM: f64 = 6371.0;

    let dlat = (lat2 - lat1).to_radians();
    let dlon = (lon2 - lon1).to_radians();
    let lat1_rad = lat1.to_radians();
    let lat2_rad = lat2.to_radians();

    let a = (dlat / 2.0).sin().powi(2)
        + lat1_rad.cos() * lat2_rad.cos() * (dlon / 2.0).sin().powi(2);

    let c = 2.0 * a.sqrt().atan2((1.0 - a).sqrt());

    EARTH_RADIUS_KM * c
}
