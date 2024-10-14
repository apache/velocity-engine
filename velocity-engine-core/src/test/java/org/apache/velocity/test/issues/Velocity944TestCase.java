package org.apache.velocity.test.issues;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.test.BaseTestCase;

public class Velocity944TestCase extends BaseTestCase {
    public Velocity944TestCase(String name) {
        super(name);
    }

    @Override
    protected void setUpEngine(VelocityEngine engine) {
        engine.setProperty("velocimacro.inline.local_scope", "true");
    }

    @Override
    protected void setUpContext(VelocityContext context) {
        context.put("someObj", new SomeClass());
    }

    public void testVelocity944StringLiteral() throws Exception {
        assertEvalEquals(
                "<span>foo</span> some text <span>bar</span>",
                "#macro( m $v )<span>$v</span>#end#m( 'foo' ) some text #set($str=\"#m( 'bar' )\")$str"
        );
    }

    public void testVelocity944ReferenceArgument() throws Exception {
        assertEvalEquals(
                "<span>foo</span> some text <span>bar</span>",
                "#macro( m $v )<span>$v</span>#end#m( 'foo' ) some text ${someObj.someMethod(\"#m( 'bar' )\")}"
        );
    }

    public void testVelocity944LoadedTemplate() throws Exception {
        addTemplate("velocity944", "#macro( m $v )<span>$v</span>#end#m( 'foo' ) some text ${someObj.someMethod(\"#m( 'bar' )\")}");
        assertTmplEquals("<span>foo</span> some text <span>bar</span>", "velocity944");
    }

    public static class SomeClass
    {
        public String someMethod(final String value)
        {
            return value;
        }
    }
}
