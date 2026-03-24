#!/usr/bin/env python3
"""
Generate sample airports and landmarks data for SkyLens testing
Creates SQL insert statements for PostgreSQL/Supabase
"""

import json

# Sample airports for testing
sample_airports = [
    {"iata": "LAX", "icao": "KLAX", "name": "Los Angeles International Airport",
     "city": "Los Angeles", "country": "USA", "lat": 33.9416, "lon": -118.4085, "elev": 38},
    {"iata": "NRT", "icao": "RJAA", "name": "Tokyo Narita International Airport",
     "city": "Tokyo", "country": "Japan", "lat": 35.7647, "lon": 140.3864, "elev": 43},
    {"iata": "JFK", "icao": "KJFK", "name": "John F. Kennedy International Airport",
     "city": "New York", "country": "USA", "lat": 40.6413, "lon": -73.7781, "elev": 4},
    {"iata": "LHR", "icao": "EGLL", "name": "London Heathrow Airport",
     "city": "London", "country": "UK", "lat": 51.4700, "lon": -0.4543, "elev": 25},
    {"iata": "DXB", "icao": "OMDB", "name": "Dubai International Airport",
     "city": "Dubai", "country": "UAE", "lat": 25.2532, "lon": 55.3657, "elev": 19},
]

# Sample landmarks for testing
sample_landmarks = [
    {"name": "Mount Fuji", "type": "MOUNTAIN", "lat": 35.3606, "lon": 138.7274,
     "elev": 3776, "country": "Japan", "importance": 95.0,
     "wiki": "Q39231"},
    {"name": "Grand Canyon", "type": "CANYON", "lat": 36.1069, "lon": -112.1129,
     "elev": 2133, "country": "USA", "importance": 92.0,
     "wiki": "Q118841"},
    {"name": "Mount Everest", "type": "MOUNTAIN", "lat": 27.9881, "lon": 86.9250,
     "elev": 8849, "country": "Nepal", "importance": 98.0,
     "wiki": "Q513"},
    {"name": "Tokyo", "type": "CITY", "lat": 35.6762, "lon": 139.6503,
     "elev": 40, "country": "Japan", "importance": 90.0,
     "wiki": "Q1490"},
    {"name": "Los Angeles", "type": "CITY", "lat": 34.0522, "lon": -118.2437,
     "elev": 71, "country": "USA", "importance": 85.0,
     "wiki": "Q65"},
    {"name": "Rocky Mountains", "type": "MOUNTAIN", "lat": 39.7392, "lon": -104.9903,
     "elev": 4401, "country": "USA", "importance": 80.0,
     "wiki": "Q5463"},
    {"name": "Lake Tahoe", "type": "LAKE", "lat": 39.0968, "lon": -120.0324,
     "elev": 1897, "country": "USA", "importance": 70.0,
     "wiki": "Q127892"},
    {"name": "Mount Shasta", "type": "VOLCANO", "lat": 41.4093, "lon": -122.1949,
     "elev": 4322, "country": "USA", "importance": 72.0,
     "wiki": "Q194057"},
]

def generate_airport_sql():
    """Generate SQL INSERT statements for airports"""
    print("-- Sample Airports Data")
    print("INSERT INTO airports (iata_code, icao_code, name, city, country, location, elevation_m, timezone) VALUES")

    values = []
    for airport in sample_airports:
        values.append(
            f"('{airport['iata']}', '{airport['icao']}', '{airport['name']}', "
            f"'{airport['city']}', '{airport['country']}', "
            f"ST_Point({airport['lon']}, {airport['lat']})::geography, "
            f"{airport['elev']}, 'UTC')"
        )

    print(",\n".join(values) + ";")
    print()

def generate_landmark_sql():
    """Generate SQL INSERT statements for landmarks"""
    print("-- Sample Landmarks Data")
    print("INSERT INTO landmarks (id, name, type, location, elevation_m, importance_score, wiki_id, country) VALUES")

    values = []
    for i, landmark in enumerate(sample_landmarks):
        landmark_id = f"landmark-{i+1:04d}"
        values.append(
            f"('{landmark_id}', '{landmark['name']}', '{landmark['type']}', "
            f"ST_Point({landmark['lon']}, {landmark['lat']})::geography, "
            f"{landmark['elev']}, {landmark['importance']}, "
            f"'{landmark['wiki']}', '{landmark['country']}')"
        )

    print(",\n".join(values) + ";")
    print()

def generate_kotlin_test_data():
    """Generate Kotlin code for test data"""
    print("// Sample Test Data for Android")
    print("object TestData {")
    print()
    print("    val sampleAirports = listOf(")
    for airport in sample_airports:
        print(f"        Airport(")
        print(f"            iataCode = \"{airport['iata']}\",")
        print(f"            icaoCode = \"{airport['icao']}\",")
        print(f"            name = \"{airport['name']}\",")
        print(f"            city = \"{airport['city']}\",")
        print(f"            country = \"{airport['country']}\",")
        print(f"            latitude = {airport['lat']},")
        print(f"            longitude = {airport['lon']},")
        print(f"            elevationM = {airport['elev']},")
        print(f"            timezone = \"UTC\"")
        print(f"        ),")
    print("    )")
    print()
    print("    val sampleLandmarks = listOf(")
    for landmark in sample_landmarks:
        print(f"        Landmark(")
        print(f"            id = \"{landmark['name'].replace(' ', '-').lower()}\",")
        print(f"            name = \"{landmark['name']}\",")
        print(f"            type = LandmarkType.{landmark['type']},")
        print(f"            latitude = {landmark['lat']},")
        print(f"            longitude = {landmark['lon']},")
        print(f"            elevationM = {landmark['elev']},")
        print(f"            importanceScore = {landmark['importance']}f,")
        print(f"            wikiId = \"{landmark['wiki']}\",")
        print(f"            country = \"{landmark['country']}\",")
        print(f"            aiStory = null,")
        print(f"            photoUrls = emptyList()")
        print(f"        ),")
    print("    )")
    print("}")

if __name__ == "__main__":
    print("=" * 80)
    print("SKYLENS SAMPLE DATA GENERATOR")
    print("=" * 80)
    print()

    generate_airport_sql()
    print()
    print("=" * 80)
    print()
    generate_landmark_sql()
    print()
    print("=" * 80)
    print()
    generate_kotlin_test_data()
