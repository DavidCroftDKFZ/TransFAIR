package de.samply.transfair;

import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FilterServiceTest {

  @Autowired FilterService filterService;

  @Test
  public void emptyLoad() {
    assert(Objects.isNull(filterService.whitelist));
    assert(Objects.isNull(filterService.blacklist));
  }

  @Test
  public void loadWhiteList() {
  }


}
