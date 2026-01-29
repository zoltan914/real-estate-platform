# Real Estate Listing Platform

Credit to: Devtiro for project brief, and youtube videos www.youtube.com/@devtiro


A comprehensive real estate platform built with Spring Boot 4, Elasticsearch, and JWT authentication. This platform enables real estate agents to manage property listings and allows home seekers to search properties and schedule viewings.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Architecture Overview](#architecture-overview)
- [Security Features](#security-features)

## Features

### For Real Estate Agents
- Create, update, and delete property listings
- Upload multiple photos for properties
- Manage listing status (ACTIVE, UNDER CONTRACT, SOLD, TEMPORARILY OFF MARKET)
- View and manage all their listings
- Schedule and manage property viewings
- Confirm or reschedule viewing appointments

### For Home Seekers
- Search properties with advanced filters (location, price, type, features)
- Geo-spatial search with distance-based filtering
- Request property viewings
- Reschedule or cancel viewing appointments
- View detailed property information with photos

### Platform Features
- Role-based access control (AGENT, USER)
- JWT-based authentication with refresh tokens
- Account lockout after failed login attempts
- Comprehensive security audit logging
- Caching with Caffeine for improved performance
- Metrics and monitoring with Prometheus
- Email notifications for viewing confirmations
- Asynchronous processing for notifications
- Full-text search with Elasticsearch

## Technology Stack

- **Framework**: Spring Boot 4.0.2
- **Language**: Java 25
- **Security**: Spring Security with JWT
- **Database**: Elasticsearch 9.2.3
- **Caching**: Caffeine Cache
- **Metrics**: Micrometer + Prometheus (can be added via docker - not configure in this project)
- **Email**: Spring Mail
- **Object Mapping**: MapStruct 1.6.3
- **Build Tool**: Maven
- **Container**: Docker Compose

## Prerequisites

- Java 25 or higher
- Maven 3.8+
- Docker and Docker Compose
- Gmail accounts (2 separate existing gmail accounts for email notifications, one as an smpt host, see the configuration below)

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd real-estate-platform
```

### 2. Start Elasticsearch

Start the Elasticsearch container using Docker Compose:

```bash
docker-compose up -d
```

This will start Elasticsearch on `http://localhost:9200`.

### 3. Configure Local Settings

Create an `application-local.properties` file in the project root directory. This file is git-ignored and should contain your local/sensitive configurations.

### 4. Build the Application

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080` by default.

## Configuration

### Main Configuration (`application.yml`)

The main configuration file uses environment variables with sensible defaults. Key configurations include:

- **Elasticsearch**: Connection settings for the search engine
- **Email**: SMTP configuration for notifications
- **JWT**: Token generation and validation settings
- **Security**: Password policies and login attempt controls
- **Caching**: Cache configuration for performance
- **Metrics**: Prometheus metrics endpoints

### Local Configuration (`application-local.properties`)

Create this file in the project root with the following required properties:

```properties
ELASTICSEARCH_URL=http://localhost:9200
ELASTICSEARCH_USERNAME=
ELASTICSEARCH_PASSWORD=

# Email Configuration (For AGENT, set with app.agent.email parameter in the application.yml)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-specific-password

# JWT Configuration
# your base64 code (don't need to be set here, one is already set in application.yml)
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Application Configuration
SERVER_PORT=8080

# (For home-seeker/USER, set with app.user.email parameter in the application.yml)
USER_EMAIL=your-secondary-email@gmail.com
```
MAIL_USERNAME and USER_EMAIL will be used to create initial agent and user/home-seeker accounts

Important note for running the postman test collection (see below)

### Gmail Configuration

To use Gmail for email notifications:

1. Enable 2-Factor Authentication on your Google account
2. Generate an App Password:
   - Go to Google Account Settings → Security → 2-Step Verification → App Passwords
   - Select "Mail" and generate a password
3. Use this generated password as `MAIL_PASSWORD` in your `application-local.properties`

### DataConfig Summary

The `DataConfig.java` class serves as the application's data initialization layer. It implements `CommandLineRunner` to seed the database with sample data on startup.

**Key Responsibilities:**

1. **User Creation**
   - Creates sample AGENT users with full access to property management
   - Creates sample USER (home seeker) accounts with viewing capabilities
   - All users are created with password: `Password123#`
   - Generates JWT tokens (access + refresh) for each user during registration

2. **Property Listings**
   - Generates 40+ sample property listings across multiple cities (San Francisco, Oakland, Berkeley, Palo Alto, San Jose, etc.)
   - Diverse property types: HOUSE, CONDO, APARTMENT, TOWNHOUSE, LAND
   - Realistic data including:
     - Geo-coordinates for location-based searches
     - Price ranges from ~525K to 4.5M
     - Detailed property features and amenities
     - Property-specific attributes (bedrooms, bathrooms, square footage, year built)

3. **Cache Management**
   - Implements cache eviction strategies to prevent ID mismatches
   - Ensures data consistency between cached users and database updates

4. **Email Configuration**
   - Retrieves agent and user emails from properties for notifications
   - Links properties to agent owners via email

**Sample Users Created:**
- **Agents**: "<your-real-gmail>", agent2@realestate.com, agent3@realestate.com
- **Users**: "<your-real-secondary-gmail>", emily_davis@example.com, seeker2@email.com

The configuration makes extensive use of the `@Value` annotation to inject email addresses from properties files, allowing easy customization of test accounts without code changes.

### Email workaround

If you don't have two separate gmail account (one with 2-Factor Authentication enabled) you can manually comment out the
notification service parts from the PropertyViewingServiceImpl.java class.
The application.yml configuration file contains default agent and user emails.


## Running the Application

### Development Mode

```bash
mvn spring-boot:run
```

### Production Mode

```bash
mvn clean package
java -jar target/realestate-0.0.1-SNAPSHOT.jar
```

### Docker Deployment

The included `docker-compose.yml` currently only runs Elasticsearch. To run the full application in Docker, you would need to add a service definition for the Spring Boot application.

## API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/register` | Register new user | Public |
| POST | `/api/auth/login` | Login and get JWT tokens | Public |
| POST | `/api/auth/refresh` | Refresh access token | Public |
| GET | `/api/auth/attempt-info` | Get login attempt information | Public |

### Property Listing Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/listings` | Create new listing | AGENT |
| GET | `/api/listings` | Get all listings (paginated) | Public |
| GET | `/api/listings/{id}` | Get listing by ID | Public |
| GET | `/api/listings/my-listings` | Get agent's listings | AGENT |
| GET | `/api/listings/search` | Search listings with filters | Public |
| PUT | `/api/listings/{id}` | Update listing | AGENT (owner) |
| PATCH | `/api/listings/{id}/status` | Update listing status | AGENT (owner) |
| DELETE | `/api/listings/{id}` | Delete listing | AGENT (owner) |
| POST | `/api/listings/{id}/photos` | Upload photos | AGENT (owner) |

### Property Viewing Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/viewings/user/request` | Request a viewing | USER |
| POST | `/api/viewings/agent/create` | Create viewing (agent) | AGENT |
| PATCH | `/api/viewings/agent/{id}/confirm` | Confirm viewing | AGENT |
| PUT | `/api/viewings/{id}/reschedule` | Reschedule viewing | AGENT/USER |
| PUT | `/api/viewings/{id}/cancel` | Cancel viewing | AGENT/USER |
| PUT | `/api/viewings/{id}/status` | Update viewing status | AGENT |
| GET | `/api/viewings/my-viewings` | Get user's viewings | Authenticated |

### Search Parameters

The `/api/listings/search` endpoint supports the following parameters:

- `minPrice` / `maxPrice` - Price range filtering
- `minBedrooms` / `maxBedrooms` - Bedroom count filtering
- `minBathrooms` / `maxBathrooms` - Bathroom count filtering
- `propertyTypes` - Filter by property type(s)
- `latitude` / `longitude` / `radiusInKm` - Geo-spatial search
- `city` - Filter by city
- `state` - Filter by state
- `neighborhood` - Filter by neighborhood
- `hasGarage` / `hasPool` / `hasGarden` - Amenity filters
- `features` - Filter by specific features
- Standard pagination: `page`, `size`, `sort`

## Testing

### Postman Collection

A comprehensive Postman collection is included: `Real Estate Listing Platform Final.postman_collection.json`

This collection includes:
- All authentication flows
- Complete CRUD operations for listings
- Property search examples
- Viewing management scenarios
- Both success and error case examples

### Import Postman Collection

1. Open Postman
2. Click Import
3. Select the JSON file
4. The collection will be organized with folders for each feature
5. Create a new Environment in postman after you imported the collection (for example Real Estate Listing Platform)
6. Create two variables: **agent_email** and **user_email** and set your real existing email address (make sure the 2fa enabled email address is the agent and the secondary is the user (home-seeker))
7. Run the tests

### Sample Test Credentials

From the DataConfig initialization:

**Agent Account:**
- Email: `agent1@realestate.com` (or your configured agent email - must be set for notification)
- Password: `Password123#`

**User Account:**
- Email: `seeker1@email.com` (or your configured user email - must be set for notification)
- Password: `Password123#`

## Architecture Overview

### Project Structure

```
src/main/java/com/devtiro/realestate/
├── config/              # Configuration classes
│   ├── AsyncConfig.java          # Async processing config
│   ├── AuditConfig.java          # JPA auditing config
│   ├── CacheConfig.java          # Caffeine cache config
│   ├── DataConfig.java           # Sample data initialization
│   ├── ElasticsearchConfig.java  # Elasticsearch setup
│   ├── SecurityConfig.java       # Security & JWT config
│   └── GlobalExceptionHandler.java
│
├── controller/          # REST controllers
│   ├── AuthController.java
│   ├── PropertyListingController.java
│   └── PropertyViewingController.java
│
├── domain/             # Domain layer
│   ├── dto/            # Data Transfer Objects
│   └── entities/       # JPA Entities
│
├── exceptions/         # Custom exceptions
├── mappers/           # MapStruct mappers
├── repositories/      # Spring Data repositories
├── security/          # Security components
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   ├── LoginAttemptService.java
│   ├── SecurityAuditService.java
│   └── MetricsService.java
│
└── services/          # Business logic
    ├── AuthService.java
    ├── PropertyListingService.java
    ├── PropertyViewingService.java
    └── NotificationService.java
```

### Key Design Patterns

1. **Repository Pattern**: Spring Data Elasticsearch repositories
2. **Service Layer Pattern**: Business logic separation
3. **DTO Pattern**: MapStruct for entity-DTO mapping
4. **Strategy Pattern**: Different search strategies
5. **Observer Pattern**: Event-driven notifications
6. **Filter Pattern**: JWT authentication filter

### Caching Strategy

The application uses Caffeine cache with the following configurations:

- **usersByEmail**: Caches user lookups (100 max, 10min expiry)
- **propertiesById**: Caches property details (500 max, 30min expiry)
- **propertySearch**: Caches search results (100 max, 5min expiry)

## Security Features

### Authentication & Authorization

1. **JWT-based Authentication**
   - Stateless authentication with access and refresh tokens
   - Access tokens expire in 1 hour
   - Refresh tokens expire in 7 days

2. **Role-Based Access Control**
   - `AGENT`: Can manage property listings and viewings
   - `USER`: Can search properties and request viewings

3. **Account Security**
   - Configurable password strength requirements
   - BCrypt password hashing (strength: 12)
   - Account lockout after 5 failed login attempts
   - 15-minute lockout duration

### Security Audit Logging

All security events are logged to `logs/security-audit.log`:
- Successful logins
- Failed login attempts
- Account lockouts
- JWT token generation
- Unauthorized access attempts
- Password validation failures

### Additional Security Measures

- CSRF protection disabled (stateless API)
- CORS configuration for cross-origin requests
- Request validation with Jakarta Validation
- Custom exception handling for security events
- Secure password validation

## Monitoring & Metrics

### Actuator Endpoints

Access monitoring endpoints at `/actuator/*`:

- `/actuator/health` - Application health status
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus-formatted metrics
- `/actuator/caches` - Cache statistics

### Prometheus Integration

Metrics are exposed in Prometheus format at `/actuator/prometheus`. Configure Prometheus to scrape this endpoint using the included `prometheus.yml` configuration.

**Available Metrics:**
- HTTP request latencies
- Cache hit/miss rates
- Login attempt metrics
- Custom business metrics

### Logging

Logs are written to:
- Console (stdout) - Development
- `logs/security-audit.log` - Security events

Log rotation configured with:
- Max file size: 100MB
- Max history: 30 days

## Troubleshooting

### Common Issues

**1. Elasticsearch Connection Refused**
```
Solution: Ensure Elasticsearch is running
docker-compose up -d
curl http://localhost:9200
```

**2. Email Sending Fails**
```
Solution: Check Gmail app password configuration
Ensure 2FA is enabled and app password is correct
```

**3. JWT Token Invalid**
```
Solution: Verify JWT_SECRET is properly configured
Ensure tokens haven't expired (check expiration times)
```

**4. Account Locked**
```
Solution: Wait for lockout duration (default 15 minutes)
Or restart application to clear login attempts cache
```

**5. Port Already in Use**
```
Solution: Change SERVER_PORT in application-local.properties
Or stop the process using port 8080
```

## Performance Considerations

1. **Caching**: Reduces database queries for frequently accessed data
2. **Pagination**: All list endpoints support pagination to limit data transfer
3. **Async Processing**: Email notifications sent asynchronously
4. **Connection Pooling**: Elasticsearch client uses connection pooling
5. **Lazy Loading**: Photos and related entities loaded on demand

## Development Guidelines

### Code Style
- Follow Java naming conventions
- Use Lombok to reduce boilerplate
- Implement proper exception handling
- Add logging for important operations
- Write comprehensive JavaDoc for public APIs

### Testing
- Write unit tests for services
- Use integration tests for repositories
- Test security configurations
- Validate all API endpoints

### Git Workflow
- The `application-local.properties` file is git-ignored
- Keep sensitive credentials out of version control
- Use meaningful commit messages
- Create feature branches for new development

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.

## Support

For issues and questions:
- Create an issue in the repository
- Check existing documentation
- Review Postman collection examples

## Acknowledgments

- <b><h2>Devtiro for project brief, and youtube videos
  www.youtube.com/@devtiro </h2></b>
- Spring Boot team for the excellent framework
- Elasticsearch for powerful search capabilities
- MapStruct for simplifying object mapping
- The open-source community

---

**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: January 2026
