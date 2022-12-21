package de.samply.transfair;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Main Application Entrypoint. */
@SpringBootApplication
@Slf4j
public class TransFairApplication implements CommandLineRunner {

  private MappingService mappingService;

  /** Loads the mapping service. */
  TransFairApplication(MappingService mappingService) {
    this.mappingService = mappingService;
  }

  /**
   * Starts the program.
   *
   * @param args additional program arguments
   */
  public static void main(String[] args) {
    long startTime = System.currentTimeMillis();
    SpringApplication.run(TransFairApplication.class, args);

    long endTime = System.currentTimeMillis() - startTime;
    log.info("Finished TransFAIR in " + endTime + " mil sec");
  }

  @Override
  public void run(String... args) throws Exception {
    log.debug("EXECUTING : command line runner");

    for (int i = 0; i < args.length; ++i) {
      log.debug("args[{}]: {}", i, args[i]);
    }

    mappingService.run();
  }
}
