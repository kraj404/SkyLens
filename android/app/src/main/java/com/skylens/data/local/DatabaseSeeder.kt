package com.skylens.data.local

import android.content.Context
import com.skylens.data.local.dao.AirportDao
import com.skylens.data.local.dao.LandmarkDao
import com.skylens.data.local.entities.AirportEntity
import com.skylens.data.local.entities.LandmarkEntity
import com.skylens.data.repository.AirportRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds the database with sample airport and landmark data for testing
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val airportDao: AirportDao,
    private val landmarkDao: LandmarkDao
) {

    suspend fun seedAirportsIfEmpty() = withContext(Dispatchers.IO) {
        android.util.Log.d("DatabaseSeeder", "Checking if airports need seeding...")
        val count = airportDao.getAirportCount()
        android.util.Log.d("DatabaseSeeder", "Current airport count: $count")
        if (count == 0) {
            val airports = getSampleAirports()
            android.util.Log.d("DatabaseSeeder", "Inserting ${airports.size} sample airports...")
            airportDao.insertAllAirports(airports)
            android.util.Log.d("DatabaseSeeder", "Airports inserted successfully")
        } else {
            android.util.Log.d("DatabaseSeeder", "Airports already seeded, skipping")
        }
    }

    suspend fun seedLandmarksIfEmpty() = withContext(Dispatchers.IO) {
        android.util.Log.d("DatabaseSeeder", "Checking if landmarks need seeding...")
        val count = landmarkDao.getLandmarkCount()
        android.util.Log.d("DatabaseSeeder", "Current landmark count: $count")
        if (count == 0) {
            val landmarks = getSampleLandmarks()
            android.util.Log.d("DatabaseSeeder", "Inserting ${landmarks.size} sample landmarks...")
            landmarkDao.insertAllLandmarks(landmarks)
            android.util.Log.d("DatabaseSeeder", "Landmarks inserted successfully")
        } else {
            android.util.Log.d("DatabaseSeeder", "Landmarks already seeded, skipping")
        }
    }

    private fun getSampleAirports(): List<AirportEntity> = listOf(
        // United States
        AirportEntity(
            iataCode = "DTW",
            icaoCode = "KDTW",
            name = "Detroit Metropolitan Wayne County Airport",
            city = "Detroit",
            country = "United States",
            latitude = 42.2162,
            longitude = -83.3554,
            elevationM = 197,
            timezone = "America/Detroit"
        ),
        AirportEntity(
            iataCode = "LAX",
            icaoCode = "KLAX",
            name = "Los Angeles International Airport",
            city = "Los Angeles",
            country = "United States",
            latitude = 33.9416,
            longitude = -118.4085,
            elevationM = 38,
            timezone = "America/Los_Angeles"
        ),
        AirportEntity(
            iataCode = "JFK",
            icaoCode = "KJFK",
            name = "John F. Kennedy International Airport",
            city = "New York",
            country = "United States",
            latitude = 40.6413,
            longitude = -73.7781,
            elevationM = 4,
            timezone = "America/New_York"
        ),
        AirportEntity(
            iataCode = "SFO",
            icaoCode = "KSFO",
            name = "San Francisco International Airport",
            city = "San Francisco",
            country = "United States",
            latitude = 37.6213,
            longitude = -122.3790,
            elevationM = 4,
            timezone = "America/Los_Angeles"
        ),
        AirportEntity(
            iataCode = "ORD",
            icaoCode = "KORD",
            name = "O'Hare International Airport",
            city = "Chicago",
            country = "United States",
            latitude = 41.9742,
            longitude = -87.9073,
            elevationM = 205,
            timezone = "America/Chicago"
        ),
        AirportEntity(
            iataCode = "MIA",
            icaoCode = "KMIA",
            name = "Miami International Airport",
            city = "Miami",
            country = "United States",
            latitude = 25.7959,
            longitude = -80.2870,
            elevationM = 3,
            timezone = "America/New_York"
        ),
        AirportEntity(
            iataCode = "SEA",
            icaoCode = "KSEA",
            name = "Seattle-Tacoma International Airport",
            city = "Seattle",
            country = "United States",
            latitude = 47.4502,
            longitude = -122.3088,
            elevationM = 132,
            timezone = "America/Los_Angeles"
        ),

        // Europe
        AirportEntity(
            iataCode = "LHR",
            icaoCode = "EGLL",
            name = "Heathrow Airport",
            city = "London",
            country = "United Kingdom",
            latitude = 51.4700,
            longitude = -0.4543,
            elevationM = 25,
            timezone = "Europe/London"
        ),
        AirportEntity(
            iataCode = "CDG",
            icaoCode = "LFPG",
            name = "Charles de Gaulle Airport",
            city = "Paris",
            country = "France",
            latitude = 49.0097,
            longitude = 2.5479,
            elevationM = 119,
            timezone = "Europe/Paris"
        ),
        AirportEntity(
            iataCode = "FRA",
            icaoCode = "EDDF",
            name = "Frankfurt Airport",
            city = "Frankfurt",
            country = "Germany",
            latitude = 50.0379,
            longitude = 8.5622,
            elevationM = 111,
            timezone = "Europe/Berlin"
        ),
        AirportEntity(
            iataCode = "AMS",
            icaoCode = "EHAM",
            name = "Amsterdam Airport Schiphol",
            city = "Amsterdam",
            country = "Netherlands",
            latitude = 52.3105,
            longitude = 4.7683,
            elevationM = -3,
            timezone = "Europe/Amsterdam"
        ),

        // Asia
        AirportEntity(
            iataCode = "BLR",
            icaoCode = "VOBL",
            name = "Kempegowda International Airport",
            city = "Bangalore",
            country = "India",
            latitude = 13.1979,
            longitude = 77.7063,
            elevationM = 915,
            timezone = "Asia/Kolkata"
        ),
        AirportEntity(
            iataCode = "NRT",
            icaoCode = "RJAA",
            name = "Tokyo Narita International Airport",
            city = "Tokyo",
            country = "Japan",
            latitude = 35.7647,
            longitude = 140.3864,
            elevationM = 43,
            timezone = "Asia/Tokyo"
        ),
        AirportEntity(
            iataCode = "HND",
            icaoCode = "RJTT",
            name = "Tokyo Haneda Airport",
            city = "Tokyo",
            country = "Japan",
            latitude = 35.5494,
            longitude = 139.7798,
            elevationM = 11,
            timezone = "Asia/Tokyo"
        ),
        AirportEntity(
            iataCode = "ICN",
            icaoCode = "RKSI",
            name = "Incheon International Airport",
            city = "Seoul",
            country = "South Korea",
            latitude = 37.4602,
            longitude = 126.4407,
            elevationM = 7,
            timezone = "Asia/Seoul"
        ),
        AirportEntity(
            iataCode = "SIN",
            icaoCode = "WSSS",
            name = "Singapore Changi Airport",
            city = "Singapore",
            country = "Singapore",
            latitude = 1.3644,
            longitude = 103.9915,
            elevationM = 7,
            timezone = "Asia/Singapore"
        ),
        AirportEntity(
            iataCode = "HKG",
            icaoCode = "VHHH",
            name = "Hong Kong International Airport",
            city = "Hong Kong",
            country = "Hong Kong",
            latitude = 22.3080,
            longitude = 113.9185,
            elevationM = 9,
            timezone = "Asia/Hong_Kong"
        ),
        AirportEntity(
            iataCode = "DXB",
            icaoCode = "OMDB",
            name = "Dubai International Airport",
            city = "Dubai",
            country = "United Arab Emirates",
            latitude = 25.2532,
            longitude = 55.3657,
            elevationM = 19,
            timezone = "Asia/Dubai"
        ),

        // Oceania
        AirportEntity(
            iataCode = "SYD",
            icaoCode = "YSSY",
            name = "Sydney Kingsford Smith Airport",
            city = "Sydney",
            country = "Australia",
            latitude = -33.9399,
            longitude = 151.1753,
            elevationM = 6,
            timezone = "Australia/Sydney"
        ),
        AirportEntity(
            iataCode = "AKL",
            icaoCode = "NZAA",
            name = "Auckland Airport",
            city = "Auckland",
            country = "New Zealand",
            latitude = -37.0082,
            longitude = 174.7850,
            elevationM = 7,
            timezone = "Pacific/Auckland"
        ),

        // South America
        AirportEntity(
            iataCode = "GRU",
            icaoCode = "SBGR",
            name = "São Paulo/Guarulhos International Airport",
            city = "São Paulo",
            country = "Brazil",
            latitude = -23.4356,
            longitude = -46.4731,
            elevationM = 750,
            timezone = "America/Sao_Paulo"
        ),
        AirportEntity(
            iataCode = "EZE",
            icaoCode = "SAEZ",
            name = "Ministro Pistarini International Airport",
            city = "Buenos Aires",
            country = "Argentina",
            latitude = -34.8222,
            longitude = -58.5358,
            elevationM = 20,
            timezone = "America/Argentina/Buenos_Aires"
        ),

        // Africa
        AirportEntity(
            iataCode = "JNB",
            icaoCode = "FAJS",
            name = "O. R. Tambo International Airport",
            city = "Johannesburg",
            country = "South Africa",
            latitude = -26.1392,
            longitude = 28.2460,
            elevationM = 1694,
            timezone = "Africa/Johannesburg"
        ),
        AirportEntity(
            iataCode = "CAI",
            icaoCode = "HECA",
            name = "Cairo International Airport",
            city = "Cairo",
            country = "Egypt",
            latitude = 30.1219,
            longitude = 31.4056,
            elevationM = 116,
            timezone = "Africa/Cairo"
        )
    )

    private fun getSampleLandmarks(): List<LandmarkEntity> = listOf(
        // North America - Near DTW
        LandmarkEntity(
            id = "lm_detroit",
            name = "Detroit River",
            type = "RIVER",
            latitude = 42.3314,
            longitude = -83.0458,
            elevationM = 174,
            importanceScore = 40f,
            wikiId = null,
            country = "United States",
            aiStory = "The Detroit River connects Lake St. Clair with Lake Erie, forming part of the U.S.-Canada border. Historic trade route and industrial waterway.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),
        LandmarkEntity(
            id = "lm_lake_erie",
            name = "Lake Erie",
            type = "LAKE",
            latitude = 42.2,
            longitude = -81.2,
            elevationM = 174,
            importanceScore = 70f,
            wikiId = null,
            country = "United States/Canada",
            aiStory = "Fourth-largest Great Lake by surface area. Historic shipping routes and freshwater ecosystem.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),

        // Atlantic Crossing
        LandmarkEntity(
            id = "lm_atlantic_ocean",
            name = "Atlantic Ocean",
            type = "NATURAL_WONDER",
            latitude = 50.0,
            longitude = -25.0,
            elevationM = 0,
            importanceScore = 95f,
            wikiId = null,
            country = "International Waters",
            aiStory = "Second-largest ocean on Earth, covering approximately 20% of Earth's surface. Major shipping and aviation routes.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),

        // British Isles
        LandmarkEntity(
            id = "lm_london",
            name = "London",
            type = "CITY",
            latitude = 51.5074,
            longitude = -0.1278,
            elevationM = 11,
            importanceScore = 100f,
            wikiId = null,
            country = "United Kingdom",
            aiStory = "Capital of the United Kingdom. Historic global center of finance, culture, and politics. Population over 9 million.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),

        // Low Countries
        LandmarkEntity(
            id = "lm_amsterdam",
            name = "Amsterdam",
            type = "CITY",
            latitude = 52.3676,
            longitude = 4.9041,
            elevationM = -2,
            importanceScore = 85f,
            wikiId = null,
            country = "Netherlands",
            aiStory = "Capital of Netherlands, known for its artistic heritage, elaborate canal system, and narrow houses.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),

        // Rhine Valley - Near FRA
        LandmarkEntity(
            id = "lm_rhine_river",
            name = "Rhine River",
            type = "RIVER",
            latitude = 50.9,
            longitude = 7.0,
            elevationM = 50,
            importanceScore = 90f,
            wikiId = null,
            country = "Germany",
            aiStory = "One of Europe's longest rivers, flowing through six countries. Vital commercial waterway and UNESCO World Heritage Site.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),
        LandmarkEntity(
            id = "lm_frankfurt_skyline",
            name = "Frankfurt Skyline",
            type = "CITY",
            latitude = 50.1109,
            longitude = 8.6821,
            elevationM = 112,
            importanceScore = 75f,
            wikiId = null,
            country = "Germany",
            aiStory = "Financial hub of Germany with distinctive skyscraper skyline. Home to European Central Bank and Frankfurt Stock Exchange.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),

        // Alps
        LandmarkEntity(
            id = "lm_swiss_alps",
            name = "Swiss Alps",
            type = "MOUNTAIN",
            latitude = 46.5,
            longitude = 8.5,
            elevationM = 4000,
            importanceScore = 100f,
            wikiId = null,
            country = "Switzerland",
            aiStory = "Iconic mountain range featuring glaciers, dramatic peaks, and Alpine villages. Home to Matterhorn and Jungfrau.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),
        LandmarkEntity(
            id = "lm_matterhorn",
            name = "Matterhorn",
            type = "MOUNTAIN",
            latitude = 45.9763,
            longitude = 7.6586,
            elevationM = 4478,
            importanceScore = 95f,
            wikiId = null,
            country = "Switzerland/Italy",
            aiStory = "One of the world's most recognizable mountains with its pyramid shape. Straddles Swiss-Italian border.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),

        // Northern Italy
        LandmarkEntity(
            id = "lm_milan",
            name = "Milan",
            type = "CITY",
            latitude = 45.4642,
            longitude = 9.19,
            elevationM = 120,
            importanceScore = 85f,
            wikiId = null,
            country = "Italy",
            aiStory = "Italy's financial and fashion capital. Home to iconic Milan Cathedral and Da Vinci's Last Supper.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),
        LandmarkEntity(
            id = "lm_venice",
            name = "Venice",
            type = "CITY",
            latitude = 45.4408,
            longitude = 12.3155,
            elevationM = 1,
            importanceScore = 95f,
            wikiId = null,
            country = "Italy",
            aiStory = "Unique city built on 118 islands connected by canals. UNESCO World Heritage Site known for gondolas and architecture.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),

        // Adriatic and Balkans
        LandmarkEntity(
            id = "lm_adriatic_sea",
            name = "Adriatic Sea",
            type = "NATURAL_WONDER",
            latitude = 43.0,
            longitude = 16.0,
            elevationM = 0,
            importanceScore = 70f,
            wikiId = null,
            country = "International Waters",
            aiStory = "Body of water separating Italian Peninsula from Balkans. Popular tourist destination with historic coastal cities.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),

        // Eastern Mediterranean
        LandmarkEntity(
            id = "lm_istanbul",
            name = "Istanbul",
            type = "CITY",
            latitude = 41.0082,
            longitude = 28.9784,
            elevationM = 39,
            importanceScore = 95f,
            wikiId = null,
            country = "Turkey",
            aiStory = "Historic city straddling Europe and Asia across the Bosphorus. Former capital of Byzantine and Ottoman Empires.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),

        // Middle East
        LandmarkEntity(
            id = "lm_persian_gulf",
            name = "Persian Gulf",
            type = "NATURAL_WONDER",
            latitude = 26.0,
            longitude = 52.0,
            elevationM = 0,
            importanceScore = 80f,
            wikiId = null,
            country = "International Waters",
            aiStory = "Strategic waterway surrounded by oil-rich nations. Critical shipping route for global energy markets.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),
        LandmarkEntity(
            id = "lm_dubai_skyline",
            name = "Dubai Skyline",
            type = "CITY",
            latitude = 25.2048,
            longitude = 55.2708,
            elevationM = 5,
            importanceScore = 90f,
            wikiId = null,
            country = "United Arab Emirates",
            aiStory = "Modern metropolis featuring Burj Khalifa, world's tallest building. Transformed from desert to global business hub.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),

        // Arabian Sea
        LandmarkEntity(
            id = "lm_arabian_sea",
            name = "Arabian Sea",
            type = "NATURAL_WONDER",
            latitude = 15.0,
            longitude = 65.0,
            elevationM = 0,
            importanceScore = 70f,
            wikiId = null,
            country = "International Waters",
            aiStory = "Northwestern region of Indian Ocean. Historic trade routes connecting Middle East with India.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),

        // Western India
        LandmarkEntity(
            id = "lm_mumbai",
            name = "Mumbai",
            type = "CITY",
            latitude = 19.0760,
            longitude = 72.8777,
            elevationM = 14,
            importanceScore = 95f,
            wikiId = null,
            country = "India",
            aiStory = "India's financial capital and largest city. Bollywood film industry center. Population over 20 million.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),

        // Western Ghats
        LandmarkEntity(
            id = "lm_western_ghats",
            name = "Western Ghats",
            type = "MOUNTAIN",
            latitude = 13.0,
            longitude = 76.0,
            elevationM = 1500,
            importanceScore = 85f,
            wikiId = null,
            country = "India",
            aiStory = "UNESCO World Heritage mountain range with high biodiversity. Creates rain shadow effect affecting monsoon patterns.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        ),

        // Karnataka - Near BLR
        LandmarkEntity(
            id = "lm_bangalore",
            name = "Bangalore",
            type = "CITY",
            latitude = 12.9716,
            longitude = 77.5946,
            elevationM = 920,
            importanceScore = 90f,
            wikiId = null,
            country = "India",
            aiStory = "India's Silicon Valley and IT capital. Known as the Garden City with pleasant year-round climate. Population over 12 million.",
            photoUrls = """["https://picsum.photos/800/600?random=1", "https://picsum.photos/800/600?random=2"]"""
        )
    )
}
