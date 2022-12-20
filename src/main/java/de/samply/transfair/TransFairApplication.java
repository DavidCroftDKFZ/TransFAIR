package de.samply.transfair;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Main Application Entrypoint. */
@SpringBootApplication
@Slf4j
public class TransFairApplication {

  /**
   * Starts the program.
   *
   * @param args additional program arguments
   */
  public static void main(String[] args) {
    long startTime = System.currentTimeMillis();
    SpringApplication.run(TransFairApplication.class, args);

    long endTime = System.currentTimeMillis() - startTime;
    log.info("Finished syncing BBMRI2BBMRI in " + endTime + " mil sec");
  }
}
