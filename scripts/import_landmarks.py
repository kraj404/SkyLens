#!/usr/bin/env python3
"""
Import landmark data from various sources into PostgreSQL database.
Uses Wikidata SPARQL queries to fetch major landmarks.
"""

import requests
import psycopg2
import sys
import uuid
import time

def query_wikidata_landmarks():
    """Query Wikidata for major landmarks worldwide"""

    # SPARQL query for mountains
    mountains_query = """
    SELECT DISTINCT ?item ?itemLabel ?coord ?elevation ?country ?countryLabel
    WHERE {
      ?item wdt:P31 wd:Q8502;  # instance of mountain
            wdt:P625 ?coord;    # coordinate location
            wdt:P2044 ?elevation. # elevation
      OPTIONAL { ?item wdt:P17 ?country. }
      FILTER(?elevation > 2000)  # Filter mountains > 2000m
      SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
    }
    LIMIT 1000
    """

    # SPARQL query for cities
    cities_query = """
    SELECT DISTINCT ?item ?itemLabel ?coord ?population ?country ?countryLabel
    WHERE {
      ?item wdt:P31/wdt:P279* wd:Q515;  # instance of city
            wdt:P625 ?coord;              # coordinate location
            wdt:P1082 ?population.        # population
      OPTIONAL { ?item wdt:P17 ?country. }
      FILTER(?population > 500000)        # Cities with >500k population
      SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
    }
    LIMIT 1000
    """

    endpoint = "https://query.wikidata.org/sparql"
    headers = {"User-Agent": "SkyLens/1.0 (landmark data import)"}

    landmarks = []

    # Fetch mountains
    print("Fetching mountains from Wikidata...")
    response = requests.get(endpoint, params={"query": mountains_query, "format": "json"}, headers=headers)
    if response.status_code == 200:
        results = response.json()["results"]["bindings"]
        for result in results:
            try:
                coords = result["coord"]["value"].split("(")[1].split(")")[0].split()
                landmarks.append({
                    "name": result["itemLabel"]["value"],
                    "type": "mountain",
                    "latitude": float(coords[1]),
                    "longitude": float(coords[0]),
                    "elevation_m": int(float(result["elevation"]["value"])),
                    "country": result.get("countryLabel", {}).get("value"),
                    "wiki_id": result["item"]["value"].split("/")[-1],
                    "importance_score": 50.0 + (float(result["elevation"]["value"]) / 100.0)
                })
            except:
                continue

    time.sleep(1)  # Rate limiting

    # Fetch cities
    print("Fetching cities from Wikidata...")
    response = requests.get(endpoint, params={"query": cities_query, "format": "json"}, headers=headers)
    if response.status_code == 200:
        results = response.json()["results"]["bindings"]
        for result in results:
            try:
                coords = result["coord"]["value"].split("(")[1].split(")")[0].split()
                population = int(float(result["population"]["value"]))
                landmarks.append({
                    "name": result["itemLabel"]["value"],
                    "type": "city",
                    "latitude": float(coords[1]),
                    "longitude": float(coords[0]),
                    "elevation_m": None,
                    "country": result.get("countryLabel", {}).get("value"),
                    "wiki_id": result["item"]["value"].split("/")[-1],
                    "importance_score": 30.0 + (population / 100000.0)
                })
            except:
                continue

    return landmarks

def import_landmarks(db_url):
    """Import landmarks into database"""
    print("Querying Wikidata for landmarks...")
    landmarks = query_wikidata_landmarks()

    print(f"Found {len(landmarks)} landmarks")

    conn = psycopg2.connect(db_url)
    cur = conn.cursor()

    inserted = 0

    for landmark in landmarks:
        try:
            landmark_id = str(uuid.uuid4())

            cur.execute("""
                INSERT INTO landmarks (id, name, type, latitude, longitude, elevation_m, importance_score, wiki_id, country)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT DO NOTHING
            """, (
                landmark_id,
                landmark["name"],
                landmark["type"],
                landmark["latitude"],
                landmark["longitude"],
                landmark["elevation_m"],
                landmark["importance_score"],
                landmark["wiki_id"],
                landmark["country"]
            ))

            inserted += 1

        except Exception as e:
            print(f"Error importing {landmark['name']}: {e}")
            continue

    conn.commit()
    cur.close()
    conn.close()

    print(f"✓ Imported {inserted} landmarks")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python import_landmarks.py <database_url>")
        sys.exit(1)

    db_url = sys.argv[1]
    import_landmarks(db_url)
