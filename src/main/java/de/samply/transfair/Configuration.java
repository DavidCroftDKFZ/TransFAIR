package de.samply.transfair;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Configuration {

  @Value("${SOURCEFHIRSEVER}")
  private String sourceFhirServer;

  @Value("${STARTRESOURCE}")
  private String startResource;

  @Value("${RESOURCEFILTER}")
  private
  String resourcesFilter;

  @Value("${TARGETFHIRSERVER}")
  private String targetFhirServer;

  @Value("${MIIFHIRSERVER}")
  private String miiFhirServer;

  @Value("${BBMRIFHIRSERVER}")
  private String bbmriFhirServer;

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

  public String getMiiFhirServer() {
    return miiFhirServer;
  }

  public void setMiiFhirServer(String miiFhirServer) {
    this.miiFhirServer = miiFhirServer;
  }

  public String getBbmriFhirServer() {
    return bbmriFhirServer;
  }

  public void setBbmriFhirServer(String bbmriFhirServer) {
    this.bbmriFhirServer = bbmriFhirServer;
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
