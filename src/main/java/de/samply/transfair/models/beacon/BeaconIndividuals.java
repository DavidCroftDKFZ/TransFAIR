package de.samply.transfair.models.beacon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.samply.transfair.TempParams;
import de.samply.transfair.models.beacon.BeaconIndividual;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Models a set of individuals as understood by Beacon 2.x.
 */
@Slf4j
public class BeaconIndividuals {
  private List<BeaconIndividual> individuals = new ArrayList<BeaconIndividual>();

  /**
   * Export all individuals to a JSON file.
   */
  public void export() {
    String filename = "individuals.json";
    String path = TempParams.getSaveToFilePath();
    String filepath = path + "/" + filename;
    log.info("export: filepath=" + filepath);
    try {
      FileWriter myWriter = new FileWriter(filepath);
      String output = toString();
      myWriter.write(output);
      myWriter.close();
    } catch (IOException e) {
      log.error("An error occurred while writing output to file " + filepath);
      e.printStackTrace();
    }
  }

  /**
   * Add the supplied individual to the list of individuals.
   *
   * @param beaconIndividual individual to be added.
   */
  public void add(BeaconIndividual beaconIndividual) {
    individuals.add(beaconIndividual);
  }

  /**
   * Convert the list of individuals to a JSON-formatted string.
   */
  public String toString() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    return gson.toJson(individuals);
  }
}
