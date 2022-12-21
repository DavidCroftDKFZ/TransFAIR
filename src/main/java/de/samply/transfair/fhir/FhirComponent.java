package de.samply.transfair.fhir;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.transfair.Configuration;
import de.samply.transfair.converters.IdMapper;
import de.samply.transfair.fhir.clients.FhirClient;
import de.samply.transfair.fhir.writers.FhirExportInterface;
import de.samply.transfair.fhir.writers.FhirFileSaver;
import de.samply.transfair.fhir.writers.FhirServerSaver;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Main Class for working with fhir mappings. */
@Component
@Slf4j
public class FhirComponent {

  /** Configuration. */
  @Autowired public Configuration configuration;

  /** ID Mapper. */
  @Autowired private IdMapper mapper;

  /** Override Config. */
  public Map<String, String> overrideConfig = new HashMap<>();

  /** transferController. */
  public FhirTransfer transferController;

  /** Source fhir client. */
  private IGenericClient sourceFhirServer;

  /** Fhir export interface. */
  private FhirExportInterface fhirExportInterface;

  FhirComponent() {}

  @PostConstruct
  private void setup() {
    configuration.getCtx().getRestfulClientFactory().setSocketTimeout(300 * 1000);

    this.transferController = new FhirTransfer(configuration.getCtx(), mapper);
  }

  /** Returns source fhir client. */
  public IGenericClient getSourceFhirServer() {

    if (Objects.nonNull(sourceFhirServer)) {
      return this.sourceFhirServer;
    }

    String sourceFhirServer =
        Objects.nonNull(overrideConfig.get("sourceServer"))
            ? overrideConfig.get("sourceServer")
            : configuration.getSourceFhirServer();

    FhirClient sourceClient = new FhirClient(configuration.getCtx(), sourceFhirServer);
    if (Objects.nonNull(configuration.getSourceFhirServerUsername())
        && Objects.nonNull(configuration.getSourceFhirServerPassword())) {
      sourceClient.setBasicAuth(
          configuration.getSourceFhirServerUsername(), configuration.getSourceFhirServerPassword());
    }
    log.info("Start collecting Resources from FHIR server " + sourceFhirServer);
    return sourceClient.getClient();
  }

  /** Returns fhir export interface. */
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
