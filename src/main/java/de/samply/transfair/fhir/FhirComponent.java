package de.samply.transfair.fhir;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import de.samply.transfair.Configuration;
import de.samply.transfair.converters.IdMapper;
import de.samply.transfair.fhir.clients.FhirClient;
import de.samply.transfair.fhir.writers.FhirExportInterface;
import de.samply.transfair.fhir.writers.FhirFileSaver;
import de.samply.transfair.fhir.writers.FhirServerSaver;

import jakarta.annotation.PostConstruct;
import java.util.Objects;
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

  /** transferController. */
  public FhirTransfer transferController;

  /** Source fhir client. */
  private FhirClient sourceFhirServer;

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
      return this.sourceFhirServer.getClient();
    }

    sourceFhirServer = new FhirClient(configuration.getCtx(), configuration.getSourceFhirServer());
    setAuth(
        sourceFhirServer,
        configuration.getSourceFhirServerUsername(),
        configuration.getSourceFhirServerPassword());
    log.info("Start collecting Resources from FHIR server " + configuration.getSourceFhirServer());

    return sourceFhirServer.getClient();
  }

  private void setAuth(FhirClient sourceClient, String user, String password) {
    if (!user.isBlank() && !password.isBlank()) {
      sourceClient.setBasicAuth(
          configuration.getSourceFhirServerUsername(), configuration.getSourceFhirServerPassword());
    }
  }

  /** Returns fhir export interface. */
  public FhirExportInterface getFhirExportInterface() {
    if (Objects.nonNull(fhirExportInterface)) {
      return fhirExportInterface;
    }

    if (configuration.isSaveToFileSystem()) {
      this.fhirExportInterface = new FhirFileSaver(configuration.getCtx());
    } else {
      FhirServerSaver fhirServerSaver =
          new FhirServerSaver(configuration.getCtx(), configuration.getTargetFhirServer());
      setAuth(
          fhirServerSaver.getClient(),
          configuration.getTargetFhirServerUsername(),
          configuration.getTargetFhirServerPassword());
      log.info("Start exporting resources to FHIR server " + sourceFhirServer);
      fhirExportInterface = fhirServerSaver;
    }

    return fhirExportInterface;
  }
}
