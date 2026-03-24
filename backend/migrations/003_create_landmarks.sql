-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Create landmarks table
CREATE TABLE IF NOT EXISTS landmarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    elevation_m INTEGER,
    importance_score REAL NOT NULL DEFAULT 0,
    wiki_id TEXT,
    country TEXT,
    ai_story TEXT,
    photo_urls TEXT[],
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create spatial index using PostGIS
CREATE INDEX idx_landmarks_location ON landmarks USING GIST (ST_MakePoint(longitude, latitude));
CREATE INDEX idx_landmarks_importance ON landmarks(importance_score DESC);
CREATE INDEX idx_landmarks_type ON landmarks(type);
