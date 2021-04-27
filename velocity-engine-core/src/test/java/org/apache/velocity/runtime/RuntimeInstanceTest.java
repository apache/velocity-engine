package org.apache.velocity.runtime;

import static org.junit.Assert.assertEquals;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ResourceManager;
import org.junit.Test;

public class RuntimeInstanceTest {

    @Test
    public void givenOverridenInputEncoding_whenInitializing_defaultEncodingIsOverridden() {
        RuntimeInstance instance = new RuntimeInstance();
        MockResourceManager manager = new MockResourceManager();
        String value = "testDummyEncoding";
        instance.addProperty(RuntimeConstants.INPUT_ENCODING, value);
        instance.addProperty(RuntimeConstants.RESOURCE_MANAGER_INSTANCE, manager);
        instance.init();

        instance.getTemplate("some template");

        assertEquals(value, manager.encoding);

    }

    class MockResourceManager implements ResourceManager {

        String encoding = null;

        @Override
        public String getLoaderNameForResource(String resourceName) {
            return null;
        }

        @Override
        public Resource getResource(String resourceName, int resourceType, String encoding)
                throws ResourceNotFoundException, ParseErrorException {
            this.encoding = encoding;
            return null;
        }

        @Override
        public void initialize(RuntimeServices rs) {

        }
    }
}
