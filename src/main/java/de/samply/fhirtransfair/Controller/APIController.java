package de.samply.fhirtransfair.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class APIController {

  @Autowired
  TransferController transferController;

  @GetMapping("/")
  public String overview() {
    return "<html><body><h1>TransFAIR</h1></body></html>";
  }

  @GetMapping("/health")
  public String health() {
    return "ok";
  }

  @GetMapping("/sync")
  public String sync() {
    this.transferController.transfer();

    return "ok";
  }
}
