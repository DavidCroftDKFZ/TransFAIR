package de.samply.transfair;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import ca.uhn.fhir.context.FhirContext;

@Slf4j
public class JsonUtils {
  
  static ca.uhn.fhir.parser.IParser parser = FhirContext.forR4().newJsonParser();
  
  public static void compareFhirObjects(IBaseResource a, IBaseResource b) {
    String actualAsJson = parser.encodeResourceToString(a);
    String expectedAsJson = parser.encodeResourceToString(b);
    assert(Objects.equals(expectedAsJson, actualAsJson));
  }

}
