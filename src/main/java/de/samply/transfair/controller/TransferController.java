package de.samply.transfair.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import de.samply.transfair.converters.IDMapper;
import de.samply.transfair.converters.Resource_Type;
import de.samply.transfair.models.ProfileFormats;
import de.samply.transfair.resources.CauseOfDeath;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Specimen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** This class has most of the transformation and converting logic. */
@Component
public class TransferController {

  private static final Logger log = LoggerFactory.getLogger(TransferController.class);

  // Variables for Source

  private ProfileFormats sourceFormat;

  @Value("${app.source.loadFromFhirServer}")
  private boolean loadFromFhirServer;

  @Value("${app.source.fhirserver}")
  private String sourceFhirserver;

  @Value("${app.source.loadFromFileSystem}")
  private boolean loadFromFileSystem;

  @Value("${app.source.startResource}")
  private String startResource;

  @Autowired IDMapper idMapper;

  private List<String> resources;

  // Variables for target
  private ProfileFormats targetFormat;

  @Value("${app.target.saveToFhirServer}")
  private boolean saveFromFhirServer;

  @Value("${app.target.fhirServer}")
  private String targetFhirServer;

  @Value("${app.target.saveToFileSystem}")
  private boolean saveToFileSystem;

  @Value("${app.source.resourceFilter}")
  String resourcesFilter;

  TransferController() throws Exception {
    ctx.getRestfulClientFactory().setSocketTimeout(300 * 1000);
  }

  @PostConstruct
  private void setup() {
    if (resourcesFilter.isEmpty()) {
      this.resources = List.of("Patient", "Specimen", "Condition", "Observation");
    } else {
      this.resources = Arrays.stream(resourcesFilter.split(",")).toList();
    }
  }

  private final FhirContext ctx = FhirContext.forR4();

  private List<IBaseResource> fetchSpecimenResources(IGenericClient client) {

    List<IBaseResource> resourceList = new ArrayList<>();

    // Search
    Bundle bundle =
        client.search().forResource(Specimen.class).returnBundle(Bundle.class).count(500).execute();
    resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));

    // Load the subsequent pages
    while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
      bundle = client.loadPage().next(bundle).execute();
      resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));
      log.debug("Fetching next page of Specimen");
    }
    log.info("Loaded " + resourceList.size() + " Specimen Resources from source");

    return resourceList;
  }

  private HashSet<String> fetchPatientIds(IGenericClient client) {
    if (Objects.equals(startResource, "Specimen")) {
      return this.getSpecimenPatients(client);
    } else {
      return this.getPatientRefs(client);
    }
  }

  private <T extends IBaseResource> List<T> fetchResources(
      Class<T> resourceType, IGenericClient client) {
    // Search
    Bundle bundle =
        client.search().forResource(resourceType).returnBundle(Bundle.class).count(500).execute();
    List<T> resourceList =
        new ArrayList<>(BundleUtil.toListOfResourcesOfType(ctx, bundle, resourceType));

    // Load the subsequent pages
    while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
      bundle = client.loadPage().next(bundle).execute();
      resourceList.addAll(BundleUtil.toListOfResourcesOfType(ctx, bundle, resourceType));
      log.debug("Fetching next page of " + resourceType.getName());
    }
    log.info(
        "Loaded " + resourceList.size() + " " + resourceType.getName() + " Resources from source");

    return resourceList;
  }

  private Patient convertPatientResource(Patient p, String patientId) throws Exception {
    de.samply.transfair.resources.Patient ap = new de.samply.transfair.resources.Patient();

    if (this.sourceFormat == ProfileFormats.BBMRI) {
      log.debug("Analysing patient " + patientId + " with format bbmri.de");
      ap.fromBbmri(p);
    } else {
      log.debug("Analysing patient " + patientId + " with format mii");
      ap.fromMii(p);
    }

    if (this.targetFormat == ProfileFormats.BBMRI) {
      log.debug("Analysing patient " + patientId + " with format bbmri.de");

      if (this.sourceFormat != this.targetFormat) {
        ap.setBbmriId(idMapper.toBbmri(ap.getMiiId(), Resource_Type.PATIENT));
      }
      return ap.toBbmri();
    } else {
      log.debug("Analysing patient " + patientId + " with format mii");
      if (!Objects.equals(this.sourceFormat, this.targetFormat)) {
        ap.setMiiId(idMapper.toMii(ap.getBbmriId(), Resource_Type.PATIENT));
      }
      return ap.toMii();
    }
  }

  private Patient fetchPatientResource(IGenericClient client, String patientId) {
    return client.read().resource(Patient.class).withId(patientId).execute();
  }

  private List<Specimen> convertBbmriSpecimenResources(List<Specimen> resourceList) {
    List<Specimen> resourceListOut = new ArrayList<>();

    for (Specimen specimen : resourceList) {
      de.samply.transfair.resources.Specimen transferSpecimen =
          new de.samply.transfair.resources.Specimen();
      log.debug("Analysing Specimen " + specimen.getId() + " with format bbmri.de");
      transferSpecimen.fromBbmri(specimen);

      log.debug("Analysing Specimen " + specimen.getId() + " with format bbmri.de");
      resourceListOut.add(transferSpecimen.toBbmri());
    }

    return resourceListOut;
  }

  private List<Specimen> convertMiiSpecimenResources(
      List<Specimen> resourceList, List<IBaseResource> conditions) {
    List<Specimen> resourceListOut = new ArrayList<>();

    for (Specimen specimen : resourceList) {
      de.samply.transfair.resources.Specimen transferSpecimen =
          new de.samply.transfair.resources.Specimen();
      transferSpecimen.fromMii(specimen);

      String code = transferSpecimen.getDiagnosisICD10Gm();

      for (IBaseResource baseResource : conditions) {
        Condition condition = (Condition) baseResource;
        Optional<Coding> filteredCondition =
            condition.getCode().getCoding().stream()
                .filter(
                    c -> {
                      return Objects.equals(
                              c.getSystem(), "http://fhir.de/StructureDefinition/CodingICD10GM")
                          && Objects.equals(c.getCode(), code);
                    })
                .findFirst();
        if (filteredCondition.isPresent()) {
          transferSpecimen.setMiiConditionRef(condition.getId());
        }
      }

      log.debug("Analysing Specimen " + specimen.getId() + " with format mii");
      resourceListOut.add(transferSpecimen.toMii());
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

  private List<IBaseResource> fetchOrganizations(IGenericClient client) {
    List<IBaseResource> resourceList = new ArrayList<>();

    Bundle bundle =
        client.search().forResource(Organization.class).returnBundle(Bundle.class).execute();

    resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));

    while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
      bundle = client.loadPage().next(bundle).execute();
      resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));
    }
    return resourceList;
  }

  private List<IBaseResource> fetchOrganizationAffiliation(IGenericClient client) {
    List<IBaseResource> resourceList = new ArrayList<>();

    Bundle bundle =
        client
            .search()
            .forResource(OrganizationAffiliation.class)
            .returnBundle(Bundle.class)
            .execute();

    resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));

    while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
      bundle = client.loadPage().next(bundle).execute();
      resourceList.addAll(BundleUtil.toListOfResources(ctx, bundle));
    }
    return resourceList;
  }

  private List<IBaseResource> convertObservations(List<IBaseResource> observations) {
    List<IBaseResource> resourceListOut = new ArrayList<>();

    for (IBaseResource base : observations) {
      Observation observation = (Observation) base;
      de.samply.transfair.resources.CheckResources checkResources =
          new de.samply.transfair.resources.CheckResources();

      if (this.sourceFormat == ProfileFormats.BBMRI) {
        if (checkResources.checkBbmriCauseOfDeath(observation)) {
          CauseOfDeath causeOfDeath = new CauseOfDeath();
          causeOfDeath.fromBbmri(observation);
          log.debug("Analysing Cause of Death " + observation.getId() + " with format bbmri");

          if (this.targetFormat == ProfileFormats.BBMRI) {
            resourceListOut.add(causeOfDeath.toBbmri());
            log.debug("Analysing Cause of Death " + observation.getId() + " with format bbmri");

          } else {
            resourceListOut.add(causeOfDeath.toMii());
            log.debug("Analysing Cause of Death " + observation.getId() + " with format mii");
          }
        }
      }
    }

    return resourceListOut;
  }

  private List<IBaseResource> fetchPatientObservation(IGenericClient client, String patientId) {
    List<IBaseResource> resourceList = new ArrayList<>();

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

    return resourceList;
  }

  private List<IBaseResource> convertConditions(List<IBaseResource> conditions) {
    List<IBaseResource> resourceListOut = new ArrayList<>();

    for (IBaseResource base : conditions) {
      Condition condition = (Condition) base;

      de.samply.transfair.resources.CheckResources checkResources =
          new de.samply.transfair.resources.CheckResources();

      if (checkResources.checkMiiCauseOfDeath(condition)
          && this.sourceFormat == ProfileFormats.MII) {
        CauseOfDeath causeOfDeath = new CauseOfDeath();
        causeOfDeath.fromMii(condition);
        log.debug("Analysing Cause of Death " + condition.getId() + " with format mii");

        if (this.targetFormat == ProfileFormats.BBMRI) {
          resourceListOut.add(causeOfDeath.toBbmri());
          log.debug("Exporting Cause of Death " + condition.getId() + " with format bbmri");

        } else {
          resourceListOut.add(causeOfDeath.toMii());
          log.debug("Exporting Cause of Death " + condition.getId() + " with format mii");
        }
        continue;
      }

      de.samply.transfair.resources.Condition conditionConverter =
          new de.samply.transfair.resources.Condition();

      if (this.sourceFormat == ProfileFormats.BBMRI) {
        conditionConverter.fromBbmri(condition);
      } else {
        conditionConverter.fromMii(condition);
      }

      if (this.targetFormat == ProfileFormats.BBMRI) {
        resourceListOut.add(conditionConverter.toBbmri());
        log.debug("Exporting Condition " + condition.getId() + " with format bbmri");

      } else {
        resourceListOut.add(conditionConverter.toMii());
        log.debug("Exporting Condition " + condition.getId() + " with format mii");
      }
    }

    return resourceListOut;
  }

  private List<IBaseResource> fetchPatientCondition(IGenericClient client, String patientId) {
    List<IBaseResource> resourceList = new ArrayList<>();

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

    return resourceList;
  }

  public void bbmri2bbmri() throws Exception {
    log.info("Running TransFAIR in BBMRI2BBMRI mode");
    this.setup();

    this.sourceFormat = ProfileFormats.BBMRI;
    this.targetFormat = ProfileFormats.BBMRI;

    if (loadFromFhirServer) {
      log.info("Start collecting Resources from FHIR server " + sourceFhirserver);
      IGenericClient sourceClient = ctx.newRestfulGenericClient(sourceFhirserver);

      // TODO: Collect Organization and Collection

      log.info("Loaded all Patient ID's");

      this.buildResources(this.fetchOrganizations(sourceClient));
      this.buildResources(this.fetchOrganizationAffiliation(sourceClient));

      int counter = 1;

      HashSet<String> patientRefs = getSpecimenPatients(sourceClient);

      for (String pid : patientRefs) {
        List<IBaseResource> patientResources = new ArrayList<>();
        log.debug("Loading data for patient " + pid);

        if (resources.contains("Patient")) {
          patientResources.add(fetchPatientResource(sourceClient, pid));
        }
        if (resources.contains("Specimen")) {
          patientResources.addAll(fetchPatientSpecimens(sourceClient, pid));
        }
        if (resources.contains("Observation")) {
          patientResources.addAll(fetchPatientObservation(sourceClient, pid));
        }
        if (resources.contains("Condition")) {
          patientResources.addAll(fetchPatientCondition(sourceClient, pid));
        }

        this.buildResources(patientResources);
        log.info("Exported Resources " + counter++ + "/" + patientRefs.size());
      }

    } else {
      log.info("Not supported currently, please stand by");
    }
  }

  public void bbmri2mii() throws Exception {
    this.sourceFormat = ProfileFormats.BBMRI;
    this.targetFormat = ProfileFormats.MII;
    this.setup();

    if (loadFromFhirServer) {
      log.info("Start collecting Resources from FHIR server " + sourceFhirserver);
      IGenericClient sourceClient = ctx.newRestfulGenericClient(sourceFhirserver);

      HashSet<String> patientIds = fetchPatientIds(sourceClient);

      log.info("Loaded all " + patientIds.size() + " Patients");

      int counter = 1;

      for (String pid : patientIds) {
        List<IBaseResource> patientResources = new ArrayList<>();
        log.debug("Loading data for patient " + pid);

        if (resources.contains("Patient")) {
          patientResources.add(
              convertPatientResource(fetchPatientResource(sourceClient, pid), pid));
        }
        List<IBaseResource> conditions = null;
        if (resources.contains("Condition")) {
          conditions.addAll(convertConditions(fetchPatientCondition(sourceClient, pid)));
          patientResources.addAll(conditions);
        }
        if (resources.contains("Specimen")) {
          patientResources.addAll(
              convertMiiSpecimenResources(fetchPatientSpecimens(sourceClient, pid), conditions));
        }
        if (resources.contains("Observation")) {
          patientResources.addAll(convertObservations(fetchPatientObservation(sourceClient, pid)));
        }

        this.buildResources(patientResources);
        log.info("Exported Resources " + counter++ + "/" + patientIds.size());
      }
    } else {
      log.info("Not ready currently");
    }
  }

  public void mii2bbmri() throws Exception {
    this.sourceFormat = ProfileFormats.MII;
    this.targetFormat = ProfileFormats.BBMRI;
    this.setup();

    if (loadFromFhirServer) {
      log.info("Start collecting Resources from FHIR server " + sourceFhirserver);
      IGenericClient sourceClient = ctx.newRestfulGenericClient(sourceFhirserver);

      HashSet<String> patientIds = fetchPatientIds(sourceClient);

      log.info("Loaded all " + patientIds.size() + " Patients");

      int counter = 1;

      for (String pid : patientIds) {
        List<IBaseResource> patientResources = new ArrayList<>();
        log.debug("Loading data for patient " + pid);

        if (resources.contains("Patient")) {
          patientResources.add(
              convertPatientResource(fetchPatientResource(sourceClient, pid), pid));
        }
        if (resources.contains("Specimen")) {
          patientResources.addAll(
              convertBbmriSpecimenResources(fetchPatientSpecimens(sourceClient, pid)));
        }
        if (resources.contains("Observation")) {
          patientResources.addAll(convertObservations(fetchPatientObservation(sourceClient, pid)));
        }
        if (resources.contains("Condition")) {
          patientResources.addAll(convertConditions(fetchPatientCondition(sourceClient, pid)));
        }

        this.buildResources(patientResources);
        log.info("Exported Resources " + counter++ + "/" + patientIds.size());
      }
    } else {
      log.info("Not ready currently");
    }
  }

  public void bbmri2dktk() {
    this.sourceFormat = ProfileFormats.BBMRI;
    this.targetFormat = ProfileFormats.DKTK;
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
    List<Patient> patients = fetchResources(Patient.class, sourceClient);
    HashSet<String> patientRefs = new HashSet<>();

    for (IBaseResource patient : patients) {
      patientRefs.add(patient.getIdElement().getValue());
    }
    return patientRefs;
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

    if (this.saveToFileSystem) {
      exportToFileSystem(bundleOut);
    }

    if (this.saveFromFhirServer) {
      exportToFhirServer(bundleOut);
    }
  }

  public boolean exportToFhirServer(Bundle bundle) {

    IGenericClient clientTarget = ctx.newRestfulGenericClient(this.targetFhirServer);
    clientTarget.transaction().withBundle(bundle).execute();
    return true;
  }

  public boolean exportToFileSystem(Bundle bundle) {

    String output = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    try {
      FileWriter myWriter = new FileWriter(bundle.getId() + ".json");
      myWriter.write(output);
      myWriter.close();
    } catch (IOException e) {
      log.error("An error occurred while writing output to file.");
      e.printStackTrace();
    }

    return true;
  }
}
