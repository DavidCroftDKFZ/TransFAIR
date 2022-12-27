package de.samply.transfair;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.samply.transfair.models.FilterModel;
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides Filter for ids and values.
 *
 * <p>Either Whitelist or Blacklist. Coded into JSON
 */
@Component
public class FilterService {

  @Autowired Configuration configuration;

  public FilterModel whitelist;

  public FilterModel blacklist;

  @PostConstruct
  private void processFilter() throws Exception {
    if (!configuration.getWhitelist().isBlank()) {
      ObjectMapper objectMapper = new ObjectMapper();
      whitelist = objectMapper.readValue(configuration.getWhitelist(), FilterModel.class);
    }
    if (!configuration.getBlacklist().isBlank()) {
      ObjectMapper objectMapper = new ObjectMapper();
      blacklist = objectMapper.readValue(configuration.getBlacklist(), FilterModel.class);
    }

    if (Objects.nonNull(blacklist) && Objects.nonNull(whitelist)) {
      if (!blacklist.patient.ids.isEmpty() && !whitelist.patient.ids.isEmpty()) {
        throw new Exception("Both white and blacklist patient ids are set!");
      }
    }
  }
}
