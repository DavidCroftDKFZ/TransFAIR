package de.samply.transfair;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Configuration {

  @Value("${SOURCEFHIRSERVER}")
  private String sourceFhirServer;

  @Value("${STARTRESOURCE}")
  private String startResource;

  @Value("${RESOURCEFILTER}")
  private
  String resourcesFilter;

  @Value("${TARGETFHIRSERVER}")
  private String targetFhirServer;

  @Value("${SAVETOFILESYSTEM}")
  private boolean saveToFileSystem;

  @Value("${PSEUDOCSVFILE}")
  private String csvFileName;

  public String getSourceFhirServer() {
    return sourceFhirServer;
  }

  public void setSourceFhirServer(String sourceFhirServer) {
    this.sourceFhirServer = sourceFhirServer;
  }

  public String getStartResource() {
    return startResource;
  }

  public void setStartResource(String startResource) {
    this.startResource = startResource;
  }

  public String getResourcesFilter() {
    return resourcesFilter;
  }

  public void setResourcesFilter(String resourcesFilter) {
    this.resourcesFilter = resourcesFilter;
  }

  public boolean isSaveToFileSystem() {
    return saveToFileSystem;
  }

  public void setSaveToFileSystem(boolean saveToFileSystem) {
    this.saveToFileSystem = saveToFileSystem;
  }

  public String getCsvFileName() {
    return csvFileName;
  }

  public void setCsvFileName(String csvFileName) {
    this.csvFileName = csvFileName;
  }

  public String getTargetFhirServer() {
    return targetFhirServer;
  }

  public void setTargetFhirServer(String targetFhirServer) {
    this.targetFhirServer = targetFhirServer;
  }
}
