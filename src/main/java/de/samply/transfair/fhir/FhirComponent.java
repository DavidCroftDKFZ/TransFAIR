package de.samply.transfair.fhir;

import ca.uhn.fhir.context.FhirContext;
import de.samply.transfair.Configuration;
import de.samply.transfair.fhir.writers.FhirExportInterface;
import de.samply.transfair.fhir.writers.FhirFileSaver;
import de.samply.transfair.fhir.writers.FhirServerSaver;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirComponent {

  @Autowired
  public Configuration configuration;

  public Map<String, String> overrideConfig;

  private FhirExportInterface fhirExportInterface;

  FhirComponent() {
    configuration.getCtx().getRestfulClientFactory().setSocketTimeout(300 * 1000);

    if (configuration.isSaveToFileSystem()) {
      this.fhirExportInterface = new FhirFileSaver(configuration.getCtx());
    } else {
      this.fhirExportInterface = new FhirServerSaver(configuration.getCtx(), configuration.getTargetFhirServer());
    }
  }

  public FhirExportInterface getFhirExportInterface() {
    return fhirExportInterface;
  }
}
