package org.apache.velocity.test.issues;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.test.BaseTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.velocity.test.TemplateTestBase.FILE_RESOURCE_LOADER_PATH;

public class Velocity919TestCase extends BaseTestCase
{
    public Velocity919TestCase(String name)
    {
        super(name);
    }

    public void testUnbreakableSpace() throws Exception
    {
        assertEvalEquals("before\u200Bafter", "before\u200Bafter");
    }

}
