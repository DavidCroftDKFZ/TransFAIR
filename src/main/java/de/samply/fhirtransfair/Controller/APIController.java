package de.samply.fhirtransfair.Controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Specimen;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class APIController {

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

    loadBlaze(ctx, "http://localhost:8080/fhir", "BBMRI");
    loadBlaze(ctx, "http://localhost:8081/fhir", "MII");

    return "ok";
  }


  private static void loadBlaze(FhirContext ctx, String serverAdress, String context) {
    System.out.println("Collecting Info for " + context);

    IGenericClient bbmriClient = ctx.newRestfulGenericClient(serverAdress);

    // We'll populate this list
    HashSet<String> patientRefs = new HashSet<>();
    List<IBaseResource> specimens = new ArrayList<>();

    // We'll do a search for all Patients and extract the first page
    Bundle bundle =
        bbmriClient.search().forResource(Specimen.class).returnBundle(Bundle.class).execute();
    specimens.addAll(BundleUtil.toListOfResources(ctx, bundle));

    // Load the subsequent pages
    while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
      bundle = bbmriClient.loadPage().next(bundle).execute();
      specimens.addAll(BundleUtil.toListOfResources(ctx, bundle));
    }
    System.out.println("Loaded " + specimens.size() + " patients!");

    for (IBaseResource specimen : specimens) {
      Specimen s = (Specimen) specimen;
      patientRefs.add(s.getSubject().getReference());
    }

    for (String p_id : patientRefs) {
      Patient patient = bbmriClient.read().resource(Patient.class).withId(p_id).execute();
      de.samply.fhirtransfair.resources.Patient ap =
          new de.samply.fhirtransfair.resources.Patient();

      if (context == "BBMRI") {
        ap.fromBBMRIPatient(patient);
      } else {
        ap.fromMIIPatient(patient);
      }

      Bundle bundle4 =
          bbmriClient
              .search()
              .forResource(Specimen.class)
              .where(Specimen.SUBJECT.hasId(p_id))
              .returnBundle(Bundle.class)
              .execute();

      for (IBaseResource s : BundleUtil.toListOfResources(ctx, bundle4)) {
        Specimen s2 = (Specimen) s;
        de.samply.fhirtransfair.resources.Specimen es =
            new de.samply.fhirtransfair.resources.Specimen();
        es.fromBBMRISpecimen(s2);
      }

      Bundle bundle2 =
          bbmriClient
              .search()
              .forResource(Observation.class)
              .where(Observation.SUBJECT.hasId(p_id))
              .returnBundle(Bundle.class)
              .execute();

      System.out.println(
          "Conditions of Patient(" + patient.getId() + "): " + bundle2.getEntry().size());
      Bundle bundle3 =
          bbmriClient
              .search()
              .forResource(Condition.class)
              .where(Condition.SUBJECT.hasId(p_id))
              .returnBundle(Bundle.class)
              .execute();

      for (IBaseResource c : BundleUtil.toListOfResources(ctx, bundle3)) {
        Condition c2 = (Condition) c;
        de.samply.fhirtransfair.resources.Condition cs =
            new de.samply.fhirtransfair.resources.Condition();
        cs.fromBBMRICondition(c2);
      }

      System.out.println(
          "Observations of Patient(" + patient.getId() + "): " + bundle3.getEntry().size());
    }
  }
}
