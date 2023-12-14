```
        +----------+                             |
        |          |  nowContext  +---------+    |
        |          | -----------> | Context | <--+ render
   init |          |              +---------+    |
  ----->| Velocity |                             v
        |          |  getTemplate  +------------------+
        |          | ------------> |     Template     |
        |          |               +------------------+
        +----------+
```

```java
import java.io.StringReader;
import java.io.Writer;
import org.apache.velocity.Template;
import org.apache.velocity.spi.Translator;
import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ResourceManager;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

public class View {

  /**
   * render the template
   *
   * @param name template name
   * @param writer out
   */
  public void render(String name, Writer writer) {
    // 1. get template
    Template template = Velocity.getTemplate(name, "utf-8");
    // 2. get current context
    Context context = Velocity.nowContext();
    // 3. render template
    template.render(context, writer);
  }
}

public class Velocity {

  private ResourceManager resourceManager;
  private Parser parser;
  private Translator translator;
  private ResourceLoader loader;

  public Template getTemplate(String name, String encoding) {
    String path = toPath(name);
    Resource resource = resourceManager.getResource(path, ResourceManager.RESOURCE_TEMPLATE, encoding);
    SimpleNode node = parser.parse(new StringReader((String) resource.getData()), new Template());
    return translator.translate(resource, node);
  }

  private String toPath(String name) {
    return "suffix/" + name;
  }

}

```
