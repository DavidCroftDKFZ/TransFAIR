package de.samply.transfair.util;

import java.util.Objects;
import org.hl7.fhir.instance.model.api.IBaseResource;
import ca.uhn.fhir.context.FhirContext;

public class JsonUtils {
  
  static ca.uhn.fhir.parser.IParser parser = FhirContext.forR4().newJsonParser();
  
  public static void compareFhirObjects(IBaseResource a, IBaseResource b) {
    String actualAsJson = parser.encodeResourceToString(a);
    String expectedAsJson = parser.encodeResourceToString(b);
    assert(Objects.equals(expectedAsJson, actualAsJson));
  }

}
