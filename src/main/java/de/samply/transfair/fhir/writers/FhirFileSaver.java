package de.samply.transfair.fhir.writers;

import ca.uhn.fhir.context.FhirContext;
import de.samply.transfair.TempParams;
import java.io.FileWriter;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;

/** Interface to post data to local file system. */
@Slf4j
public class FhirFileSaver extends FhirExportInterface {

  /** Filer Saver constructor. */
  public FhirFileSaver(FhirContext context) {
    this.ctx = context;
  }

  /** export. */
  @Override
  public Boolean export(Bundle bundle) {

    String output = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    String filename = bundle.getId() + ".json";
    String path = TempParams.getSaveToFilePath();
    String filepath = path + "/" + filename;
    log.info("export: filepath=" + filepath);
    try {
      FileWriter myWriter = new FileWriter(filepath);
      myWriter.write(output);
      myWriter.close();
    } catch (IOException e) {
      log.error("An error occurred while writing output to file " + filepath);
      e.printStackTrace();
    }

    return true;
  }
}
