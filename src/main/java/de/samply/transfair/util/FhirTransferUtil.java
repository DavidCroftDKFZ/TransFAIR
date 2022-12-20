package de.samply.transfair.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import de.samply.transfair.converters.IdMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Specimen;

/** This class has most of the transformation and converting logic. */
@Slf4j
public class FhirTransferUtil {

  IdMapper idMapper;

  FhirContext ctx;

  public FhirTransferUtil(FhirContext ctx, IdMapper idMapper) {
    this.ctx = ctx;
    this.idMapper = idMapper;
  }

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

  public HashSet<String> fetchPatientIds(IGenericClient client, String startResource) {
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

  public Patient fetchPatientResource(IGenericClient client, String patientId) {
    return client.read().resource(Patient.class).withId(patientId).execute();
  }

  public List<Specimen> fetchPatientSpecimens(IGenericClient client, String patientId) {
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

  public List<IBaseResource> fetchOrganizations(IGenericClient client) {
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

  public List<IBaseResource> fetchOrganizationAffiliation(IGenericClient client) {
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

  public List<IBaseResource> fetchPatientObservation(IGenericClient client, String patientId) {
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

  public List<IBaseResource> fetchPatientCondition(IGenericClient client, String patientId) {
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

  public HashSet<String> getSpecimenPatients(IGenericClient sourceClient) {
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

  public Bundle buildResources(List<IBaseResource> resources) {
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
      log.error(e.getMessage());
    }

    return bundleOut;
  }
}
