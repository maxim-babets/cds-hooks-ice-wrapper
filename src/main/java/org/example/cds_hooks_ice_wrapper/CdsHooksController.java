package org.example.cds_hooks_ice_wrapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cds-services")
public class CdsHooksController {

    @Autowired
    private RestTemplate restTemplate;

    private final FhirContext fhirContext = FhirContext.forR4();

    @Value("${fhir.server.url}")
    private String fhirServerUrl;

    @Value("${ice.service.url}")
    private String iceServiceUrl;

    @GetMapping
    public Map<String, Object> discovery() {
        return Map.of(
                "services", List.of(
                        Map.of(
                                "id", "ice-immunization-forecast",
                                "hook", "patient-view",
                                "title", "Immunization Forecast by ICE",
                                "description", "Provides immunization recommendations using ICE engine"
                        )
                )
        );
    }

    @PostMapping(
            value = "/ice-immunization-forecast",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Map<String, Object> handlePatientView(@RequestBody Map<String, Object> hookRequest) {
        String fhirServer = (String) hookRequest.getOrDefault("fhirServer", fhirServerUrl);
        Map<String, Object> context = (Map<String, Object>) hookRequest.get("context");
        String patientId = (context != null) ? (String) context.get("patientId") : null;

        String patientName = "";
        List<Map<String, Object>> overdueImmunizations = new ArrayList<>();

        if (fhirServer != null && patientId != null) {
            Patient patient = fetchPatientFromFhir(fhirServer, patientId);
            if (patient != null && patient.getNameFirstRep() != null) {
                patientName = patient.getNameFirstRep().getNameAsSingleString();
            }
            overdueImmunizations = fetchImmunizationsFromFhir(fhirServer, patientId);
        }

        Map<String, Object> iceVersionInfo = callIceVersion();
        String version = (iceVersionInfo != null) ? (String) iceVersionInfo.get("iceVersion") : "Unknown";

        final String finalPatientName = patientName;
        final String finalVersion = version;

        List<Map<String, Object>> cards = overdueImmunizations.stream()
                .map(immunization -> {
                    final String immunizationDate = (String) immunization.get("date");
                    final String vaccineCode = (String) immunization.get("vaccineCode");

                    Map<String, Object> suggestion = new HashMap<>();
                    suggestion.put("label", String.format("Order %s Vaccine", vaccineCode));
                    suggestion.put("uuid", UUID.randomUUID().toString());
                    suggestion.put("actions", Collections.emptyList());

                    Map<String, Object> card = new HashMap<>();
                    card.put("summary", String.format("Patient: %s is overdue for %s vaccine", finalPatientName, vaccineCode));
                    card.put("detail", String.format("The patient missed the scheduled immunization on %s. ICE version: %s",
                            immunizationDate, finalVersion));
                    card.put("indicator", "warning");
                    card.put("source", Map.of("label", "FHIR Server"));
                    card.put("suggestions", Collections.singletonList(suggestion));
                    return card;
                })
                .collect(Collectors.toList());

        return Map.of("cards", cards);
    }

    private Map<String, Object> callIceVersion() {
        String url = iceServiceUrl;
        try {
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            System.out.println("Error calling ICE: " + e.getMessage());
            return null;
        }
    }

    private Patient fetchPatientFromFhir(String baseUrl, String patientId) {
        try {
            IGenericClient client = fhirContext.newRestfulGenericClient(baseUrl);
            return client.read()
                    .resource(Patient.class)
                    .withId(patientId)
                    .execute();
        } catch (Exception e) {
            System.out.println("Error fetching Patient from FHIR: " + e.getMessage());
            return null;
        }
    }

    private List<Map<String, Object>> fetchImmunizationsFromFhir(String baseUrl, String patientId) {
        try {
            IGenericClient client = fhirContext.newRestfulGenericClient(baseUrl);
            Bundle bundle = client.search()
                    .forResource(Immunization.class)
                    .where(Immunization.PATIENT.hasId(patientId))
                    .returnBundle(Bundle.class)
                    .execute();

            return bundle.getEntry().stream()
                    .map(entry -> {
                        Immunization immunization = (Immunization) entry.getResource();
                        Map<String, Object> immunizationData = new HashMap<>();
                        immunizationData.put("date", immunization.getOccurrenceDateTimeType().getValueAsString());
                        immunizationData.put("vaccineCode", immunization.getVaccineCode().getCodingFirstRep().getDisplay());
                        immunizationData.put("status", immunization.getStatus() != null ? immunization.getStatus().toCode() : "unknown");
                        return immunizationData;
                    })
                    .filter(immunization -> "overdue".equalsIgnoreCase((String) immunization.get("status")))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Error fetching Immunizations from FHIR: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}