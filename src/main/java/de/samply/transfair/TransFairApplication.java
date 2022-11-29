package de.samply.transfair;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Main Application Entrypoint. */
@SpringBootApplication
public class TransFairApplication {

  /**
   * Starts the program.
   *
   * @param args additional program arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(TransFairApplication.class, args);
  }
}
