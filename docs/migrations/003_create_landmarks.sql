-- Migration: 003_create_landmarks.sql
-- Landmark data for visible points of interest

CREATE TABLE public.landmarks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    elevation_m INTEGER,
    importance_score FLOAT NOT NULL DEFAULT 0,
    wiki_id TEXT,
    country TEXT,
    ai_story TEXT,
    photo_urls TEXT[],
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Spatial index for proximity queries
CREATE INDEX idx_landmarks_location ON public.landmarks USING GIST(location);

-- Index for importance-based sorting
CREATE INDEX idx_landmarks_importance ON public.landmarks (importance_score DESC);

-- Index for type filtering
CREATE INDEX idx_landmarks_type ON public.landmarks (type);

-- Public read access
ALTER TABLE public.landmarks ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Anyone can read landmarks"
    ON public.landmarks
    FOR SELECT
    USING (true);

-- Sample landmark data
INSERT INTO public.landmarks (name, type, location, elevation_m, importance_score, wiki_id, country) VALUES
('Mount Fuji', 'MOUNTAIN', ST_Point(138.7274, 35.3606)::geography, 3776, 95.0, 'Q39231', 'Japan'),
('Grand Canyon', 'CANYON', ST_Point(-112.1129, 36.1069)::geography, 2133, 92.0, 'Q118841', 'USA'),
('Mount Everest', 'MOUNTAIN', ST_Point(86.9250, 27.9881)::geography, 8849, 98.0, 'Q513', 'Nepal'),
('Tokyo', 'CITY', ST_Point(139.6503, 35.6762)::geography, 40, 90.0, 'Q1490', 'Japan'),
('Los Angeles', 'CITY', ST_Point(-118.2437, 34.0522)::geography, 71, 85.0, 'Q65', 'USA'),
('Rocky Mountains', 'MOUNTAIN', ST_Point(-104.9903, 39.7392)::geography, 4401, 80.0, 'Q5463', 'USA'),
('Lake Tahoe', 'LAKE', ST_Point(-120.0324, 39.0968)::geography, 1897, 70.0, 'Q127892', 'USA'),
('Mount Shasta', 'VOLCANO', ST_Point(-122.1949, 41.4093)::geography, 4322, 72.0, 'Q194057', 'USA'),
('Yosemite Valley', 'NATURAL_WONDER', ST_Point(-119.5383, 37.7455)::geography, 1200, 85.0, 'Q200984', 'USA'),
('Mount Whitney', 'MOUNTAIN', ST_Point(-118.2920, 36.5785)::geography, 4421, 78.0, 'Q194218', 'USA'),
('San Francisco', 'CITY', ST_Point(-122.4194, 37.7749)::geography, 16, 88.0, 'Q62', 'USA'),
('Kyoto', 'CITY', ST_Point(135.7681, 35.0116)::geography, 50, 82.0, 'Q34600', 'Japan'),
('Mount Rainier', 'VOLCANO', ST_Point(-121.7603, 46.8523)::geography, 4392, 75.0, 'Q194057', 'USA');

-- Function to search nearby landmarks
CREATE OR REPLACE FUNCTION search_nearby_landmarks(
    lat DOUBLE PRECISION,
    lon DOUBLE PRECISION,
    radius_meters INTEGER,
    result_limit INTEGER DEFAULT 50
)
RETURNS TABLE (
    id UUID,
    name TEXT,
    type TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    elevation_m INTEGER,
    importance_score FLOAT,
    distance_m DOUBLE PRECISION
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        l.id,
        l.name,
        l.type,
        ST_Y(l.location::geometry) as latitude,
        ST_X(l.location::geometry) as longitude,
        l.elevation_m,
        l.importance_score,
        ST_Distance(l.location, ST_Point(lon, lat)::geography) as distance_m
    FROM public.landmarks l
    WHERE ST_DWithin(
        l.location,
        ST_Point(lon, lat)::geography,
        radius_meters
    )
    ORDER BY importance_score DESC, distance_m ASC
    LIMIT result_limit;
END;
$$ LANGUAGE plpgsql;
