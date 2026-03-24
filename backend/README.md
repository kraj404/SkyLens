# SkyLens Backend API

Rust + Axum backend for SkyLens flight tracking app.

## Features

- Airport search API
- Landmark discovery with PostGIS spatial queries
- Offline pack generation
- Trip tracking
- Health check endpoint

## Tech Stack

- **Framework**: Axum 0.7
- **Database**: PostgreSQL 16 + PostGIS 3.4
- **ORM**: SQLx (compile-time checked queries)
- **Geospatial**: geo crate + PostGIS
- **Deployment**: Fly.io

## Setup

### Prerequisites

- Rust 1.80+
- PostgreSQL 16 with PostGIS extension
- Python 3.x (for data import scripts)

### Environment Variables

Copy `.env.example` to `.env` and configure:

```bash
cp .env.example .env
```

Edit `.env` with your credentials:
- `DATABASE_URL`: PostgreSQL connection string
- `CLAUDE_API_KEY`: Anthropic Claude API key
- `R2_*`: Cloudflare R2 credentials

### Database Setup

```bash
# Create database
createdb skylens

# Enable PostGIS
psql skylens -c "CREATE EXTENSION postgis;"

# Run migrations (handled automatically on startup)
cargo run
```

### Import Data

```bash
# Download airport data
wget https://raw.githubusercontent.com/jpatokal/openflights/master/data/airports.dat

# Import airports
python3 scripts/import_airports.py airports.dat $DATABASE_URL

# Import landmarks (uses Wikidata API)
python3 scripts/import_landmarks.py $DATABASE_URL
```

## Development

```bash
# Run server
cargo run

# Run with auto-reload
cargo watch -x run

# Run tests
cargo test

# Check code
cargo clippy
```

## API Endpoints

### Health Check
```
GET /health
```

### Airports
```
GET /api/v1/airports?search=LAX&limit=10
```

### Landmarks
```
POST /api/v1/landmarks/nearby
{
  "latitude": 35.6762,
  "longitude": 139.6503,
  "radius_km": 200,
  "limit": 50
}
```

### Offline Packs
```
POST /api/v1/offline-packs/generate
{
  "departure": "LAX",
  "arrival": "NRT"
}

GET /api/v1/offline-packs/LAX-NRT
```

### Trips
```
POST /api/v1/trips
{
  "departure_airport": "LAX",
  "arrival_airport": "NRT",
  "start_time": "2026-03-24T10:00:00Z",
  "route_geojson": { ... }
}
```

## Deployment

### Fly.io

```bash
# Install Fly CLI
curl -L https://fly.io/install.sh | sh

# Login
fly auth login

# Create app
fly launch

# Set secrets
fly secrets set DATABASE_URL=postgresql://...
fly secrets set CLAUDE_API_KEY=sk-ant-...

# Deploy
fly deploy
```

### Docker

```bash
# Build image
docker build -t skylens-backend .

# Run container
docker run -p 3000:3000 --env-file .env skylens-backend
```

## Database Schema

See `migrations/` directory for full schema.

Key tables:
- `airports` - Airport reference data
- `landmarks` - Points of interest with spatial index
- `trips` - User flight records
- `trip_events` - Landmark sightings during flights
- `offline_packs` - Downloadable offline data packs

## Performance

- PostGIS spatial indexes for fast landmark queries (<50ms)
- Connection pooling with SQLx
- Gzip compression on responses
- CORS enabled for web clients

## License

MIT
