#!/usr/bin/env python3
"""
Import airport data from OpenFlights.org into PostgreSQL database.
Download data from: https://raw.githubusercontent.com/jpatokal/openflights/master/data/airports.dat
"""

import csv
import psycopg2
import sys

def import_airports(csv_file, db_url):
    conn = psycopg2.connect(db_url)
    cur = conn.cursor()

    inserted = 0
    skipped = 0

    with open(csv_file, 'r', encoding='utf-8') as f:
        reader = csv.reader(f)

        for row in reader:
            try:
                airport_id = row[0]
                name = row[1]
                city = row[2]
                country = row[3]
                iata_code = row[4] if row[4] != '\\N' else None
                icao_code = row[5] if row[5] != '\\N' else None
                latitude = float(row[6])
                longitude = float(row[7])
                elevation = int(row[8]) if row[8] != '\\N' else None
                timezone = row[11] if row[11] != '\\N' else None

                # Skip airports without IATA code
                if not iata_code or len(iata_code) != 3:
                    skipped += 1
                    continue

                cur.execute("""
                    INSERT INTO airports (iata_code, icao_code, name, city, country, latitude, longitude, elevation_m, timezone)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                    ON CONFLICT (iata_code) DO UPDATE
                    SET name = EXCLUDED.name,
                        city = EXCLUDED.city,
                        country = EXCLUDED.country,
                        latitude = EXCLUDED.latitude,
                        longitude = EXCLUDED.longitude,
                        elevation_m = EXCLUDED.elevation_m,
                        timezone = EXCLUDED.timezone
                """, (iata_code, icao_code, name, city, country, latitude, longitude, elevation, timezone))

                inserted += 1

            except Exception as e:
                print(f"Error importing row: {e}")
                continue

    conn.commit()
    cur.close()
    conn.close()

    print(f"✓ Imported {inserted} airports")
    print(f"⊗ Skipped {skipped} airports (no IATA code)")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python import_airports.py <airports.dat> <database_url>")
        sys.exit(1)

    csv_file = sys.argv[1]
    db_url = sys.argv[2]

    import_airports(csv_file, db_url)
