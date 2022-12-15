package de.samply.transfair.fhir.writers;

import ca.uhn.fhir.context.FhirContext;
import java.io.FileWriter;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;

@Slf4j
public class FhirFileSaver extends FhirExportInterface {

  public FhirFileSaver(FhirContext context) {
    this.ctx = context;
  }

  @Override
  public Boolean export(Bundle bundle) {

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
