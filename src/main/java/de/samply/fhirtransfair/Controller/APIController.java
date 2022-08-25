package de.samply.fhirtransfair.Controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import de.samply.fhirtransfair.resources.CauseOfDeath;
import de.samply.fhirtransfair.resources.CheckResources;
import de.samply.fhirtransfair.resources.ConvertClass;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Specimen;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class APIController {

  FhirContext ctx = new FhirContext();

  @GetMapping("/")
  public String overview() {
    return "<html><body><h1>FHIR TransFAIR</h1></body></html>";
  }

  @GetMapping("/health")
  public String health() {
    return "ok";
  }

  @GetMapping("/sync")
  public String sync() {
    FhirContext ctx = FhirContext.forR4();

    //loadBlaze("http://localhost:8080/fhir", "BBMRI");
    // loadBlaze( "http://localhost:8081/fhir", "MII");

    return "ok";
  }


  @GetMapping("/check")
  public void testcheck() {

  loadBlaze("BBMRI");
  }

  private void loadBlaze(String context) {
    System.out.println("Collecting Info for " + context);

    IGenericClient Client = ctx.newRestfulGenericClient("http://localhost:8087/fhir");

    // We'll populate this list
    HashSet<String> patientRefs = new HashSet<>();
    List<IBaseResource> specimens = new ArrayList<>();

    List<ConvertClass> resources = new ArrayList<>();

    // We'll do a search for all Patients and extract the first page
    Bundle bundle =
        Client.search().forResource(Specimen.class).returnBundle(Bundle.class).execute();
    specimens.addAll(BundleUtil.toListOfResources(ctx, bundle));

    // Load the subsequent pages
    while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
      bundle = Client.loadPage().next(bundle).execute();
      specimens.addAll(BundleUtil.toListOfResources(ctx, bundle));
    }
    System.out.println("Loaded " + specimens.size() + " patients!");

    for (IBaseResource specimen : specimens) {
      Specimen s = (Specimen) specimen;
      patientRefs.add(s.getSubject().getReference());
    }

    for (String p_id : patientRefs) {
      Patient patient = Client.read().resource(Patient.class).withId(p_id).execute();
      de.samply.fhirtransfair.resources.Patient ap =
          new de.samply.fhirtransfair.resources.Patient();

      if (context == "BBMRI") {
        ap.fromBbmri(patient);
      } else {
        ap.fromMii(patient);
      }

      resources.add(ap);

      Bundle bundle4 =
          Client
              .search()
              .forResource(Specimen.class)
              .where(Specimen.SUBJECT.hasId(p_id))
              .returnBundle(Bundle.class)
              .execute();

      for (IBaseResource s : BundleUtil.toListOfResources(ctx, bundle4)) {
        Specimen s2 = (Specimen) s;
        de.samply.fhirtransfair.resources.Specimen es =
            new de.samply.fhirtransfair.resources.Specimen();
        if (context == "BBMRI") {
          es.fromBbmri(s2);
        } else {
          es.fromMii(s2);
        }
        resources.add(es);
      }

      Bundle bundle2 =
          Client
              .search()
              .forResource(Observation.class)
              .where(Observation.SUBJECT.hasId(p_id))
              .returnBundle(Bundle.class)
              .execute();

      System.out.println(
          "Conditions of Patient(" + patient.getId() + "): " + bundle2.getEntry().size());
      Bundle bundle3 =
          Client
              .search()
              .forResource(Condition.class)
              .where(Condition.SUBJECT.hasId(p_id))
              .returnBundle(Bundle.class)
              .execute();

      for (IBaseResource c : BundleUtil.toListOfResources(ctx, bundle3)) {
        Condition c2 = (Condition) c;
        de.samply.fhirtransfair.resources.Condition cs =
            new de.samply.fhirtransfair.resources.Condition();
        if (context == "BBMRI") {
          cs.fromBbmri(c2);
          resources.add(cs);
        }
      }

      Bundle bundle5 = Client.search().forResource(Condition.class).returnBundle(Bundle.class).execute();
      List<IBaseResource> conditions = new ArrayList<>(BundleUtil.toListOfResources(ctx, bundle5));

      while (bundle5.getLink(IBaseBundle.LINK_NEXT) != null) {
        bundle5 = Client.loadPage().next(bundle5).execute();
        conditions.addAll(BundleUtil.toListOfResources(ctx, bundle5));
      }

      CheckResources checkResources = new CheckResources();

      for (IBaseResource condition : conditions) {
        if (checkResources.checkMiiCauseOfDeath((Condition) condition)) {
          System.out.println("Check");

          CauseOfDeath causeOfDeath = new CauseOfDeath();
          causeOfDeath.fromMii((Condition) condition);
          resources.add(causeOfDeath);
        };
      }

      System.out.println(
          "Observations of Patient(" + patient.getId() + "): " + bundle3.getEntry().size());
    }

    uploadResources(resources);
  }

  private void uploadResources(List<ConvertClass> resources) {
    Bundle bundleOut = new Bundle();
    bundleOut.setType(Bundle.BundleType.TRANSACTION);

    try{


    for (ConvertClass convertClass: resources) {
      DomainResource resource = (DomainResource) convertClass.toMii();
      if(Objects.equals(resource,null)) {
        continue;
      }

      bundleOut.addEntry()
          .setFullUrl(resource.getIdElement().getId())
          .setResource(resource)
          .getRequest()
          .setUrl(resource.getResourceType() + "/" + resource.getIdElement().getIdPart())
          .setMethod(HTTPVerb.PUT);
    }

    }catch (Error e) {
      System.out.println(e.getMessage());
    }

    // System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundleOut));

    IGenericClient clientTarget = ctx.newRestfulGenericClient("http://localhost:8088/fhir");
    Bundle resp = clientTarget.transaction().withBundle(bundleOut).execute();

// Log the response
    System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resp));
  }
}
