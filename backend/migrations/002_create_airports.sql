-- Create airports table
CREATE TABLE IF NOT EXISTS airports (
    id SERIAL PRIMARY KEY,
    iata_code VARCHAR(3) UNIQUE NOT NULL,
    icao_code VARCHAR(4),
    name TEXT NOT NULL,
    city TEXT,
    country TEXT,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    elevation_m INTEGER,
    timezone TEXT
);

CREATE INDEX idx_airports_iata ON airports(iata_code);
CREATE INDEX idx_airports_name ON airports(name);
CREATE INDEX idx_airports_location ON airports(latitude, longitude);
