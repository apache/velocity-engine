package org.apache.velocity.test.issues;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.test.BaseTestCase;

import java.util.HashMap;
import java.util.Map;

public class Velocity988TestCase extends BaseTestCase {
    public Velocity988TestCase(String name) {
        super(name);
    }

    @Override
    protected void setUpContext(VelocityContext context)
    {
        context.put("foo", "bar");

        Map<String, String> submap = new HashMap<>();
        submap.put("@StudyEventRepeatKey", "123");
        Map<String, Object> map = new HashMap<>();
        map.put("StudyEventData", submap);
        context.put("SubjectData", map);
    }

    public void testSimpleNoSilent()
    {
        assertEvalEquals("bar", "${$foo}");
    }

    public void testSimpleSilent()
    {
        assertEvalEquals("bar", "$!{$foo}");
    }

    public void testSimplePrefix()
    {
        assertEvalEquals("#bar", "#${$foo}");
    }

    private static final String EXPECTED =
            "{\n" +
            " \"eventRepeatKey\": \"123\",\n" +
            " \"otherField\": \"someValue\"\n" +
            "}";

    public void testComplexWithoutExtraDollar()
    {
        String template =
                "{\n" +
                        " #if($!{SubjectData.StudyEventData['@StudyEventRepeatKey']})\n" +
                        " \"eventRepeatKey\": \"$SubjectData.StudyEventData['@StudyEventRepeatKey']\",\n" +
                        " #end\n" +
                        " \"otherField\": \"someValue\"\n" +
                        "}";
        assertEvalEquals(EXPECTED, template);
    }

    public void testComplexWithExtraDollar()
    {
        String template =
                "{\n" +
                " #if($!{$SubjectData.StudyEventData['@StudyEventRepeatKey']})\n" +
                " \"eventRepeatKey\": \"$SubjectData.StudyEventData['@StudyEventRepeatKey']\",\n" +
                " #end\n" +
                " \"otherField\": \"someValue\"\n" +
                "}";
        assertEvalEquals(EXPECTED, template);
    }
}
