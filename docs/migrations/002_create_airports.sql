-- Migration: 002_create_airports.sql
-- Airport reference data

CREATE TABLE public.airports (
    id SERIAL PRIMARY KEY,
    iata_code VARCHAR(3) UNIQUE NOT NULL,
    icao_code VARCHAR(4),
    name TEXT NOT NULL,
    city TEXT,
    country TEXT,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    elevation_m INTEGER,
    timezone TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Spatial index for location queries
CREATE INDEX idx_airports_location ON public.airports USING GIST(location);

-- Index for code lookups
CREATE INDEX idx_airports_iata ON public.airports (iata_code);

-- Public read access (no auth required)
ALTER TABLE public.airports ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Anyone can read airports"
    ON public.airports
    FOR SELECT
    USING (true);

-- Sample data inserts
INSERT INTO public.airports (iata_code, icao_code, name, city, country, location, elevation_m, timezone) VALUES
('LAX', 'KLAX', 'Los Angeles International Airport', 'Los Angeles', 'USA', ST_Point(-118.4085, 33.9416)::geography, 38, 'America/Los_Angeles'),
('NRT', 'RJAA', 'Tokyo Narita International Airport', 'Tokyo', 'Japan', ST_Point(140.3864, 35.7647)::geography, 43, 'Asia/Tokyo'),
('JFK', 'KJFK', 'John F. Kennedy International Airport', 'New York', 'USA', ST_Point(-73.7781, 40.6413)::geography, 4, 'America/New_York'),
('LHR', 'EGLL', 'London Heathrow Airport', 'London', 'UK', ST_Point(-0.4543, 51.4700)::geography, 25, 'Europe/London'),
('DXB', 'OMDB', 'Dubai International Airport', 'Dubai', 'UAE', ST_Point(55.3657, 25.2532)::geography, 19, 'Asia/Dubai'),
('SFO', 'KSFO', 'San Francisco International Airport', 'San Francisco', 'USA', ST_Point(-122.3790, 37.6213)::geography, 4, 'America/Los_Angeles'),
('ORD', 'KORD', 'O''Hare International Airport', 'Chicago', 'USA', ST_Point(-87.9048, 41.9742)::geography, 205, 'America/Chicago'),
('CDG', 'LFPG', 'Charles de Gaulle Airport', 'Paris', 'France', ST_Point(2.5479, 49.0097)::geography, 119, 'Europe/Paris');
