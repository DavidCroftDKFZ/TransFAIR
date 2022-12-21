package de.samply.transfair.fhir.writers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.transfair.fhir.clients.FhirClient;
import org.hl7.fhir.r4.model.Bundle;

/** Interface to post data to a fhir server. */
public class FhirServerSaver extends FhirExportInterface {

  private FhirClient client;

  /** Constructor. */
  public FhirServerSaver(FhirContext context, String targetServer) {
    this.ctx = context;
    this.client = new FhirClient(ctx, targetServer);
  }

  /** export. */
  @Override
  public Boolean export(Bundle bundle) {
    getClient().getClient().transaction().withBundle(bundle).execute();
    return true;
  }

  /** Target Fhir client. */
  public FhirClient getClient() {
    return client;
  }
}
