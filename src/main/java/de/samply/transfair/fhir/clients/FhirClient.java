package de.samply.transfair.fhir.clients;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;

/** Fhir generic client with some additions. */
public class FhirClient {

  private IGenericClient client;

  /** Sets basic auth for client. */
  public void setBasicAuth(String username, String password) {
    IClientInterceptor authInterceptor = new BasicAuthInterceptor(username, password);
    client.registerInterceptor(authInterceptor);
  }

  /** Creates the fhir server client. */
  public FhirClient(FhirContext ctx, String server) {
    client = ctx.newRestfulGenericClient(server);
  }

  /** Return the fhir server client. */
  public IGenericClient getClient() {
    return client;
  }
}
