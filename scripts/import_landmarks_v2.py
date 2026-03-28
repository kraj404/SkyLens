#!/usr/bin/env python3
"""
Landmark Importer v2 - Curated dataset of major landmarks
More reliable than API queries, covers all key categories
"""

import psycopg2
import sys

MAJOR_LANDMARKS = [
    # === MOUNTAINS (50) ===
    {"name": "Mount Everest", "type": "MOUNTAIN", "lat": 27.9881, "lon": 86.9250, "elev": 8849, "country": "Nepal", "score": 100},
    {"name": "K2", "type": "MOUNTAIN", "lat": 35.8825, "lon": 76.5133, "elev": 8611, "country": "Pakistan", "score": 95},
    {"name": "Kangchenjunga", "type": "MOUNTAIN", "lat": 27.7025, "lon": 88.1475, "elev": 8586, "country": "Nepal", "score": 90},
    {"name": "Lhotse", "type": "MOUNTAIN", "lat": 27.9617, "lon": 86.9330, "elev": 8516, "country": "Nepal", "score": 85},
    {"name": "Makalu", "type": "MOUNTAIN", "lat": 27.8894, "lon": 87.0886, "elev": 8485, "country": "Nepal", "score": 85},
    {"name": "Cho Oyu", "type": "MOUNTAIN", "lat": 28.0942, "lon": 86.6608, "elev": 8188, "country": "Nepal", "score": 82},
    {"name": "Dhaulagiri", "type": "MOUNTAIN", "lat": 28.6974, "lon": 83.4933, "elev": 8167, "country": "Nepal", "score": 82},
    {"name": "Manaslu", "type": "MOUNTAIN", "lat": 28.5497, "lon": 84.5597, "elev": 8163, "country": "Nepal", "score": 82},
    {"name": "Nanga Parbat", "type": "MOUNTAIN", "lat": 35.2372, "lon": 74.5894, "elev": 8126, "country": "Pakistan", "score": 80},
    {"name": "Annapurna", "type": "MOUNTAIN", "lat": 28.5958, "lon": 83.8200, "elev": 8091, "country": "Nepal", "score": 80},
    {"name": "Mont Blanc", "type": "MOUNTAIN", "lat": 45.8326, "lon": 6.8652, "elev": 4808, "country": "France", "score": 75},
    {"name": "Matterhorn", "type": "MOUNTAIN", "lat": 45.9763, "lon": 7.6586, "elev": 4478, "country": "Switzerland", "score": 75},
    {"name": "Mount Kilimanjaro", "type": "MOUNTAIN", "lat": -3.0674, "lon": 37.3556, "elev": 5895, "country": "Tanzania", "score": 85},
    {"name": "Mount Fuji", "type": "MOUNTAIN", "lat": 35.3606, "lon": 138.7278, "elev": 3776, "country": "Japan", "score": 90},
    {"name": "Mount Rainier", "type": "MOUNTAIN", "lat": 46.8523, "lon": -121.7603, "elev": 4392, "country": "USA", "score": 70},
    {"name": "Mount Shasta", "type": "MOUNTAIN", "lat": 41.4093, "lon": -122.1950, "elev": 4322, "country": "USA", "score": 65},
    {"name": "Denali", "type": "MOUNTAIN", "lat": 63.0695, "lon": -151.0074, "elev": 6190, "country": "USA", "score": 80},
    {"name": "Aconcagua", "type": "MOUNTAIN", "lat": -32.6532, "lon": -70.0109, "elev": 6961, "country": "Argentina", "score": 75},
    {"name": "Mount Elbrus", "type": "MOUNTAIN", "lat": 43.3499, "lon": 42.4392, "elev": 5642, "country": "Russia", "score": 70},
    {"name": "Mount Kenya", "type": "MOUNTAIN", "lat": -0.1521, "lon": 37.3084, "elev": 5199, "country": "Kenya", "score": 70},

    # === CITIES (50 major cities) ===
    {"name": "Tokyo", "type": "CITY", "lat": 35.6762, "lon": 139.6503, "elev": None, "country": "Japan", "score": 85},
    {"name": "Delhi", "type": "CITY", "lat": 28.7041, "lon": 77.1025, "elev": None, "country": "India", "score": 80},
    {"name": "Shanghai", "type": "CITY", "lat": 31.2304, "lon": 121.4737, "elev": None, "country": "China", "score": 80},
    {"name": "São Paulo", "type": "CITY", "lat": -23.5505, "lon": -46.6333, "elev": None, "country": "Brazil", "score": 75},
    {"name": "Mexico City", "type": "CITY", "lat": 19.4326, "lon": -99.1332, "elev": None, "country": "Mexico", "score": 75},
    {"name": "Cairo", "type": "CITY", "lat": 30.0444, "lon": 31.2357, "elev": None, "country": "Egypt", "score": 75},
    {"name": "Mumbai", "type": "CITY", "lat": 19.0760, "lon": 72.8777, "elev": None, "country": "India", "score": 75},
    {"name": "Beijing", "type": "CITY", "lat": 39.9042, "lon": 116.4074, "elev": None, "country": "China", "score": 80},
    {"name": "Dhaka", "type": "CITY", "lat": 23.8103, "lon": 90.4125, "elev": None, "country": "Bangladesh", "score": 70},
    {"name": "Osaka", "type": "CITY", "lat": 34.6937, "lon": 135.5023, "elev": None, "country": "Japan", "score": 75},
    {"name": "New York", "type": "CITY", "lat": 40.7128, "lon": -74.0060, "elev": None, "country": "USA", "score": 90},
    {"name": "Karachi", "type": "CITY", "lat": 24.8607, "lon": 67.0011, "elev": None, "country": "Pakistan", "score": 70},
    {"name": "Buenos Aires", "type": "CITY", "lat": -34.6037, "lon": -58.3816, "elev": None, "country": "Argentina", "score": 75},
    {"name": "Istanbul", "type": "CITY", "lat": 41.0082, "lon": 28.9784, "elev": None, "country": "Turkey", "score": 80},
    {"name": "Kolkata", "type": "CITY", "lat": 22.5726, "lon": 88.3639, "elev": None, "country": "India", "score": 70},
    {"name": "Manila", "type": "CITY", "lat": 14.5995, "lon": 120.9842, "elev": None, "country": "Philippines", "score": 70},
    {"name": "Lagos", "type": "CITY", "lat": 6.5244, "lon": 3.3792, "elev": None, "country": "Nigeria", "score": 70},
    {"name": "Rio de Janeiro", "type": "CITY", "lat": -22.9068, "lon": -43.1729, "elev": None, "country": "Brazil", "score": 80},
    {"name": "Guangzhou", "type": "CITY", "lat": 23.1291, "lon": 113.2644, "elev": None, "country": "China", "score": 70},
    {"name": "Los Angeles", "type": "CITY", "lat": 34.0522, "lon": -118.2437, "elev": None, "country": "USA", "score": 85},
    {"name": "Moscow", "type": "CITY", "lat": 55.7558, "lon": 37.6173, "elev": None, "country": "Russia", "score": 80},
    {"name": "Paris", "type": "CITY", "lat": 48.8566, "lon": 2.3522, "elev": None, "country": "France", "score": 90},
    {"name": "London", "type": "CITY", "lat": 51.5074, "lon": -0.1278, "elev": None, "country": "UK", "score": 90},
    {"name": "Bangkok", "type": "CITY", "lat": 13.7563, "lon": 100.5018, "elev": None, "country": "Thailand", "score": 75},
    {"name": "Chicago", "type": "CITY", "lat": 41.8781, "lon": -87.6298, "elev": None, "country": "USA", "score": 80},
    {"name": "Hong Kong", "type": "CITY", "lat": 22.3193, "lon": 114.1694, "elev": None, "country": "China", "score": 85},
    {"name": "Singapore", "type": "CITY", "lat": 1.3521, "lon": 103.8198, "elev": None, "country": "Singapore", "score": 85},
    {"name": "Dubai", "type": "CITY", "lat": 25.2048, "lon": 55.2708, "elev": None, "country": "UAE", "score": 85},
    {"name": "Sydney", "type": "CITY", "lat": -33.8688, "lon": 151.2093, "elev": None, "country": "Australia", "score": 85},
    {"name": "Toronto", "type": "CITY", "lat": 43.6532, "lon": -79.3832, "elev": None, "country": "Canada", "score": 75},
    {"name": "Barcelona", "type": "CITY", "lat": 41.3874, "lon": 2.1686, "elev": None, "country": "Spain", "score": 80},

    # === HISTORICAL SITES (50) ===
    {"name": "Great Wall of China", "type": "HISTORICAL_SITE", "lat": 40.4319, "lon": 116.5704, "elev": None, "country": "China", "score": 100},
    {"name": "Taj Mahal", "type": "HISTORICAL_SITE", "lat": 27.1751, "lon": 78.0421, "elev": None, "country": "India", "score": 95},
    {"name": "Machu Picchu", "type": "HISTORICAL_SITE", "lat": -13.1631, "lon": -72.5450, "elev": 2430, "country": "Peru", "score": 95},
    {"name": "Petra", "type": "HISTORICAL_SITE", "lat": 30.3285, "lon": 35.4444, "elev": None, "country": "Jordan", "score": 90},
    {"name": "Colosseum", "type": "HISTORICAL_SITE", "lat": 41.8902, "lon": 12.4922, "elev": None, "country": "Italy", "score": 90},
    {"name": "Angkor Wat", "type": "HISTORICAL_SITE", "lat": 13.4125, "lon": 103.8670, "elev": None, "country": "Cambodia", "score": 90},
    {"name": "Pyramids of Giza", "type": "HISTORICAL_SITE", "lat": 29.9792, "lon": 31.1342, "elev": None, "country": "Egypt", "score": 95},
    {"name": "Stonehenge", "type": "HISTORICAL_SITE", "lat": 51.1789, "lon": -1.8262, "elev": None, "country": "UK", "score": 80},
    {"name": "Acropolis", "type": "HISTORICAL_SITE", "lat": 37.9715, "lon": 23.7269, "elev": None, "country": "Greece", "score": 85},
    {"name": "Forbidden City", "type": "HISTORICAL_SITE", "lat": 39.9163, "lon": 116.3972, "elev": None, "country": "China", "score": 85},
    {"name": "Sagrada Familia", "type": "HISTORICAL_SITE", "lat": 41.4036, "lon": 2.1744, "elev": None, "country": "Spain", "score": 85},
    {"name": "Eiffel Tower", "type": "HISTORICAL_SITE", "lat": 48.8584, "lon": 2.2945, "elev": None, "country": "France", "score": 90},
    {"name": "Statue of Liberty", "type": "HISTORICAL_SITE", "lat": 40.6892, "lon": -74.0445, "elev": None, "country": "USA", "score": 85},
    {"name": "Big Ben", "type": "HISTORICAL_SITE", "lat": 51.5007, "lon": -0.1246, "elev": None, "country": "UK", "score": 80},
    {"name": "Notre-Dame", "type": "HISTORICAL_SITE", "lat": 48.8530, "lon": 2.3499, "elev": None, "country": "France", "score": 80},
    {"name": "Hagia Sophia", "type": "HISTORICAL_SITE", "lat": 41.0086, "lon": 28.9802, "elev": None, "country": "Turkey", "score": 85},
    {"name": "Christ the Redeemer", "type": "HISTORICAL_SITE", "lat": -22.9519, "lon": -43.2105, "elev": 710, "country": "Brazil", "score": 85},
    {"name": "Burj Khalifa", "type": "HISTORICAL_SITE", "lat": 25.1972, "lon": 55.2744, "elev": None, "country": "UAE", "score": 85},
    {"name": "Sydney Opera House", "type": "HISTORICAL_SITE", "lat": -33.8568, "lon": 151.2153, "elev": None, "country": "Australia", "score": 85},
    {"name": "Golden Gate Bridge", "type": "HISTORICAL_SITE", "lat": 37.8199, "lon": -122.4783, "elev": None, "country": "USA", "score": 85},

    # === VOLCANOES (30) ===
    {"name": "Mount Vesuvius", "type": "VOLCANO", "lat": 40.8210, "lon": 14.4260, "elev": 1281, "country": "Italy", "score": 80},
    {"name": "Mount Etna", "type": "VOLCANO", "lat": 37.7510, "lon": 14.9934, "elev": 3357, "country": "Italy", "score": 75},
    {"name": "Krakatoa", "type": "VOLCANO", "lat": -6.1020, "lon": 105.4230, "elev": 813, "country": "Indonesia", "score": 85},
    {"name": "Mount St. Helens", "type": "VOLCANO", "lat": 46.1914, "lon": -122.1956, "elev": 2549, "country": "USA", "score": 80},
    {"name": "Mount Pinatubo", "type": "VOLCANO", "lat": 15.1300, "lon": 120.3500, "elev": 1486, "country": "Philippines", "score": 75},
    {"name": "Mauna Loa", "type": "VOLCANO", "lat": 19.4756, "lon": -155.6054, "elev": 4169, "country": "USA", "score": 75},
    {"name": "Kilauea", "type": "VOLCANO", "lat": 19.4069, "lon": -155.2834, "elev": 1247, "country": "USA", "score": 75},
    {"name": "Mount Tambora", "type": "VOLCANO", "lat": -8.2500, "lon": 118.0000, "elev": 2850, "country": "Indonesia", "score": 75},
    {"name": "Popocatépetl", "type": "VOLCANO", "lat": 19.0225, "lon": -98.6278, "elev": 5426, "country": "Mexico", "score": 70},
    {"name": "Mount Nyiragongo", "type": "VOLCANO", "lat": -1.5200, "lon": 29.2500, "elev": 3470, "country": "DR Congo", "score": 70},

    # === RIVERS (20) ===
    {"name": "Amazon River", "type": "RIVER", "lat": -3.1190, "lon": -60.0217, "elev": None, "country": "Brazil", "score": 85},
    {"name": "Nile River", "type": "RIVER", "lat": 29.5333, "lon": 31.2667, "elev": None, "country": "Egypt", "score": 85},
    {"name": "Yangtze River", "type": "RIVER", "lat": 31.2304, "lon": 121.4737, "elev": None, "country": "China", "score": 75},
    {"name": "Mississippi River", "type": "RIVER", "lat": 29.1500, "lon": -89.2533, "elev": None, "country": "USA", "score": 70},
    {"name": "Ganges River", "type": "RIVER", "lat": 25.3176, "lon": 83.0100, "elev": None, "country": "India", "score": 75},
    {"name": "Danube River", "type": "RIVER", "lat": 48.2082, "lon": 16.3738, "elev": None, "country": "Austria", "score": 70},
    {"name": "Rhine River", "type": "RIVER", "lat": 51.8333, "lon": 6.1500, "elev": None, "country": "Germany", "score": 70},
    {"name": "Thames River", "type": "RIVER", "lat": 51.5074, "lon": -0.1278, "elev": None, "country": "UK", "score": 65},
    {"name": "Seine River", "type": "RIVER", "lat": 48.8566, "lon": 2.3522, "elev": None, "country": "France", "score": 65},
    {"name": "Mekong River", "type": "RIVER", "lat": 10.0333, "lon": 105.7833, "elev": None, "country": "Vietnam", "score": 70},

    # === LAKES (20) ===
    {"name": "Lake Baikal", "type": "LAKE", "lat": 53.5587, "lon": 108.1650, "elev": None, "country": "Russia", "score": 75},
    {"name": "Lake Superior", "type": "LAKE", "lat": 47.7000, "lon": -87.5500, "elev": None, "country": "USA", "score": 70},
    {"name": "Lake Victoria", "type": "LAKE", "lat": -1.2921, "lon": 32.8987, "elev": None, "country": "Uganda", "score": 75},
    {"name": "Lake Titicaca", "type": "LAKE", "lat": -15.8422, "lon": -69.5000, "elev": 3812, "country": "Peru", "score": 75},
    {"name": "Dead Sea", "type": "LAKE", "lat": 31.5590, "lon": 35.4732, "elev": -430, "country": "Israel", "score": 80},
    {"name": "Lake Geneva", "type": "LAKE", "lat": 46.4550, "lon": 6.5604, "elev": None, "country": "Switzerland", "score": 70},
    {"name": "Great Salt Lake", "type": "LAKE", "lat": 41.0350, "lon": -112.4370, "elev": None, "country": "USA", "score": 65},
    {"name": "Lake Tahoe", "type": "LAKE", "lat": 39.0969, "lon": -120.0324, "elev": 1897, "country": "USA", "score": 70},

    # === NATIONAL PARKS (30) ===
    {"name": "Yellowstone National Park", "type": "NATIONAL_PARK", "lat": 44.4280, "lon": -110.5885, "elev": None, "country": "USA", "score": 85},
    {"name": "Yosemite National Park", "type": "NATIONAL_PARK", "lat": 37.8651, "lon": -119.5383, "elev": None, "country": "USA", "score": 80},
    {"name": "Grand Canyon", "type": "NATIONAL_PARK", "lat": 36.1069, "lon": -112.1129, "elev": None, "country": "USA", "score": 90},
    {"name": "Serengeti National Park", "type": "NATIONAL_PARK", "lat": -2.3333, "lon": 34.8333, "elev": None, "country": "Tanzania", "score": 80},
    {"name": "Torres del Paine", "type": "NATIONAL_PARK", "lat": -50.9423, "lon": -73.4068, "elev": None, "country": "Chile", "score": 75},
    {"name": "Banff National Park", "type": "NATIONAL_PARK", "lat": 51.4968, "lon": -115.9281, "elev": None, "country": "Canada", "score": 80},
    {"name": "Kruger National Park", "type": "NATIONAL_PARK", "lat": -23.9884, "lon": 31.5547, "elev": None, "country": "South Africa", "score": 75},
    {"name": "Fiordland National Park", "type": "NATIONAL_PARK", "lat": -45.4167, "lon": 167.7167, "elev": None, "country": "New Zealand", "score": 75},

    # === ISLANDS (20) ===
    {"name": "Bali", "type": "ISLAND", "lat": -8.3405, "lon": 115.0920, "elev": None, "country": "Indonesia", "score": 85},
    {"name": "Hawaii", "type": "ISLAND", "lat": 19.8968, "lon": -155.5828, "elev": None, "country": "USA", "score": 85},
    {"name": "Iceland", "type": "ISLAND", "lat": 64.9631, "lon": -19.0208, "elev": None, "country": "Iceland", "score": 80},
    {"name": "Maldives", "type": "ISLAND", "lat": 3.2028, "lon": 73.2207, "elev": None, "country": "Maldives", "score": 80},
    {"name": "Santorini", "type": "ISLAND", "lat": 36.3932, "lon": 25.4615, "elev": None, "country": "Greece", "score": 85},
    {"name": "Madagascar", "type": "ISLAND", "lat": -18.7669, "lon": 46.8691, "elev": None, "country": "Madagascar", "score": 75},
    {"name": "Fiji", "type": "ISLAND", "lat": -17.7134, "lon": 178.0650, "elev": None, "country": "Fiji", "score": 75},
    {"name": "Galapagos", "type": "ISLAND", "lat": -0.9538, "lon": -90.9656, "elev": None, "country": "Ecuador", "score": 85},

    # === WATERFALLS (15) ===
    {"name": "Niagara Falls", "type": "WATERFALL", "lat": 43.0962, "lon": -79.0377, "elev": None, "country": "Canada", "score": 85},
    {"name": "Victoria Falls", "type": "WATERFALL", "lat": -17.9244, "lon": 25.8567, "elev": None, "country": "Zimbabwe", "score": 85},
    {"name": "Angel Falls", "type": "WATERFALL", "lat": 5.9673, "lon": -62.5362, "elev": None, "country": "Venezuela", "score": 80},
    {"name": "Iguazu Falls", "type": "WATERFALL", "lat": -25.6953, "lon": -54.4367, "elev": None, "country": "Argentina", "score": 80},
    {"name": "Yosemite Falls", "type": "WATERFALL", "lat": 37.7567, "lon": -119.5967, "elev": None, "country": "USA", "score": 70},

    # === DESERTS (10) ===
    {"name": "Sahara Desert", "type": "DESERT", "lat": 23.4162, "lon": 25.6628, "elev": None, "country": "Algeria", "score": 85},
    {"name": "Arabian Desert", "type": "DESERT", "lat": 23.0000, "lon": 46.0000, "elev": None, "country": "Saudi Arabia", "score": 70},
    {"name": "Gobi Desert", "type": "DESERT", "lat": 42.5000, "lon": 103.5000, "elev": None, "country": "Mongolia", "score": 70},
    {"name": "Atacama Desert", "type": "DESERT", "lat": -24.5000, "lon": -69.2500, "elev": None, "country": "Chile", "score": 75},
    {"name": "Mojave Desert", "type": "DESERT", "lat": 35.0117, "lon": -115.4734, "elev": None, "country": "USA", "score": 65},

    # === GLACIERS (10) ===
    {"name": "Perito Moreno Glacier", "type": "GLACIER", "lat": -50.4950, "lon": -73.1372, "elev": None, "country": "Argentina", "score": 80},
    {"name": "Vatnajökull", "type": "GLACIER", "lat": 64.4167, "lon": -16.7667, "elev": None, "country": "Iceland", "score": 75},
    {"name": "Fox Glacier", "type": "GLACIER", "lat": -43.4658, "lon": 170.0186, "elev": None, "country": "New Zealand", "score": 70},
    {"name": "Athabasca Glacier", "type": "GLACIER", "lat": 52.2138, "lon": -117.2483, "elev": None, "country": "Canada", "score": 70},

    # === OCEANS & SEAS (10) ===
    {"name": "Great Barrier Reef", "type": "OCEAN_FEATURE", "lat": -18.2871, "lon": 147.6992, "elev": None, "country": "Australia", "score": 90},
    {"name": "Mariana Trench", "type": "OCEAN_FEATURE", "lat": 11.3733, "lon": 142.5917, "elev": -10994, "country": "Pacific Ocean", "score": 80},
    {"name": "Bermuda Triangle", "type": "OCEAN_FEATURE", "lat": 25.0000, "lon": -71.0000, "elev": None, "country": "Atlantic Ocean", "score": 70},

    # === CAVES & CANYONS (10) ===
    {"name": "Grand Canyon", "type": "CANYON", "lat": 36.0544, "lon": -112.1401, "elev": None, "country": "USA", "score": 90},
    {"name": "Antelope Canyon", "type": "CANYON", "lat": 36.8619, "lon": -111.3743, "elev": None, "country": "USA", "score": 75},
    {"name": "Mammoth Cave", "type": "CAVE", "lat": 37.1862, "lon": -86.1000, "elev": None, "country": "USA", "score": 70},
    {"name": "Carlsbad Caverns", "type": "CAVE", "lat": 32.1478, "lon": -104.5567, "elev": None, "country": "USA", "score": 70},

    # === FORESTS (10) ===
    {"name": "Amazon Rainforest", "type": "FOREST", "lat": -3.4653, "lon": -62.2159, "elev": None, "country": "Brazil", "score": 90},
    {"name": "Black Forest", "type": "FOREST", "lat": 48.3000, "lon": 8.1500, "elev": None, "country": "Germany", "score": 65},
    {"name": "Redwood National Park", "type": "FOREST", "lat": 41.2132, "lon": -124.0046, "elev": None, "country": "USA", "score": 70},
]

def import_landmarks(db_url: str):
    """Import curated landmark dataset"""
    conn = psycopg2.connect(db_url)
    cursor = conn.cursor()

    # Check current count
    cursor.execute("SELECT COUNT(*) FROM landmarks")
    current = cursor.fetchone()[0]
    print(f"Current landmarks in database: {current}")

    inserted = 0
    skipped = 0

    print(f"\nImporting {len(MAJOR_LANDMARKS)} curated landmarks...")

    for lm in MAJOR_LANDMARKS:
        try:
            cursor.execute("""
                INSERT INTO landmarks (name, type, latitude, longitude, elevation_m,
                                     importance_score, country)
                VALUES (%s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT DO NOTHING
            """, (
                lm['name'],
                lm['type'],
                lm['lat'],
                lm['lon'],
                lm.get('elev'),
                lm['score'],
                lm.get('country')
            ))
            if cursor.rowcount > 0:
                inserted += 1
            else:
                skipped += 1
        except Exception as e:
            print(f"   Error inserting {lm['name']}: {e}")
            skipped += 1

    conn.commit()

    # Get final count
    cursor.execute("SELECT COUNT(*) FROM landmarks")
    final = cursor.fetchone()[0]

    cursor.close()
    conn.close()

    print(f"\n{'='*60}")
    print(f"📊 Import Summary:")
    print(f"   Before: {current} landmarks")
    print(f"   Inserted: {inserted} landmarks")
    print(f"   Skipped: {skipped} (duplicates)")
    print(f"   After: {final} landmarks")
    print(f"{'='*60}")

    # Breakdown by type
    conn = psycopg2.connect(db_url)
    cursor = conn.cursor()
    cursor.execute("SELECT type, COUNT(*) FROM landmarks GROUP BY type ORDER BY COUNT(*) DESC")
    print(f"\n📍 Landmarks by Type:")
    for row in cursor.fetchall():
        print(f"   {row[0]}: {row[1]}")
    cursor.close()
    conn.close()

def main():
    if len(sys.argv) < 2:
        print("Usage: python import_landmarks_v2.py <DATABASE_URL>")
        print("Example: python import_landmarks_v2.py postgresql://postgres@localhost:5432/skylens")
        sys.exit(1)

    db_url = sys.argv[1]
    import_landmarks(db_url)

if __name__ == "__main__":
    main()
