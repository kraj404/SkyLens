-- Create offline packs table
CREATE TABLE IF NOT EXISTS offline_packs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_key TEXT UNIQUE NOT NULL,
    departure_airport VARCHAR(3),
    arrival_airport VARCHAR(3),
    pack_size_mb REAL,
    landmark_count INTEGER,
    tile_count INTEGER,
    storage_url TEXT,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_offline_packs_route ON offline_packs(route_key);
CREATE INDEX idx_offline_packs_created ON offline_packs(created_at DESC);
