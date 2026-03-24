-- Migration: 004_create_trips.sql
-- Trip history tracking

CREATE TABLE public.trips (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    departure_airport VARCHAR(3) NOT NULL,
    arrival_airport VARCHAR(3) NOT NULL,
    route_corridor GEOGRAPHY(LineString, 4326),
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    ai_summary TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_trips_user ON public.trips (user_id);
CREATE INDEX idx_trips_created ON public.trips (created_at DESC);

-- Trip events (landmarks seen during flight)
CREATE TABLE public.trip_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_id UUID REFERENCES public.trips(id) ON DELETE CASCADE,
    landmark_id UUID REFERENCES public.landmarks(id),
    event_time TIMESTAMPTZ NOT NULL,
    distance_km FLOAT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_trip_events_trip ON public.trip_events (trip_id);
CREATE INDEX idx_trip_events_time ON public.trip_events (event_time);

-- Row Level Security
ALTER TABLE public.trips ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.trip_events ENABLE ROW LEVEL SECURITY;

-- Trips: Users can only access their own trips
CREATE POLICY "Users can read own trips"
    ON public.trips
    FOR SELECT
    USING (auth.uid() = user_id OR user_id IS NULL);

CREATE POLICY "Users can insert own trips"
    ON public.trips
    FOR INSERT
    WITH CHECK (auth.uid() = user_id OR user_id IS NULL);

CREATE POLICY "Users can update own trips"
    ON public.trips
    FOR UPDATE
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own trips"
    ON public.trips
    FOR DELETE
    USING (auth.uid() = user_id);

-- Trip Events: Accessible if user owns the trip
CREATE POLICY "Users can read trip events for their trips"
    ON public.trip_events
    FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM public.trips
            WHERE id = trip_id
            AND (user_id = auth.uid() OR user_id IS NULL)
        )
    );

CREATE POLICY "Users can insert trip events for their trips"
    ON public.trip_events
    FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.trips
            WHERE id = trip_id
            AND (user_id = auth.uid() OR user_id IS NULL)
        )
    );
