package org.apache.velocity.spi.translators;

import java.io.IOException;
import java.io.Reader;
import java.util.Locale;
import org.apache.velocity.api.Resource;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.spi.Loader;
import org.apache.velocity.spi.Parser;
import org.apache.velocity.spi.compilers.JavassistCompiler;
import org.apache.velocity.spi.loaders.ClasspathLoader;
import org.apache.velocity.spi.parsers.TemplateParser;
import org.junit.Assert;
import org.junit.Test;

public class CompiledTranslatorTest {

  @Test
  public void test_translate() {
    RuntimeInstance runtimeInstance = new RuntimeInstance();
    runtimeInstance.init();
    Loader loader = creatLoader(runtimeInstance);
    Parser parser = createParser(runtimeInstance);

    CompiledTranslator translator = new CompiledTranslator();
    translator.setCompiler(new JavassistCompiler());
    Resource resource;
    String res;
    try {
      resource = loader.load("eval1.vm", null, null);
      try (Reader reader = resource.openReader()) {
        StringBuilder out = new StringBuilder();
        int ch;
        while ((ch = reader.read()) != -1) {
          out.append((char) ch);
        }
        res = out.toString();
      }
      translator.translate(resource, parser.parse("eval1.vm", res));
    } catch (IOException | ParseException e) {
      Assert.fail(e.getMessage());
    }
  }

  private Parser createParser(RuntimeInstance runtimeInstance) {
    TemplateParser parser = new TemplateParser();
    parser.setInnerParser(runtimeInstance.createNewParser());
    return parser;
  }

  private Loader creatLoader(RuntimeInstance runtimeInstance) {
    ClasspathLoader loader = new ClasspathLoader();
    loader.setEngine(runtimeInstance);
    loader.setEncoding("utf-8");
    loader.setLocale(Locale.getDefault());
    loader.setReloadable(true);
    loader.setTemplateSuffix(new String[]{".vm"});
    loader.setTemplateDirectory(new String[]{"evaluate/"});
    return loader;
  }
}
