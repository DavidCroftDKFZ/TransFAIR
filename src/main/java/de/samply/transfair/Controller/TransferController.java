package de.samply.transfair.Controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import de.samply.transfair.resources.CauseOfDeath;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Specimen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TransferController {

  private static final Logger log = LoggerFactory.getLogger(TransferController.class);

  // Variables for Source
  @Value("${app.source.format}")
  private String sourceFormat;

  @Value("${app.source.loadFromFhirServer}")
  private boolean loadFromFhirServer;

  @Value("${app.source.fhirserver}")
  private String sourceFhirserver;

  @Value("${app.source.loadFromFileSystem}")
  private boolean loadFromFileSystem;

  //@Value("${app.source.pathToFhirResources}")
  //private boolean pathToFhirResources;

  // Variables for target
  @Value("${app.target.format}")
  private String targetFormat;

  @Value("${app.target.saveToFhirServer}")
  private boolean saveFromFhirServer;

  @Value("${app.target.fhirServer}")
  private String targetFhirServer;

  @Value("${app.target.saveToFileSystem}")
  private boolean saveToFileSystem;


  TransferController() {
    ctx.getRestfulClientFactory().setSocketTimeout(300 * 1000);
  }

  private FhirContext ctx = FhirContext.forR4();


  private List<IBaseResource> fetchSpecimenResources(IGenericClient client) {

    List<IBaseResource> resourceList = new ArrayList<>();

    // Search
    Bundle bundle =
        client.search().forResource(Specimen.class).returnBundle(Bundle.class).execute();
    resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));

    // Load the subsequent pages
    while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
      bundle = client.loadPage().next(bundle).execute();
      resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));
      log.info("Fetching next page of Specimen");

    }
    log.info("Loaded " + resourceList.size() + " Specimen Resources from source");

    return resourceList;
  }

  private Patient fetchPatientResource(IGenericClient client, String patientId) {
    Patient p = client.read().resource(Patient.class).withId(patientId).execute();

    de.samply.transfair.resources.Patient ap = new de.samply.transfair.resources.Patient();

    if (Objects.equals(this.sourceFormat, "bbmri.de")) {
      log.debug("Analysing patient " + patientId + " with format bbmri.de");
      ap.fromBbmri(p);
    } else {
      log.debug("Analysing patient " + patientId + " with format mii");
      ap.fromMii(p);
    }

    if (Objects.equals(this.targetFormat, "bbmri.de")) {
      log.debug("Analysing patient " + patientId + " with format bbmri.de");
      return ap.toBbmri();
    } else {
      log.debug("Analysing patient " + patientId + " with format mii");
      return ap.toMii();
    }
  }

  private List<Specimen> fetchPatientSpecimens(IGenericClient client, String patientId) {
    List<IBaseResource> resourceList = new ArrayList<>();
    List<Specimen> resourceListOut = new ArrayList<>();

    Bundle bundle =
        client
            .search()
            .forResource(Specimen.class)
            .where(Specimen.SUBJECT.hasId(patientId))
            .returnBundle(Bundle.class)
            .execute();

    resourceList.addAll(BundleUtil.toListOfResources(ctx,bundle));

    while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
      bundle = client.loadPage().next(bundle).execute();
      resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));
    }

    for (IBaseResource base : resourceList) {
      Specimen specimen = (Specimen) base;
      de.samply.transfair.resources.Specimen s =
          new de.samply.transfair.resources.Specimen();
      if (Objects.equals(this.sourceFormat, "bbmri.de")) {
        log.debug("Analysing Specimen " + patientId + " with format bbmri.de");
        s.fromBbmri(specimen);
      } else {
        log.debug("Analysing Specimen " + patientId + " with format mii");
        s.fromMii(specimen);
      }

      if (Objects.equals(this.targetFormat, "bbmri.de")) {
        log.debug("Analysing Specimen " + patientId + " with format bbmri.de");
        resourceListOut.add(s.toBbmri());
      } else {
        log.debug("Analysing Specimen " + patientId + " with format mii");
        resourceListOut.add(s.toMii());
      }
    }

    return resourceListOut;
  }

  private List<IBaseResource> fetchPatientObservation(IGenericClient client, String patientId) {
    List<IBaseResource> resourceList = new ArrayList<>();
    List<IBaseResource> resourceListOut = new ArrayList<>();

    Bundle bundle =
        client
            .search()
            .forResource(Observation.class)
            .where(Observation.SUBJECT.hasId(patientId))
            .returnBundle(Bundle.class)
            .execute();

    resourceList.addAll(BundleUtil.toListOfResources(ctx,bundle));

    while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
      bundle = client.loadPage().next(bundle).execute();
      resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));
    }

    for (IBaseResource base : resourceList) {
      Observation observation = (Observation) base;
      de.samply.transfair.resources.CheckResources checkResources =
          new de.samply.transfair.resources.CheckResources();

      if (Objects.equals(this.sourceFormat, "bbmri.de")) {
        if (checkResources.checkBbmriCauseOfDeath(observation)) {
          CauseOfDeath causeOfDeath = new CauseOfDeath();
          causeOfDeath.fromBbmri(observation);
          log.debug("Analysing Cause of Death " + patientId + " with format bbmri");

          if (Objects.equals(this.targetFormat, "bbmri.de")) {
            resourceListOut.add(causeOfDeath.toBbmri());
            log.debug("Analysing Cause of Death " + patientId + " with format bbmri");

          } else {
            resourceListOut.add(causeOfDeath.toMii());
            log.debug("Analysing Cause of Death " + patientId + " with format mii");

          }
        }
      }
    }

    return resourceListOut;
  }

  private List<IBaseResource> fetchPatientCondition(IGenericClient client, String patientId) {
    List<IBaseResource> resourceList = new ArrayList<>();
    List<IBaseResource> resourceListOut = new ArrayList<>();

    Bundle bundle =
        client
            .search()
            .forResource(Condition.class)
            .where(Condition.SUBJECT.hasId(patientId))
            .returnBundle(Bundle.class)
            .execute();

    resourceList.addAll(BundleUtil.toListOfResources(ctx,bundle));

    while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
      bundle = client.loadPage().next(bundle).execute();
      resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));
    }

    for (IBaseResource base : resourceList) {
      Condition c = (Condition) base;

      de.samply.transfair.resources.CheckResources checkResources =
          new de.samply.transfair.resources.CheckResources();

      if (checkResources.checkMiiCauseOfDeath(c) && Objects.equals(this.sourceFormat, "mii")) {
        CauseOfDeath causeOfDeath = new CauseOfDeath();
        causeOfDeath.fromMii(c);
        log.debug("Analysing Cause of Death " + patientId + " with format mii");

        if (Objects.equals(this.targetFormat, "bbmri.de")) {
          resourceListOut.add(causeOfDeath.toBbmri());
          log.debug("Exporting Cause of Death " + patientId + " with format bbmri");

        } else {
          resourceListOut.add(causeOfDeath.toMii());
          log.debug("Exporting Cause of Death " + patientId + " with format mii");

        }
        continue;
      }

      de.samply.transfair.resources.Condition condition =
          new de.samply.transfair.resources.Condition();

      if (Objects.equals(this.sourceFormat, "bbmri.de")) {
        condition.fromBbmri(c);
      } else {
        condition.fromMii(c);
      }

      if (Objects.equals(this.targetFormat, "bbmri.de")) {
        resourceListOut.add(condition.toBbmri());
        log.debug("Exporting Condition " + patientId + " with format bbmri");

      } else {
        resourceListOut.add(condition.toMii());
        log.debug("Exporting Condition " + patientId + " with format mii");

      }
    }

    return resourceListOut;
  }

  public void transfer() {
    log.info("Collecting Resources from Source in " + sourceFormat + " format");

    HashSet<String> patientRefs = new HashSet<>();
    List<IBaseResource> specimens;

    if (loadFromFhirServer) {
      log.info("Start collecting Resources from FHIR server " + sourceFhirserver);
      IGenericClient sourceClient = ctx.newRestfulGenericClient(sourceFhirserver);

      log.info("FHIR Server connected");

      specimens = fetchSpecimenResources(sourceClient);

      for (IBaseResource specimen : specimens) {
        Specimen s = (Specimen) specimen;
        patientRefs.add(s.getSubject().getReference());
      }

      log.info("Loaded all Patient ID's");

      for (String p_id : patientRefs) {
        List<IBaseResource> patientResources = new ArrayList<>();
        log.debug("Loading data for patient " + p_id);

        patientResources.add(fetchPatientResource(sourceClient, p_id));
        patientResources.addAll(fetchPatientSpecimens(sourceClient, p_id));
        patientResources.addAll(fetchPatientObservation(sourceClient, p_id));
        patientResources.addAll(fetchPatientCondition(sourceClient, p_id));

        this.buildResources(patientResources);

        if (this.saveFromFhirServer) {}
      }

    } else if (this.loadFromFileSystem) {
      log.info("Unsupported");
    } else {
      log.error("Please provide either a FHIR Server or files in fhir format");
    }
  }

  public void buildResources(List<IBaseResource> resources) {
    Bundle bundleOut = new Bundle();
    bundleOut.setId(String.valueOf(UUID.randomUUID()));
    bundleOut.setType(Bundle.BundleType.TRANSACTION);

    try {
      for (IBaseResource resource : resources) {
        bundleOut
            .addEntry()
            .setFullUrl(resource.getIdElement().getValue())
            .setResource((Resource) resource)
            .getRequest()
            .setUrl(
                ((Resource) resource).getResourceType() + "/" + resource.getIdElement().getIdPart())
            .setMethod(HTTPVerb.PUT);
      }

    } catch (Error e) {
      System.out.println(e.getMessage());
    }

    if (this.saveToFileSystem) exportToFileSystem(bundleOut);

    if (this.saveFromFhirServer) exportToFhirServer(bundleOut);
  }

  public boolean exportToFhirServer(Bundle bundle) {

    IGenericClient clientTarget = ctx.newRestfulGenericClient(this.targetFhirServer);
    clientTarget.transaction().withBundle(bundle).execute();
    log.info("Post transformed patient");

    return true;
  }

  public boolean exportToFileSystem(Bundle bundle) {

    String output = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    try {
      FileWriter myWriter = new FileWriter(bundle.getId() + ".json");
      myWriter.write(output);
      myWriter.close();
      System.out.println("Successfully wrote output to file.");
    } catch (IOException e) {
      System.out.println("An error occurred while writing output to file.");
      e.printStackTrace();
    }

    return true;
  }
}
