package de.samply.fhirtransfair;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Specimen;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Main Application Entrypoint. */
@SpringBootApplication
public class FhirTransFairApplication {

  @Value("http://localhost:8080/fhir")
  private static String bbmriFhirStore;

  @Value("http://localhost:8081/fhir")
  private String miiFhirStore;

  /**
   * Starts the program.
   *
   * @param args additional program arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(FhirTransFairApplication.class, args);

  }


}
