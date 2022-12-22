package de.samply.transfair;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.samply.transfair.models.FilterModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FilterServiceTest {

  @Test
  public void testWhitelist() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();

    String json = "{\"patient\": {\n" + "  \t\t\t\"ids\": [\"1\"]\n" + "\t\t\t}" + "}";
    FilterModel filter = objectMapper.readValue(json, FilterModel.class);

    assert (filter.patient.ids.contains("1"));
  }

  @Test
  public void testWhitelist2() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();

    String json =
        "{\"patient\": {\n"
            + "  \t\t\t\"ids\": [\"1\",\"jsao3aac8\"],\n"
            + "\t\t\t\"profile\": [\"fhir\"]\n"
            + "\t\t\t},\n"
            + "  \"specimen\": {\n"
            + "\t\"ids\": [\"1\"],\n"
            + "\t\"profile\": [\"fhir\"]\n"
            + "\t\t\t}\n"
            + "}";
    FilterModel filter = objectMapper.readValue(json, FilterModel.class);

    assert (filter.patient.ids.contains("1"));
    assert (filter.patient.ids.contains("jsao3aac8"));
    assert (filter.patient.profile.contains("fhir"));
    assert (filter.specimen.ids.contains("1"));
    assert (filter.specimen.profile.contains("fhir"));
  }
}
