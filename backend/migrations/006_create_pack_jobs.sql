-- Add job tracking table for async pack generation

CREATE TABLE IF NOT EXISTS pack_generation_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    route_key TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('pending', 'processing', 'completed', 'failed')),
    error_message TEXT,
    landmarks_found INTEGER,
    tiles_downloaded INTEGER,
    progress_percent INTEGER DEFAULT 0,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_pack_jobs_route_key ON pack_generation_jobs(route_key);
CREATE INDEX idx_pack_jobs_status ON pack_generation_jobs(status);
