package de.samply.transfair.fhir.writers;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Bundle;

public abstract class FhirExportInterface {

  public abstract Boolean export(Bundle bundle);

  FhirContext ctx;
}
