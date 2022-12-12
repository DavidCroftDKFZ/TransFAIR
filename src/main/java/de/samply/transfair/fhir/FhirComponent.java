package de.samply.transfair.fhir;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.transfair.Configuration;
import de.samply.transfair.fhir.clients.FhirClient;
import de.samply.transfair.fhir.writers.FhirExportInterface;
import de.samply.transfair.fhir.writers.FhirFileSaver;
import de.samply.transfair.fhir.writers.FhirServerSaver;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FhirComponent {

  private static final Logger log = LoggerFactory.getLogger(FhirComponent.class);

  @Autowired
  public Configuration configuration;

  public Map<String, String> overrideConfig;

  private IGenericClient sourceFhirServer;
  private FhirExportInterface fhirExportInterface;

  FhirComponent() {
    configuration.getCtx().getRestfulClientFactory().setSocketTimeout(300 * 1000);
  }

  public void setSourceFhirServer(String server) {
    overrideConfig.put("sourceServer", server);
  }

  public void setTargetServer(String server) {
        overrideConfig.put("targetServer", server);
  }

  public IGenericClient getSourceFhirServer() {

    if(Objects.nonNull(sourceFhirServer)) {
      return this.sourceFhirServer;
    }

    String sourceFhirServer =
        Objects.nonNull(overrideConfig.get("sourceServer"))
            ? overrideConfig.get("sourceServer")
            : configuration.getSourceFhirServer();

    FhirClient sourceClient =
        new FhirClient(configuration.getCtx(), sourceFhirServer);
    if (Objects.nonNull(configuration.getSourceFhirServerUsername())
        && Objects.nonNull(configuration.getSourceFhirServerPassword())) {
      sourceClient.setBasicAuth(
          configuration.getSourceFhirServerUsername(), configuration.getSourceFhirServerPassword());
    }
    log.info("Start collecting Resources from FHIR server " + sourceFhirServer);
    return sourceClient.getClient();
  }


    public FhirExportInterface getFhirExportInterface() {
    if (Objects.isNull(fhirExportInterface)) {
      String targetFhirServer =
          Objects.nonNull(overrideConfig.get("targetServer"))
              ? overrideConfig.get("targetServer")
              : configuration.getTargetFhirServer();

      if (configuration.isSaveToFileSystem()) {
        this.fhirExportInterface = new FhirFileSaver(configuration.getCtx());
      } else {
        this.fhirExportInterface = new FhirServerSaver(configuration.getCtx(), targetFhirServer);
        log.info("Start exporting resources to FHIR server " + sourceFhirServer);

      }
    }

    return fhirExportInterface;
  }
}
