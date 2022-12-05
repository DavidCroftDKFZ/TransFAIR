package de.samply.transfair.controller;

import de.samply.transfair.Configuration;
import de.samply.transfair.mappings.Bbmri2Bbmri;
import de.samply.transfair.mappings.Bbmri2Mii;
import de.samply.transfair.mappings.Mii2Bbmri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Rest Endpoints for Transfair. */
@RestController
public class ApiController {

  private static final Logger log = LoggerFactory.getLogger(ApiController.class);

  @Autowired Bbmri2Bbmri bbmri2Bbmri;

  @Autowired Bbmri2Mii bbmri2Mii;

  @Autowired Mii2Bbmri mii2Bbmri;

  @Autowired Configuration configuration;

  @GetMapping("/")
  public String overview() {
    return "<html><body><h1>TransFAIR</h1></body></html>";
  }

  @GetMapping("/health")
  public String health() {
    return "ok";
  }

  /** Rest Endpoint for transferring bbmri.de data from one fhir store to another. */
  @GetMapping("/v1/fhir/bbmri2bbmri")
  public String bbmri2bbmri() throws Exception {
    long startTime = System.currentTimeMillis();
    bbmri2Bbmri.bbmri2bbmri();
    long endTime = System.currentTimeMillis() - startTime;
    log.info("Finished syncing BBMRI2BBMRI in " + endTime + " mil sec");
    return "Status: ok; Time: " + endTime;
  }

  /** Rest Endpoint for converting bbmri.de to mii profiles and transfer it to another fhir sever */
  @GetMapping("/v1/fhir/bbmri2mii")
  public String bbmri2mii() throws Exception {
    long startTime = System.currentTimeMillis();
    bbmri2Mii.bbmri2mii();
    long endTime = System.currentTimeMillis() - startTime;
    log.info("Finished syncing BBMRI2MII in " + endTime + " mil sec");
    return "Status: ok; Time: " + endTime;
  }

  /** Rest Endpoint for converting mii to bbmri.de profiles and transfer it to another fhir sever */
  @GetMapping("/v1/fhir/mii2bbmri")
  public String mii2bbmri() throws Exception {
    long startTime = System.currentTimeMillis();
    mii2Bbmri.mii2bbmri();
    long endTime = System.currentTimeMillis() - startTime;
    log.info("Finished syncing MII2BBMRI in " + endTime + " mil sec");
    return "Status: ok; Time: " + endTime;
  }

  @GetMapping("/v1/config")
  public String config() {
    String header =
        "<html><head> <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-rbsA2VBKQhggwzxH7pPCaAqO46MgnOM80zW1RWuH61DGLwZJEdK2Kadq2F9CUG65\" crossorigin=\"anonymous\"></head><body><h1>Config</h1><table class=\"table\"><tr><th>Name</th><th>Param</th></tr>";
    String footer = "</table></body></html>";
    String body = "";
    body =
        body
            + "<tr><td>Source FHIR Server</td><td>"
            + configuration.getSourceFhirServer()
            + " </td></tr>";

    body =
        body
            + "<tr><td>Resource Filter</td><td>"
            + configuration.getResourcesFilter()
            + " </td></tr>";

    body =
        body
            + "<tr><td>Target FHIR Server</td><td>"
            + configuration.getTargetFhirServer()
            + " </td></tr>";

    body =
        body
            + "<tr><td>Save To File System</td><td>"
            + configuration.isSaveToFileSystem()
            + " </td></tr>";

    body =
        body + "<tr><td>Start Resource</td><td>" + configuration.getStartResource() + " </td></tr>";
    body =
        body
            + "<tr><td>BBMRI FHIR Sever Resource</td><td>"
            + configuration.getBbmriFhirServer()
            + " </td></tr>";
    body =
        body
            + "<tr><td>MII FHIR Sever Resource</td><td>"
            + configuration.getMiiFhirServer()
            + " </td></tr>";
    body =
        body + "<tr><td>PSEUDO CSV File</td><td>" + configuration.getCsvFileName() + " </td></tr>";

    return header + body + footer;
  }
}
