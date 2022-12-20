package de.samply.transfair;

import ca.uhn.fhir.context.FhirContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Environment configuration parameters. */
@Data
@Component
public class Configuration {

  private final FhirContext ctx = FhirContext.forR4();

  @Value("${SOURCEFHIRSERVER}")
  private String sourceFhirServer;

  private String sourceFhirServerUsername;

  private String sourceFhirServerPassword;

  @Value("${STARTRESOURCE}")
  private String startResource;

  @Value("${RESOURCEFILTER}")
  private String resourcesFilter;

  @Value("${TARGETFHIRSERVER}")
  private String targetFhirServer;

  private String targetFhirServerUsername;

  private String targetFhirServerPassword;

  @Value("${SAVETOFILESYSTEM}")
  private boolean saveToFileSystem;

  @Value("${PSEUDOCSVFILE}")
  private String csvFileName;
}
