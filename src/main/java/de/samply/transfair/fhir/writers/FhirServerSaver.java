package de.samply.transfair.fhir.writers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;

/** Interface to post data to a fhir server. */
public class FhirServerSaver extends FhirExportInterface {

  IGenericClient client;

  /** Constructor. */
  public FhirServerSaver(FhirContext context, String targetServer) {
    this.ctx = context;
    this.client = ctx.newRestfulGenericClient(targetServer);
  }

  /** export. */
  @Override
  public Boolean export(Bundle bundle) {
    client.transaction().withBundle(bundle).execute();
    return true;
  }
}
