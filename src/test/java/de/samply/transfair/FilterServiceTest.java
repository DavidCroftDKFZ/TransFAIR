package de.samply.transfair;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.samply.transfair.models.FilterModel;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FilterServiceTest {

  @Autowired FilterService filterService;

  @Test
  public void testWhitelist() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    String json = "{\"patient\": {\n" + "  \t\t\t\"ids\": [\"1\"]\n" + "\t\t\t}" + "}";

    FilterModel filter = objectMapper.readValue(json, FilterModel.class);

    assert (filter.patient.ids.contains("1"));
  }


    @Test
  public void emptyLoad() {
    assert(Objects.isNull(filterService.whitelist));
    assert(Objects.isNull(filterService.blacklist));
  }

  @Test
  public void loadWhiteList() {
  }


}
