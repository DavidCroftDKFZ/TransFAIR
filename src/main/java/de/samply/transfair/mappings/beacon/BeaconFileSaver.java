package de.samply.transfair.mappings.beacon;

import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 * Export objects as JSON files.
 */
@Slf4j
public class BeaconFileSaver {
  /**
   * Serialize the given data into JSON and dump into the given filename.
   *
   * @param data The object to be serialized.
   * @param pathname Path to the directory where the file should be stored.
   *                 If null or empty, use /srv/transfair.
   * @param filename Name of the file where the data will be dumped.
   */
  public static void export(Object data, String pathname, String filename) {
    BeaconFileSaver beaconFileSaver = new BeaconFileSaver();
    if (pathname == null || pathname.length() < 1) {
      pathname = "/srv/transfair";
    }
    Path path = Path.of(pathname);
    Path filepath = path.resolve(filename);
    log.info("export: filepath=" + filepath);
    try {
      FileWriter myWriter = new FileWriter(filepath.toFile());
      String output = toJson(data);
      myWriter.write(output);
      myWriter.close();
    } catch (IOException e) {
      log.error("An error occurred while writing output to file " + filepath);
      e.printStackTrace();
    }
  }

  /**
   * Serialize the given data into JSON and return as a String.
   *
   * @param data The object to be serialized.
   * @return Serialized object.
   */
  public static String toJson(Object data) {
    return new GsonBuilder().setPrettyPrinting().create().toJson(data);
  }
}