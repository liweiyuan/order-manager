package com.example.ordermanager;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ApplicationModularityTest {

  ApplicationModules modules = ApplicationModules.of(OrderManagerApplication.class);

  @Test
  public void verifiesModuleStructure() {
    modules.verify();
  }

  @Test
  public void createDocumentation() {
    if (modules == null) {
      throw new IllegalStateException("No modules found! Cannot create documentation.");

    }
    new Documenter(modules).writeDocumentation().writeIndividualModulesAsPlantUml();
  }
}
