package de.samply.transfair.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import de.samply.transfair.converters.IDMapper;
import de.samply.transfair.converters.Resource_Type;
import de.samply.transfair.models.Modes;
import de.samply.transfair.models.ProfileFormats;
import de.samply.transfair.resources.CauseOfDeath;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TransferController {

  private static final Logger log = LoggerFactory.getLogger(TransferController.class);

  // Variables for Source
  private Modes mode;

  private ProfileFormats sourceFormat;

  @Value("${app.source.loadFromFhirServer}")
  private boolean loadFromFhirServer;

  @Value("${app.source.fhirserver}")
  private String sourceFhirserver;

  @Value("${app.source.loadFromFileSystem}")
  private boolean loadFromFileSystem;

  @Value("${app.source.startResource}")
  private String startResource;

  @Autowired
  IDMapper idMapper;

  private List<String> resources;


  // Variables for target
  private ProfileFormats targetFormat;

  @Value("${app.target.saveToFhirServer}")
  private boolean saveFromFhirServer;

  @Value("${app.target.fhirServer}")
  private String targetFhirServer;

  @Value("${app.target.saveToFileSystem}")
  private boolean saveToFileSystem;

  TransferController(  @Value("${app.mode}") String operationMode, @Value("${app.source.resourceFilter}") String resources_filter) throws Exception {
    ctx.getRestfulClientFactory().setSocketTimeout(300 * 1000);
    this.resources = Arrays.stream(resources_filter.split(",")).toList();

    switch (operationMode) {
      case "BBMRI2BBMRI" -> {
        this.mode = Modes.BBMRI2BBRMI;
        this.sourceFormat = ProfileFormats.BBMRI;
        this.targetFormat = ProfileFormats.BBMRI;
      }
      case "BBMRI2MII" -> {
        this.mode = Modes.BBMRI2MII;
        this.sourceFormat = ProfileFormats.BBMRI;
        this.targetFormat = ProfileFormats.MII;
      }
      case "MII2BBRMI" -> {
        this.mode = Modes.MII2BBMRI;
        this.sourceFormat = ProfileFormats.MII;
        this.targetFormat = ProfileFormats.BBMRI;
      }
      case "BBMRI2DKTK" -> {
        this.mode = Modes.BBMRI2DKTK;
        this.sourceFormat = ProfileFormats.BBMRI;
        this.targetFormat = ProfileFormats.DKTK;
      }
      default -> throw new IllegalStateException("Unexpected value: " + operationMode);
    }
  }

  private final FhirContext ctx = FhirContext.forR4();

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

  private HashSet<String> fetchPatientIds(IGenericClient client) {
    if(Objects.equals(startResource, "Specimen")) {
       return this.getSpecimenPatients(client);
    } else {
      return this.getPatientRefs(client);
    }
  }

  private List<IBaseResource> fetchPatientResources(IGenericClient client) {

    List<IBaseResource> resourceList = new ArrayList<>();

    // Search
    Bundle bundle =
        client.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
    resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));

    // Load the subsequent pages
    while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
      bundle = client.loadPage().next(bundle).execute();
      resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));
      log.info("Fetching next page of Specimen");

    }
    log.info("Loaded " + resourceList.size() + " Patient Resources from source");

    return resourceList;
  }

  private Patient convertPatientResource(Patient p, String patientId) throws Exception {
    de.samply.transfair.resources.Patient ap = new de.samply.transfair.resources.Patient();

    if (Objects.equals(this.sourceFormat, ProfileFormats.BBMRI)) {
      log.debug("Analysing patient " + patientId + " with format bbmri.de");
      ap.fromBbmri(p);
    } else {
      log.debug("Analysing patient " + patientId + " with format mii");
      ap.fromMii(p);
    }

    if (Objects.equals(this.targetFormat, ProfileFormats.BBMRI)) {
      log.debug("Analysing patient " + patientId + " with format bbmri.de");

      if(!Objects.equals(this.sourceFormat, this.targetFormat)) {
        ap.setBbmriId(idMapper.toBbmri(ap.getMiiId(), Resource_Type.PATIENT));
      }
      return ap.toBbmri();
    } else {
      log.debug("Analysing patient " + patientId + " with format mii");
      if(!Objects.equals(this.sourceFormat, this.targetFormat)) {
       // ap.setMiiId(idMapper.toMii(ap.getBbmriId(), Resource_Type.PATIENT));
        ap.setMiiId(ap.getBbmriId());
      }
      return ap.toMii();
    }
  }

  private Patient fetchPatientResource(IGenericClient client, String patientId) {
    return client.read().resource(Patient.class).withId(patientId).execute();
  }

  private List<Specimen> convertSpecimenResouces(List<Specimen> resourceList) {
    List<Specimen> resourceListOut = new ArrayList<>();

    for (Specimen specimen : resourceList) {
      de.samply.transfair.resources.Specimen s = new de.samply.transfair.resources.Specimen();
      if (Objects.equals(this.sourceFormat, "bbmri.de")) {
        log.debug("Analysing Specimen " + specimen.getId() + " with format bbmri.de");
        s.fromBbmri(specimen);
      } else {
        log.debug("Analysing Specimen " + specimen.getId() + " with format mii");
        s.fromMii(specimen);
      }

      if (Objects.equals(this.targetFormat, "bbmri.de")) {
        log.debug("Analysing Specimen " + specimen.getId() + " with format bbmri.de");
        resourceListOut.add(s.toBbmri());
      } else {
        log.debug("Analysing Specimen " + specimen.getId() + " with format mii");
        resourceListOut.add(s.toMii());
      }
    }

    return resourceListOut;
  }

  private List<Specimen> fetchPatientSpecimens(IGenericClient client, String patientId) {
    List<IBaseResource> resourceList = new ArrayList<>();

    Bundle bundle =
        client
            .search()
            .forResource(Specimen.class)
            .where(Specimen.SUBJECT.hasId(patientId))
            .returnBundle(Bundle.class)
            .execute();

    resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));

    while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
      bundle = client.loadPage().next(bundle).execute();
      resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));
    }

    List<Specimen> specimens = new ArrayList<>();

    for (IBaseResource resource : resourceList) {
      specimens.add((Specimen) resource);
    }

    return specimens;
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

    resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));

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

    resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));

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

  private void bbmri2bbmri() throws Exception {
    log.info("Running TransFAIR in BBMRI2BBMRI mode");
    if (loadFromFhirServer) {
      log.info("Start collecting Resources from FHIR server " + sourceFhirserver);
      IGenericClient sourceClient = ctx.newRestfulGenericClient(sourceFhirserver);

      //TODO: Collect Organization and Collection

      HashSet<String> patientRefs = getSpecimenPatients(sourceClient);

      log.info("Loaded all Patient ID's");

      for (String p_id : patientRefs) {
        List<IBaseResource> patientResources = new ArrayList<>();
        log.debug("Loading data for patient " + p_id);

        if(resources.contains("Patient")) {
          patientResources.add(fetchPatientResource(sourceClient, p_id));
        }
        if(resources.contains("Specimen")) {
          patientResources.addAll(fetchPatientSpecimens(sourceClient, p_id));
        }
        if(resources.contains("Observation")) {
          patientResources.addAll(fetchPatientObservation(sourceClient, p_id));
        }
        if(resources.contains("Condition")) {
          patientResources.addAll(fetchPatientCondition(sourceClient, p_id));
        }

        this.buildResources(patientResources);
      }

    } else {
      log.info("Not supported currently, please stand by");
    }
  }

  private void bbmri2mii() throws Exception {

    if (loadFromFhirServer) {
      log.info("Start collecting Resources from FHIR server " + sourceFhirserver);
      IGenericClient sourceClient = ctx.newRestfulGenericClient(sourceFhirserver);

      HashSet<String> patientIds = fetchPatientIds(sourceClient);

      log.info("Loaded all " + patientIds.size() + " Patients");

      for (String p_id : patientIds) {
        List<IBaseResource> patientResources = new ArrayList<>();
        log.debug("Loading data for patient " + p_id);

        if(resources.contains("Patient")) {
          patientResources.add(convertPatientResource(fetchPatientResource(sourceClient, p_id), p_id));
        }
        if(resources.contains("Specimen")) {
          patientResources.addAll(convertSpecimenResouces(fetchPatientSpecimens(sourceClient, p_id)));
        }
        if(resources.contains("Observation")) {
          patientResources.addAll(fetchPatientObservation(sourceClient, p_id));
        }
        if(resources.contains("Condition")) {
          patientResources.addAll(fetchPatientCondition(sourceClient, p_id));
        }

        this.buildResources(patientResources);
      }
    } else {
      log.info("Not ready currently");
    }
  }

  private void mii2bbmri() {

  }

  private void bbmri2dktk() {

  }

  private HashSet<String> getSpecimenPatients(IGenericClient sourceClient) {
    List<IBaseResource> specimens = fetchSpecimenResources(sourceClient);
    HashSet<String> patientRefs = new HashSet<>();
    for (IBaseResource specimen : specimens) {
      Specimen s = (Specimen) specimen;
      patientRefs.add(s.getSubject().getReference());
    }
    return patientRefs;
  }

  private HashSet<String> getPatientRefs(IGenericClient sourceClient) {
    List<IBaseResource> patients = fetchPatientResources(sourceClient);
    HashSet<String> patientRefs = new HashSet<>();

    for (IBaseResource patient : patients) {
      Patient p = (Patient) patient;
      patientRefs.add(p.getId());
    }
    return patientRefs;
    }

  public void transfer() throws Exception {
    log.info("Running TransFAIR in " + mode + " mode");
    long startTime = System.currentTimeMillis();

    switch (mode) {
      case BBMRI2BBRMI -> {
        this.bbmri2bbmri();
      }
      case BBMRI2MII -> {
        this.bbmri2mii();
      }
      case MII2BBMRI -> {
        this.mii2bbmri();
      }
      case BBMRI2DKTK -> {
        this.bbmri2dktk();
      }
      default -> throw new IllegalStateException("Unexpected value");
    }
    log.info("Finished syncing " + mode + " in " + (System.currentTimeMillis() - startTime) + " mil sec");
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
