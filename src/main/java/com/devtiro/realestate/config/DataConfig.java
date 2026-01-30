
package com.devtiro.realestate.config;

import com.devtiro.realestate.domain.dto.AuthResponse;
import com.devtiro.realestate.domain.dto.RegisterRequest;
import com.devtiro.realestate.domain.entities.*;
import com.devtiro.realestate.repositories.PropertyListingRepository;
import com.devtiro.realestate.repositories.UserRepository;
import com.devtiro.realestate.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataConfig {

    @Value("${app.agent.email}")
    private String agentEmail;

    @Value("${app.user.email}")
    private String userEmail;

    private final UserRepository userRepository;
    private final PropertyListingRepository propertyListingRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final CacheManager cacheManager;

    @Bean
    public CommandLineRunner loadSampleData() {
        return args -> {
            log.info("=== Loading sample data ===");

            // Clear existing data
            userRepository.findAll()
                    .forEach(this::deleteUser);
            propertyListingRepository.deleteAll();

            log.info("Cleared existing data");


            log.info("Emails: {}, {}", agentEmail, userEmail);
            // Create sample agents
            User agent1 = createAgent(agentEmail, "John", "Smith");
            User agent2 = createAgent("agent2@realestate.com", "Sarah", "Johnson");
            User agent3 = createAgent("agent3@realestate.com", "Michael", "Brown");

            log.info("Created {} sample agents", 3);

            // Create sample home seekers
            createHomeSeeker(userEmail, "Emily", "Davis", "123123123");
            createHomeSeeker("seeker2@email.com", "David", "Wilson", "0404040");

            log.info("Created {} sample home seekers", 2);

            // Create sample property listings
            List<PropertyListing> properties = new ArrayList<>();

            // San Francisco Properties
            properties.add(createProperty(agent1.getId(), agent1.getEmail(), agent1.getFirstName(), agent1.getLastName(),
                    "Stunning Victorian Home in Pacific Heights",
                    "Beautiful 4-bedroom Victorian with original details, hardwood floors, and bay views",
                    PropertyType.HOUSE, "2156 Pacific Ave", "San Francisco", "CA", "94115",
                    "Pacific Heights", 37.7929, -122.4364, 4, 3, new BigDecimal("2800"),
                    1895, new BigDecimal("3250000"), true, 2, false, true,
                    Arrays.asList("Bay Views", "Original Details", "Hardwood Floors", "Updated Kitchen")));

            properties.add(createProperty(agent2.getId(), agent2.getEmail(), agent2.getFirstName(), agent2.getLastName(),
                    "Modern Luxury Condo with Bay Bridge Views",
                    "Sleek 2-bedroom condo in SOMA with floor-to-ceiling windows and luxury amenities",
                    PropertyType.CONDO, "300 Beale St", "San Francisco", "CA", "94105",
                    "South Beach", 37.7906, -122.3927, 2, 2, new BigDecimal("1400"),
                    2018, new BigDecimal("1450000"), true, 1, false, false,
                    Arrays.asList("Floor-to-ceiling Windows", "Gym", "Concierge", "Rooftop Deck")));

            properties.add(createProperty(agent1.getId(), agent1.getEmail(), agent1.getFirstName(), agent1.getLastName(),"Charming Cottage in Noe Valley",
                    "Cozy 3-bedroom cottage with private garden, perfect for families",
                    PropertyType.HOUSE, "845 Noe St", "San Francisco", "CA", "94114",
                    "Noe Valley", 37.7503, -122.4330, 3, 2, new BigDecimal("1800"),
                    1925, new BigDecimal("1850000"), true, 1, false, true,
                    Arrays.asList("Private Garden", "Updated Kitchen", "Hardwood Floors", "Near Parks")));

            properties.add(createProperty(agent3.getId(), agent3.getEmail(), agent3.getFirstName(), agent3.getLastName(),"Spacious Family Home in Sunset District",
                    "4-bedroom home with large backyard, perfect for growing families",
                    PropertyType.HOUSE, "2345 44th Ave", "San Francisco", "CA", "94116",
                    "Outer Sunset", 37.7474, -122.5060, 4, 2, new BigDecimal("2200"),
                    1965, new BigDecimal("1650000"), true, 2, false, true,
                    Arrays.asList("Large Backyard", "Near Ocean", "Updated Bathrooms", "Fireplace")));

            properties.add(createProperty(agent2.getId(), agent2.getEmail(), agent2.getFirstName(), agent2.getLastName(),"Mission District Loft",
                    "Industrial-chic 2-bedroom loft with exposed brick and high ceilings",
                    PropertyType.APARTMENT, "555 Valencia St", "San Francisco", "CA", "94110",
                    "Mission District", 37.7617, -122.4216, 2, 1, new BigDecimal("1200"),
                    2010, new BigDecimal("925000"), false, 0, false, false,
                    Arrays.asList("Exposed Brick", "High Ceilings", "Walk Score 98", "Near BART")));

            // Oakland Properties
            properties.add(createProperty(agent1.getId(), agent1.getEmail(), agent1.getFirstName(), agent1.getLastName(),"Modern Oakland Hills Home with Views",
                    "Stunning 3-bedroom contemporary home with panoramic bay views",
                    PropertyType.HOUSE, "5678 Skyline Blvd", "Oakland", "CA", "94619",
                    "Oakland Hills", 37.8044, -122.1742, 3, 2, new BigDecimal("2500"),
                    2015, new BigDecimal("1250000"), true, 2, true, true,
                    Arrays.asList("Panoramic Views", "Pool", "Modern Design", "Smart Home")));

            properties.add(createProperty(agent3.getId(), agent3.getEmail(), agent3.getFirstName(), agent3.getLastName(),"Charming Rockridge Bungalow",
                    "Updated 2-bedroom bungalow in desirable Rockridge neighborhood",
                    PropertyType.HOUSE, "345 Ocean View Dr", "Oakland", "CA", "94618",
                    "Rockridge", 37.8424, -122.2517, 2, 1, new BigDecimal("1400"),
                    1920, new BigDecimal("875000"), true, 1, false, true,
                    Arrays.asList("Updated Kitchen", "Hardwood Floors", "Walk to Shops", "Quiet Street")));

            properties.add(createProperty(agent2.getId(), agent2.getEmail(), agent2.getFirstName(), agent2.getLastName(),"Lake Merritt Luxury Apartment",
                    "Beautiful 1-bedroom with lake views and modern finishes",
                    PropertyType.APARTMENT, "1200 Lakeshore Ave", "Oakland", "CA", "94606",
                    "Lake Merritt", 37.8044, -122.2603, 1, 1, new BigDecimal("850"),
                    2019, new BigDecimal("525000"), false, 1, false, false,
                    Arrays.asList("Lake Views", "In-unit Laundry", "Gym", "Secure Parking")));

            // Berkeley Properties
            properties.add(createProperty(agent1.getId(), agent1.getEmail(), agent1.getFirstName(), agent1.getLastName(),"Berkeley Hills Retreat",
                    "Peaceful 4-bedroom home nestled in the hills with bay views",
                    PropertyType.HOUSE, "789 Grizzly Peak Blvd", "Berkeley", "CA", "94708",
                    "Berkeley Hills", 37.8885, -122.2508, 4, 3, new BigDecimal("3000"),
                    1975, new BigDecimal("1950000"), true, 2, false, true,
                    Arrays.asList("Bay Views", "Hiking Trails", "Updated Kitchen", "Deck")));

            properties.add(createProperty(agent3.getId(), agent3.getEmail(), agent3.getFirstName(), agent3.getLastName(),"Downtown Berkeley Condo",
                    "Walk to campus! Modern 2-bedroom condo in the heart of downtown",
                    PropertyType.CONDO, "2100 Durant Ave", "Berkeley", "CA", "94704",
                    "Downtown Berkeley", 37.8688, -122.2588, 2, 2, new BigDecimal("1100"),
                    2016, new BigDecimal("775000"), false, 1, false, false,
                    Arrays.asList("Walk to UC Berkeley", "Rooftop Garden", "Modern Appliances", "Bike Storage")));

            // San Jose Properties
            properties.add(createProperty(agent2.getId(), agent2.getEmail(), agent2.getFirstName(), agent2.getLastName(),"San Jose Tech Professional's Dream",
                    "Contemporary 3-bedroom townhouse near tech campuses",
                    PropertyType.TOWNHOUSE, "456 Innovation Way", "San Jose", "CA", "95134",
                    "North San Jose", 37.4087, -121.9406, 3, 2, new BigDecimal("1850"),
                    2020, new BigDecimal("1050000"), true, 2, false, true,
                    Arrays.asList("Near Tech Campuses", "Modern Design", "Smart Home", "Community Pool")));

            properties.add(createProperty(agent1.getId(), agent1.getEmail(), agent1.getFirstName(), agent1.getLastName(),"Willow Glen Charmer",
                    "Classic 4-bedroom home in sought-after Willow Glen neighborhood",
                    PropertyType.HOUSE, "1234 Lincoln Ave", "San Jose", "CA", "95125",
                    "Willow Glen", 37.3015, -121.8939, 4, 2, new BigDecimal("2400"),
                    1955, new BigDecimal("1750000"), true, 2, true, true,
                    Arrays.asList("Pool", "Updated Interior", "Large Lot", "Near Downtown")));

            properties.add(createProperty(agent3.getId(), agent3.getEmail(), agent3.getFirstName(), agent3.getLastName(),"Downtown San Jose High-Rise",
                    "Luxury 2-bedroom in the heart of downtown with city views",
                    PropertyType.CONDO, "88 E San Fernando St", "San Jose", "CA", "95113",
                    "Downtown", 37.3337, -121.8887, 2, 2, new BigDecimal("1300"),
                    2021, new BigDecimal("895000"), false, 2, false, false,
                    Arrays.asList("City Views", "Concierge", "Gym", "Walk Score 95")));

            // Palo Alto Properties
            properties.add(createProperty(agent2.getId(), agent2.getEmail(), agent2.getFirstName(), agent2.getLastName(),"Palo Alto Family Estate",
                    "Elegant 5-bedroom estate in Old Palo Alto with guest house",
                    PropertyType.HOUSE, "567 Embarcadero Rd", "Palo Alto", "CA", "94301",
                    "Old Palo Alto", 37.4489, -122.1600, 5, 4, new BigDecimal("4500"),
                    1985, new BigDecimal("5250000"), true, 3, true, true,
                    Arrays.asList("Guest House", "Pool", "Wine Cellar", "Top Schools")));

            properties.add(createProperty(agent1.getId(), agent1.getEmail(), agent1.getFirstName(), agent1.getLastName(),"Stanford Area Condo",
                    "Modern 2-bedroom condo near Stanford University",
                    PropertyType.CONDO, "789 El Camino Real", "Palo Alto", "CA", "94306",
                    "Stanford", 37.4275, -122.1697, 2, 2, new BigDecimal("1200"),
                    2017, new BigDecimal("1150000"), false, 1, false, false,
                    Arrays.asList("Near Stanford", "Modern Kitchen", "Balcony", "Secure Building")));

            // Affordable Starter Homes
            properties.add(createProperty(agent3.getId(), agent3.getEmail(), agent3.getFirstName(), agent3.getLastName(),"Affordable Fremont Starter Home",
                    "Perfect 2-bedroom starter home in family-friendly neighborhood",
                    PropertyType.HOUSE, "456 Maple St", "Fremont", "CA", "94536",
                    "Centerville", 37.5541, -121.9860, 2, 1, new BigDecimal("1100"),
                    1960, new BigDecimal("650000"), true, 1, false, true,
                    Arrays.asList("Updated Kitchen", "Fenced Yard", "Near Schools", "Quiet Street")));

            properties.add(createProperty(agent2.getId(), agent2.getEmail(), agent2.getFirstName(), agent2.getLastName(),"Hayward Budget-Friendly Home",
                    "Great 3-bedroom home with investment potential",
                    PropertyType.HOUSE, "789 Castro Valley Blvd", "Hayward", "CA", "94544",
                    "Castro Valley", 37.6688, -122.0808, 3, 2, new BigDecimal("1500"),
                    1970, new BigDecimal("725000"), true, 2, false, true,
                    Arrays.asList("Large Lot", "BART Nearby", "Updated Bathrooms", "Hardwood Floors")));

            // Luxury Properties
            properties.add(createProperty(agent1.getId(), agent1.getEmail(), agent1.getFirstName(), agent1.getLastName(),"Atherton Luxury Estate",
                    "Magnificent 6-bedroom estate on 2 acres with every amenity",
                    PropertyType.HOUSE, "123 Atherton Ave", "Atherton", "CA", "94027",
                    "Central Atherton", 37.4613, -122.1975, 6, 5, new BigDecimal("8500"),
                    2019, new BigDecimal("12500000"), true, 4, true, true,
                    Arrays.asList("2 Acre Lot", "Pool", "Tennis Court", "Wine Cellar", "Home Theater")));

            properties.add(createProperty(agent2.getId(), agent2.getEmail(), agent2.getFirstName(), agent2.getLastName(),"Sausalito Waterfront Masterpiece",
                    "Stunning 4-bedroom waterfront home with private dock",
                    PropertyType.HOUSE, "456 Bridgeway", "Sausalito", "CA", "94965",
                    "Waterfront", 37.8590, -122.4852, 4, 4, new BigDecimal("3800"),
                    2018, new BigDecimal("7850000"), true, 2, false, true,
                    Arrays.asList("Waterfront", "Private Dock", "Bay Views", "Chef's Kitchen")));

            // Commercial Property
            properties.add(createProperty(agent3.getId(), agent3.getEmail(), agent3.getFirstName(), agent3.getLastName(),"Prime Commercial Space in San Mateo",
                    "Well-located commercial building perfect for retail or office",
                    PropertyType.COMMERCIAL, "555 El Camino Real", "San Mateo", "CA", "94402",
                    "Downtown", 37.5630, -122.3255, 0, 2, new BigDecimal("5000"),
                    2000, new BigDecimal("2750000"), true, 10, false, false,
                    Arrays.asList("High Traffic", "Corner Location", "Updated Systems", "Flexible Space")));

            // Multi-Family
            properties.add(createProperty(agent1.getId(),agent1.getEmail(), agent1.getFirstName(), agent1.getLastName(), "Income-Producing Duplex",
                    "Well-maintained duplex with 2 units, great investment opportunity",
                    PropertyType.MULTI_FAMILY, "890 Park Ave", "San Jose", "CA", "95126",
                    "Rose Garden", 37.3382, -121.9248, 4, 2, new BigDecimal("2200"),
                    1950, new BigDecimal("1350000"), true, 2, false, true,
                    Arrays.asList("2 Units", "Separate Entrances", "Updated Plumbing", "Long-term Tenants")));

            // Additional Variety - Apartments
            properties.add(createProperty(agent3.getId(), agent3.getEmail(), agent3.getFirstName(), agent3.getLastName(),"Modern Studio in SOMA",
                    "Efficient studio apartment perfect for young professionals",
                    PropertyType.APARTMENT, "123 Folsom St", "San Francisco", "CA", "94105",
                    "SOMA", 37.7881, -122.3972, 0, 1, new BigDecimal("450"),
                    2019, new BigDecimal("575000"), false, 0, false, false,
                    Arrays.asList("Pet Friendly", "Roof Deck", "Bike Storage", "Gym")));

            properties.add(createProperty(agent2.getId(), agent2.getEmail(), agent2.getFirstName(), agent2.getLastName(),"Luxury Penthouse Apartment",
                    "3-bedroom penthouse with wraparound terrace and stunning views",
                    PropertyType.APARTMENT, "1 Hawthorne St", "San Francisco", "CA", "94105",
                    "Rincon Hill", 37.7886, -122.3919, 3, 3, new BigDecimal("2800"),
                    2020, new BigDecimal("4500000"), false, 2, false, false,
                    Arrays.asList("Penthouse", "Wraparound Terrace", "Concierge", "Bay Views", "Wine Storage")));

            // Budget-Friendly Options
            properties.add(createProperty(agent1.getId(), agent1.getEmail(), agent1.getFirstName(), agent1.getLastName(),"Cozy Oakland Apartment",
                    "Charming 1-bedroom apartment in friendly neighborhood",
                    PropertyType.APARTMENT, "567 Telegraph Ave", "Oakland", "CA", "94609",
                    "Temescal", 37.8315, -122.2646, 1, 1, new BigDecimal("650"),
                    2005, new BigDecimal("385000"), false, 0, false, false,
                    Arrays.asList("Walk Score 90", "Updated Kitchen", "Pet Friendly", "Near BART")));

            properties.add(createProperty(agent3.getId(), agent3.getEmail(), agent3.getFirstName(), agent3.getLastName(),"Affordable Richmond District House",
                    "Starter home with potential in quiet neighborhood",
                    PropertyType.HOUSE, "456 38th Ave", "San Francisco", "CA", "94121",
                    "Richmond District", 37.7768, -122.4974, 2, 1, new BigDecimal("1200"),
                    1940, new BigDecimal("850000"), true, 1, false, true,
                    Arrays.asList("Near Golden Gate Park", "Original Hardwood", "Fixer Opportunity", "Large Lot")));

            // Mid-Range Family Homes
            properties.add(createProperty(agent2.getId(), agent2.getEmail(), agent2.getFirstName(), agent2.getLastName(),"Pleasant Hill Family Home",
                    "Spacious 4-bedroom home with excellent schools nearby",
                    PropertyType.HOUSE, "789 Gregory Ln", "Pleasant Hill", "CA", "94523",
                    "Downtown", 37.9480, -122.0608, 4, 2, new BigDecimal("2100"),
                    1985, new BigDecimal("975000"), true, 2, false, true,
                    Arrays.asList("Top Schools", "Updated Kitchen", "Large Backyard", "Cul-de-sac")));

            properties.add(createProperty(agent1.getId(), agent1.getEmail(), agent1.getFirstName(), agent1.getLastName(),"Walnut Creek Suburban Dream",
                    "Beautiful 3-bedroom home in desirable Walnut Creek",
                    PropertyType.HOUSE, "234 Ygnacio Valley Rd", "Walnut Creek", "CA", "94596",
                    "Ygnacio Valley", 37.9070, -122.0441, 3, 2, new BigDecimal("1900"),
                    1995, new BigDecimal("1125000"), true, 2, true, true,
                    Arrays.asList("Pool", "Mountain Views", "Gourmet Kitchen", "Three Car Garage")));

            // Townhouses
            properties.add(createProperty(agent3.getId(), agent3.getEmail(), agent3.getFirstName(), agent3.getLastName(),"Contemporary Redwood City Townhouse",
                    "Brand new 3-bedroom townhouse in gated community",
                    PropertyType.TOWNHOUSE, "890 Main St", "Redwood City", "CA", "94063",
                    "Downtown", 37.4852, -122.2261, 3, 2, new BigDecimal("1650"),
                    2022, new BigDecimal("1285000"), true, 2, false, false,
                    Arrays.asList("New Construction", "Gated Community", "Solar Panels", "EV Charging")));

            properties.add(createProperty(agent2.getId(), agent2.getEmail(), agent2.getFirstName(), agent2.getLastName(),"Sunnyvale Tech Worker's Townhouse",
                    "Modern 2-bedroom townhouse close to tech campuses",
                    PropertyType.TOWNHOUSE, "123 Mathilda Ave", "Sunnyvale", "CA", "94086",
                    "North Sunnyvale", 37.3894, -122.0307, 2, 2, new BigDecimal("1350"),
                    2021, new BigDecimal("950000"), true, 2, false, false,
                    Arrays.asList("Near Tech Companies", "Smart Home", "Community Amenities", "Low HOA")));

            // Condos with variety
            properties.add(createProperty(agent1.getId(), agent1.getEmail(), agent1.getFirstName(), agent1.getLastName(),"Marina District Condo",
                    "Bright 2-bedroom condo steps from the Marina",
                    PropertyType.CONDO, "567 Chestnut St", "San Francisco", "CA", "94123",
                    "Marina District", 37.8021, -122.4386, 2, 1, new BigDecimal("1100"),
                    2008, new BigDecimal("1175000"), false, 1, false, false,
                    Arrays.asList("Marina Views", "Updated Interior", "In-unit Laundry", "Storage")));

            properties.add(createProperty(agent3.getId(), agent3.getEmail(), agent3.getFirstName(), agent3.getLastName(),"Mountain View Tech Hub Condo",
                    "1-bedroom condo perfect for tech professionals",
                    PropertyType.CONDO, "789 Castro St", "Mountain View", "CA", "94041",
                    "Downtown", 37.3938, -122.0810, 1, 1, new BigDecimal("750"),
                    2015, new BigDecimal("685000"), false, 1, false, false,
                    Arrays.asList("Walk to Downtown", "Bike to Google", "Gym", "Pool")));

            // More Luxury Properties
            properties.add(createProperty(agent2.getId(), agent2.getEmail(), agent2.getFirstName(), agent2.getLastName(),"Los Gatos Estate with Vineyard",
                    "5-bedroom Mediterranean estate on 5 acres with vineyard",
                    PropertyType.HOUSE, "456 Los Gatos Blvd", "Los Gatos", "CA", "95030",
                    "Los Gatos Hills", 37.2358, -121.9620, 5, 4, new BigDecimal("6000"),
                    2010, new BigDecimal("6750000"), true, 3, true, true,
                    Arrays.asList("Vineyard", "Pool", "Guest House", "Wine Cellar", "Mountain Views")));

            properties.add(createProperty(agent1.getId(), agent1.getEmail(), agent1.getFirstName(), agent1.getLastName(),"Tiburon Waterfront Paradise",
                    "Spectacular 4-bedroom home with 180-degree bay views",
                    PropertyType.HOUSE, "123 Paradise Dr", "Tiburon", "CA", "94920",
                    "Paradise Cay", 37.8735, -122.4564, 4, 3, new BigDecimal("3500"),
                    2016, new BigDecimal("5950000"), true, 2, false, true,
                    Arrays.asList("Waterfront", "Private Beach", "Bay Views", "Boat Dock")));

            // Investment Properties
            properties.add(createProperty(agent3.getId(), agent3.getEmail(), agent3.getFirstName(), agent3.getLastName(),"San Leandro Investment Triplex",
                    "Fully occupied triplex with strong rental income",
                    PropertyType.MULTI_FAMILY, "234 E 14th St", "San Leandro", "CA", "94577",
                    "Downtown", 37.7249, -122.1560, 6, 3, new BigDecimal("3000"),
                    1960, new BigDecimal("1450000"), true, 3, false, true,
                    Arrays.asList("3 Units", "Fully Occupied", "Separate Utilities", "Strong Cash Flow")));

            // Small Homes - Perfect for filtering tests
            properties.add(createProperty(agent2.getId(), agent2.getEmail(), agent2.getFirstName(), agent2.getLastName(),"Tiny Home in Santa Cruz",
                    "Adorable 1-bedroom tiny home near the beach",
                    PropertyType.HOUSE, "890 Beach St", "Santa Cruz", "CA", "95060",
                    "Beach Flats", 36.9741, -122.0308, 1, 1, new BigDecimal("600"),
                    2018, new BigDecimal("525000"), false, 0, false, true,
                    Arrays.asList("Near Beach", "Eco-Friendly", "Low Maintenance", "Community Garden")));

            // Large Family Homes
            properties.add(createProperty(agent1.getId(), agent1.getEmail(), agent1.getFirstName(), agent1.getLastName(),"Expansive Danville Family Estate",
                    "6-bedroom executive home in prestigious Blackhawk",
                    PropertyType.HOUSE, "567 Blackhawk Dr", "Danville", "CA", "94506",
                    "Blackhawk", 37.7800, -121.9163, 6, 5, new BigDecimal("5500"),
                    2005, new BigDecimal("3750000"), true, 4, true, true,
                    Arrays.asList("Blackhawk Country Club", "Pool", "Home Theater", "Wine Cellar", "Guest Suite")));

            // Commercial with variety
            properties.add(createProperty(agent3.getId(),agent3.getEmail(), agent3.getFirstName(), agent3.getLastName(), "Downtown Oakland Office Building",
                    "Modern office building with retail ground floor",
                    PropertyType.COMMERCIAL, "1234 Broadway", "Oakland", "CA", "94612",
                    "Downtown", 37.8044, -122.2711, 0, 4, new BigDecimal("8000"),
                    2012, new BigDecimal("4250000"), true, 20, false, false,
                    Arrays.asList("Class A Office", "Retail Space", "BART Adjacent", "Updated Systems")));

            properties.add(createProperty(agent2.getId(), agent2.getEmail(), agent2.getFirstName(), agent2.getLastName(),"Retail Space on University Avenue",
                    "Prime retail location in heart of Palo Alto",
                    PropertyType.COMMERCIAL, "456 University Ave", "Palo Alto", "CA", "94301",
                    "Downtown", 37.4467, -122.1596, 0, 2, new BigDecimal("2500"),
                    1995, new BigDecimal("3150000"), false, 5, false, false,
                    Arrays.asList("High Foot Traffic", "Corner Location", "Established Area", "Flexible Layout")));

            // Land Properties
            properties.add(createProperty(agent1.getId(), agent1.getEmail(), agent1.getFirstName(), agent1.getLastName(),"Prime Development Land in Cupertino",
                    "1.5 acre lot approved for residential development",
                    PropertyType.LAND, "789 Stevens Creek Blvd", "Cupertino", "CA", "95014",
                    "Monta Vista", 37.3230, -122.0322, 0, 0, new BigDecimal("65340"),
                    0, new BigDecimal("2850000"), false, 0, false, false,
                    Arrays.asList("Approved Plans", "Utilities Available", "Top Schools", "1.5 Acres")));

            properties.add(createProperty(agent3.getId(), agent3.getEmail(), agent3.getFirstName(), agent3.getLastName(),"Half Moon Bay Ocean View Lot",
                    "Beautiful 0.75 acre lot with ocean views",
                    PropertyType.LAND, "234 Highway 1", "Half Moon Bay", "CA", "94019",
                    "Coastal", 37.4636, -122.4286, 0, 0, new BigDecimal("32670"),
                    0, new BigDecimal("1750000"), false, 0, false, false,
                    Arrays.asList("Ocean Views", "Build Your Dream Home", "Coastal Location", "0.75 Acres")));

            // Edge Cases for Testing
            properties.add(createProperty(agent2.getId(), agent2.getEmail(), agent2.getFirstName(), agent2.getLastName(),"Ultra-Luxury Hillsborough Mansion",
                    "Stunning 8-bedroom estate on 3 acres with every conceivable amenity",
                    PropertyType.HOUSE, "1 Exclusive Ln", "Hillsborough", "CA", "94010",
                    "Central Hillsborough", 37.5741, -122.3780, 8, 7, new BigDecimal("12000"),
                    2021, new BigDecimal("18500000"), true, 6, true, true,
                    Arrays.asList("Tennis Court", "Pool", "Guest House", "Wine Cellar", "Home Theater", "Elevator")));

            properties.add(createProperty(agent1.getId(), agent1.getEmail(), agent1.getFirstName(), agent1.getLastName(),"Budget Starter in Pittsburg",
                    "Entry-level 2-bedroom home perfect for first-time buyers",
                    PropertyType.HOUSE, "567 Railroad Ave", "Pittsburg", "CA", "94565",
                    "Old Town", 38.0280, -121.8847, 2, 1, new BigDecimal("950"),
                    1955, new BigDecimal("425000"), true, 1, false, true,
                    Arrays.asList("First Time Buyer", "BART Access", "Investment Potential", "Fenced Yard")));

            // Save all properties
            propertyListingRepository.saveAll(properties);

            log.info("Created {} sample property listings", properties.size());
            log.info("=== Sample data loading complete ===");
            
            // Log summary statistics
            log.info("Property breakdown:");
            log.info("- Houses: {}", properties.stream().filter(p -> p.getPropertyType() == PropertyType.HOUSE).count());
            log.info("- Apartments: {}", properties.stream().filter(p -> p.getPropertyType() == PropertyType.APARTMENT).count());
            log.info("- Condos: {}", properties.stream().filter(p -> p.getPropertyType() == PropertyType.CONDO).count());
            log.info("- Townhouses: {}", properties.stream().filter(p -> p.getPropertyType() == PropertyType.TOWNHOUSE).count());
            log.info("- Commercial: {}", properties.stream().filter(p -> p.getPropertyType() == PropertyType.COMMERCIAL).count());
            log.info("- Multi-Family: {}", properties.stream().filter(p -> p.getPropertyType() == PropertyType.MULTI_FAMILY).count());
            log.info("- Land: {}", properties.stream().filter(p -> p.getPropertyType() == PropertyType.LAND).count());
            log.info("Price range: ${} - ${}", 
                    properties.stream().map(PropertyListing::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO),
                    properties.stream().map(PropertyListing::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        };
    }

    /**
     * Cache eviction needed, because later id mismatch occurs when calling the my-listing endpoint.
     * The cached and the updated user id has mismatch!!
     */

    private User createAgent(String email, String firstName, String lastName) {
        User agent = User.builder()
                .username(firstName.toLowerCase() + "_" + lastName.toLowerCase())
                .email(email)
                .password(passwordEncoder.encode("Password123#"))
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber("3123123")
                .role(Role.AGENT)
                .enabled(true)
                .accountNonLocked(true)
                .build();
        agent.setCreatedBy("system");
        agent.setLastModifiedBy("system");

        RegisterRequest registerRequest = new RegisterRequest(
                agent.getUsername(),
                agent.getEmail(),
                "Password123#",
                agent.getFirstName(),
                agent.getLastName(),
                agent.getPhoneNumber(),
                agent.getRole()
        );
        AuthResponse registrationResponse = authService.register(
                registerRequest,
                "0:0:0:0:0:0:0:1"
        );

        agent.setRefreshToken(registrationResponse.getRefreshToken());
        LocalDateTime refreshExpiryInLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(registrationResponse.getRefreshExpiresIn()), ZoneId.systemDefault());
        agent.setRefreshTokenExpiryDate(refreshExpiryInLocalDateTime);

        User savedAgent = updateUser(agent);
        // System.out.println(cacheManager.getCache("usersByEmail").get(savedAgent.getEmail(), User.class).getId());

        log.info("agent id: {}, email: {}, accesstoken: {}, refreshtoken: {}", savedAgent.getId(), savedAgent.getEmail(), registrationResponse.getAccessToken(), registrationResponse.getRefreshToken());
        return savedAgent;
    }

    private User createHomeSeeker(String email, String firstName, String lastName, String phoneNumber) {
        User seeker = User.builder()
                .username(firstName.toLowerCase() + "_" + lastName.toLowerCase())
                .email(email)
                .password(passwordEncoder.encode("Password123#"))
                .firstName(firstName)
                .lastName(lastName)
                .role(Role.USER)
                .phoneNumber(phoneNumber)
                .enabled(true)
                .accountNonLocked(true)
                .build();
        seeker.setCreatedBy("system");
        seeker.setLastModifiedBy("system");

        RegisterRequest registerRequest = new RegisterRequest(
                seeker.getUsername(),
                seeker.getEmail(),
                "Password123#",
                seeker.getFirstName(),
                seeker.getLastName(),
                seeker.getPhoneNumber(),
                seeker.getRole()
        );
        AuthResponse registrationResponse = authService.register(
                registerRequest,
                "0:0:0:0:0:0:0:1"
        );

        seeker.setRefreshToken(registrationResponse.getRefreshToken());
        LocalDateTime refreshExpiryInLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(registrationResponse.getRefreshExpiresIn()), ZoneId.systemDefault());
        seeker.setRefreshTokenExpiryDate(refreshExpiryInLocalDateTime);

        User savedSeeker = updateUser(seeker);

        log.info("home seeker id: {}, email: {}, accesstoken: {}, refreshtoken: {}", savedSeeker.getId(), savedSeeker.getEmail(), registrationResponse.getAccessToken(), registrationResponse.getRefreshToken());
        return savedSeeker;
    }


    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @CacheEvict(value = "usersByEmail", key = "#user.email")
    public void deleteUser(User user) {
        userRepository.delete(user);
    }


    private PropertyListing createProperty(String agentId, String agentEmail, String agentFirstName,
                                           String agentLastName, String title, String description,
                                          PropertyType propertyType, String street, String city,
                                          String state, String zipCode, String neighborhood,
                                          double latitude, double longitude, int bedrooms,
                                          int bathrooms, BigDecimal squareFeet, int yearBuilt,
                                          BigDecimal price, boolean hasGarage, int garageSpaces,
                                          boolean hasPool, boolean hasGarden, List<String> features) {
        return PropertyListing.builder()
                .agentId(agentId)
                .agentEmail(agentEmail)
                .agentName(agentFirstName + " " + agentLastName)
                .title(title)
                .description(description)
                .propertyType(propertyType)
                .status(PropertyStatus.ACTIVE)
                .street(street)
                .city(city)
                .state(state)
                .zipCode(zipCode)
                .neighborhood(neighborhood)
                .location(new GeoPoint(latitude, longitude))
                .bedrooms(bedrooms)
                .bathrooms(bathrooms)
                .squareFeet(squareFeet)
                .yearBuilt(yearBuilt)
                .price(price)
                .hasGarage(hasGarage)
                .garageSpaces(garageSpaces)
                .hasPool(hasPool)
                .hasGarden(hasGarden)
                .features(features != null ? features : new ArrayList<>())
                .photos(new ArrayList<>())
                .build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null || xForwardedForHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        // X-Forwarded-For can contain multiple IPs, first one is the client
        return xForwardedForHeader.split(",")[0].trim();
    }

}
