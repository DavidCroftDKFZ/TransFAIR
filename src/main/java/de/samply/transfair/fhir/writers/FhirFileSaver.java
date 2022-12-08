package de.samply.transfair.fhir.writers;

import ca.uhn.fhir.context.FhirContext;
import java.io.FileWriter;
import java.io.IOException;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FhirFileSaver extends FhirExportInterface {

  private static final Logger log = LoggerFactory.getLogger(FhirFileSaver.class);


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
