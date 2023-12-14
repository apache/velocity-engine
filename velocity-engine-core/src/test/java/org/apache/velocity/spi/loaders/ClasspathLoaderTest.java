package org.apache.velocity.spi.loaders;

import java.io.IOException;
import java.io.Reader;
import java.util.Locale;
import org.apache.velocity.api.Resource;
import org.apache.velocity.runtime.RuntimeInstance;
import org.junit.Assert;
import org.junit.Test;

public class ClasspathLoaderTest {

  @Test
  public void test_load() {
    ClasspathLoader loader = new ClasspathLoader();
    RuntimeInstance runtimeInstance = new RuntimeInstance();
    runtimeInstance.init();
    loader.setEngine(runtimeInstance);
    loader.setEncoding("utf-8");
    loader.setLocale(Locale.getDefault());
    loader.setReloadable(true);
    loader.setTemplateSuffix(new String[]{".vm"});
    loader.setTemplateDirectory(new String[]{"evaluate/"});
    try {
      Resource resource = loader.load("eval1.vm", null, null);
      try (Reader reader = resource.openReader()) {
      }
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }
}
