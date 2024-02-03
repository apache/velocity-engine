package org.apache.velocity.runtime.parser;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class CustomParserTestCase
{
    VelocityEngine engine;

    static String TEMPLATES_DIR = System.getProperty("test.templates.dir");
    static String RESULTS_DIR = System.getProperty("test.results.dir");
    static String REFERENCE_DIR = System.getProperty("test.reference.dir");

    @Before
    public void setUp()
    {
        engine = new VelocityEngine();
        engine.setProperty("resource.loaders", "file");
        engine.setProperty("resource.loader.file.path", TEMPLATES_DIR);
        engine.setProperty("parser.class", "org.apache.velocity.runtime.parser.custom.CustomParser");
        engine.init();
    }

    @Test
    public void testMarkdownTemplate() throws Exception
    {
        VelocityContext ctx = new VelocityContext();
        ctx.put("some", "value");
        Template tmpl = engine.getTemplate("test.md", "UTF-8");

        String resultFile = RESULTS_DIR + File.separator + "test.md";
        String referenceFile = REFERENCE_DIR + File.separator + "test.md";

        new File(resultFile).getParentFile().mkdirs();

        FileWriter writer = new FileWriter(resultFile);
        tmpl.merge(ctx, writer);
        writer.flush();
        writer.close();

        String result = new String(Files.readAllBytes(Paths.get(resultFile)), StandardCharsets.UTF_8)
        String reference = new String(Files.readAllBytes(Paths.get(referenceFile)), StandardCharsets.UTF_8)
        assertEquals(reference, result);
    }
}
