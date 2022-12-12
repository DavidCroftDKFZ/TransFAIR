package de.samply.transfair.fhir.clients;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;

public class FhirClient {

  private IGenericClient client;

  public void setBasicAuth(String username, String password){
    IClientInterceptor authInterceptor = new BasicAuthInterceptor(username, password);
    client.registerInterceptor(authInterceptor);
  }

  public FhirClient(FhirContext ctx, String server) {
    client = ctx.newRestfulGenericClient(server);
  }

  public IGenericClient getClient() {
    return client;
  }
}
