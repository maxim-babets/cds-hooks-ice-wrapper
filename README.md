# CDS Hooks ICE Wrapper

A Spring Boot application that integrates the Immunization Forecasting Engine (ICE) with FHIR servers through the CDS Hooks standard. This service provides clinical decision support for immunization recommendations.

## Overview

This wrapper service bridges between CDS Hooks implementations and the ICE immunization forecasting engine, enabling clinical systems to:
- Retrieve patient immunization history from FHIR-compliant servers
- Generate immunization forecasts and recommendations using the ICE engine
- Return actionable clinical decision cards through the CDS Hooks protocol

## Technology Stack

- **Java** (95.7% of codebase)
- **Spring Boot** - Web framework
- **HAPI FHIR** - FHIR client library
- **Docker** - Containerization

## Prerequisites

- Java 11 or higher
- Docker and Docker Compose
- Access to a FHIR-compliant server
- ICE (Immunization Forecasting Engine) service

## Quick Start

### 1. Start the ICE Engine

```bash
docker pull hlnconsulting/ice
docker run -p 8080:8080 hlnconsulting/ice
```

### 2. Build and Run the Application

```bash
docker build -t cds-hooks-ice-wrapper .
docker run -p 8081:8081 \
  -e FHIR_SERVER_URL=https://au-core.beda.software/fhir \
  -e ICE_SERVICE_URL=http://localhost:8080/opencds-decision-support-service/version \
  cds-hooks-ice-wrapper
```

## Configuration

The application can be configured via environment variables or `application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8081 | Server port |
| `server.address` | 0.0.0.0 | Server bind address |
| `fhir.server.url` | https://au-core.beda.software/fhir | FHIR server base URL |
| `ice.service.url` | http://localhost:8080/opencds-decision-support-service/version | ICE service endpoint |

**Example configuration (application.properties):**
```properties
spring.application.name=cds-hooks-ice-wrapper
server.address=0.0.0.0
server.port=8081
fhir.server.url=https://au-core.beda.software/fhir
ice.service.url=http://localhost:8080/opencds-decision-support-service/version
```

## API Endpoints

### Service Discovery

**Endpoint:** `GET /cds-services`

Returns available CDS services.

```bash
curl -X GET http://localhost:8081/cds-services
```

**Response:**
```json
{
  "services": [
    {
      "id": "ice-immunization-forecast",
      "hook": "patient-view",
      "title": "Immunization Forecast by ICE",
      "description": "Provides immunization recommendations using ICE engine"
    }
  ]
}
```

### Immunization Forecast

**Endpoint:** `POST /cds-services/ice-immunization-forecast`

Processes a CDS Hooks patient-view hook and returns immunization recommendations.

**Request Body:**
```json
{
  "fhirServer": "https://au-core.beda.software/fhir",
  "context": {
    "patientId": "patient-tc-2"
  }
}
```

## Usage Examples

### Retrieve Service Information

```bash
curl -X GET http://localhost:8081/cds-services
```

### Get Patient Data from FHIR

```bash
curl -X GET "https://au-core.beda.software/fhir/Patient/patient-tc-2" \
  -H "Accept: application/json"
```

### Get Immunization Records for Patient

```bash
curl -X GET "https://au-core.beda.software/fhir/Immunization?patient=patient-tc-2" \
  -H "Accept: application/json"
```

### List All Patients

```bash
curl -X GET "https://au-core.beda.software/fhir/Patient" \
  -H "Accept: application/json"
```

## Architecture

- **CdsHooksController** - REST API controller handling CDS Hooks requests
- **FHIR Integration** - Retrieves patient and immunization data from FHIR servers
- **ICE Integration** - Calls the ICE engine for immunization forecasting
- **Response Formatting** - Generates CDS Hooks-compliant cards with recommendations

## Error Handling

The application gracefully handles errors from FHIR and ICE services, returning empty results or default values when services are unavailable.

## License

[Specify your license here]

## Support

For issues and questions, please open an issue on the GitHub repository.
