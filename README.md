# Doctor Appointment Management System

A Spring Boot application for managing doctor appointments with JWT authentication.

## Project Structure

The application follows a modern Spring Boot architecture with the following structure:

```
src/main/java/com/doctor/appointment/
├── config/             # Configuration classes
├── controller/         # REST controllers
├── exception/          # Global exception handling
├── model/              # Entity classes and DTOs
├── repository/         # JPA repositories
├── service/            # Business logic
└── AppointmentApplication.java  # Main application class
```

## Features

- Doctor management
- Patient management
- Appointment scheduling
- User authentication with JWT
- Role-based access control
- API documentation with Swagger/OpenAPI

## Technologies

- Java 21
- Spring Boot 3.4.1
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL
- Lombok
- ModelMapper
- Swagger/OpenAPI for documentation

## Getting Started

### Prerequisites

- Java 21
- PostgreSQL
- Maven

### Database Setup

1. Create a PostgreSQL database named `appointment`
2. Update the database configuration in `application.yml` if needed

### Running the Application

```bash
# Clone the repository
git clone https://github.com/yourusername/doctor-appointment.git

# Navigate to the project directory
cd doctor-appointment

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on port 8081. You can access the API at `http://localhost:8081/api`

### API Documentation

Swagger UI is available at: `http://localhost:8081/api/swagger-ui.html`

## API Endpoints

### Authentication

- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/register` - Register a new user

### Doctors

- `GET /api/doctors` - Get all doctors
- `GET /api/doctors/{id}` - Get doctor by ID
- `POST /api/doctors` - Create a new doctor
- `PUT /api/doctors/{id}` - Update a doctor
- `DELETE /api/doctors/{id}` - Delete a doctor

### Patients

- `GET /api/patients` - Get all patients
- `GET /api/patients/{id}` - Get patient by ID
- `POST /api/patients` - Create a new patient
- `PUT /api/patients/{id}` - Update a patient
- `DELETE /api/patients/{id}` - Delete a patient

### Appointments

- `GET /api/appointments` - Get all appointments
- `GET /api/appointments/{id}` - Get appointment by ID
- `POST /api/appointments` - Create a new appointment
- `PUT /api/appointments/{id}` - Update an appointment
- `DELETE /api/appointments/{id}` - Delete an appointment

## License

This project is licensed under the MIT License - see the LICENSE file for details.
