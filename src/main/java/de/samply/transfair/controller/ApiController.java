package de.samply.transfair.controller;

import de.samply.transfair.models.Modes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Rest Endpoints for Transfair. */
@RestController
public class ApiController {

  private static final Logger log = LoggerFactory.getLogger(ApiController.class);

  @Autowired TransferController transferController;

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
    log.info("Running TransFAIR in BBMRI2BBMRI mode");
    long startTime = System.currentTimeMillis();
    transferController.bbmri2bbmri();
    long endTime = System.currentTimeMillis() - startTime;
    log.info("Finished syncing BBMRI2BBMRI in " + endTime + " mil sec");
    return "Status: ok; Time: " + endTime;
  }

  /** Rest Endpoint for converting bbmri.de to mii profiles and transfer it to another fhir sever */
  @GetMapping("/v1/fhir/bbmri2mii")
  public String bbmri2mii() throws Exception {
    log.info("Running TransFAIR in BBMRI2MII mode");
    long startTime = System.currentTimeMillis();
    transferController.bbmri2mii();
    long endTime = System.currentTimeMillis() - startTime;
    log.info("Finished syncing BBMRI2MII in " + endTime + " mil sec");
    return "Status: ok; Time: " + endTime;
  }

  /** Rest Endpoint for converting mii to bbmri.de profiles and transfer it to another fhir sever */
  @GetMapping("/v1/fhir/mii2bbmri")
  public String mii2bbmri() throws Exception {
    log.info("Running TransFAIR in MII2BBMRI mode");
    long startTime = System.currentTimeMillis();
    transferController.mii2bbmri();
    long endTime = System.currentTimeMillis() - startTime;
    log.info("Finished syncing MII2BBMRI in " + endTime + " mil sec");
    return "Status: ok; Time: " + endTime;
  }

  /** Rest Endpoint for extracting bbmri.de specimens transfer it to a ccp fhir server */
  @GetMapping("/v1/fhir/bbmri2dktk")
  public String bbmri2dktk() throws Exception {
    log.info("Running TransFAIR in BBMRI2DKTK mode");
    long startTime = System.currentTimeMillis();
    TransferController transferController = new TransferController();
    transferController.bbmri2dktk();
    long endTime = System.currentTimeMillis() - startTime;
    log.info("Finished syncing BBMRI2DKTK in " + endTime + " mil sec");
    return "Status: ok; Time: " + endTime;
  }
}
