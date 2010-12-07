/**
 *
 */
package org.apache.velocity.slf4j;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.*;
import static org.junit.Assert.*;

import org.apache.velocity.runtime.log.LogChute;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

/**
 * Tests {@link Slf4jLogChute}.
 *
 * @version $Id$
 */
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("org.apache.velocity.Slf4jLogChute")
public class Slf4jLogChuteTest {

	/**
	 * Test method for {@link org.apache.velocity.slf4j.Slf4jLogChute#log(int, java.lang.String)}.
	 */
	@Test
	public void testLogIntString() {
		Logger logger = createNiceMock(Logger.class);
		Slf4jLogChute chute = new Slf4jLogChute();
		Whitebox.setInternalState(chute, Logger.class, logger);

		logger.trace("trace message");
		logger.debug("debug message");
		logger.info("info message");
		logger.warn("warn message");
		logger.error("error message");

		replay(logger);
		chute.log(LogChute.TRACE_ID, "trace message");
		chute.log(LogChute.DEBUG_ID, "debug message");
		chute.log(LogChute.INFO_ID, "info message");
		chute.log(LogChute.WARN_ID, "warn message");
		chute.log(LogChute.ERROR_ID, "error message");
		verify(logger);
	}

	/**
	 * Test method for {@link org.apache.velocity.slf4j.Slf4jLogChute#log(int, java.lang.String, java.lang.Throwable)}.
	 */
	@Test
	public void testLogIntStringThrowable() {
		Logger logger = createNiceMock(Logger.class);
		Slf4jLogChute chute = new Slf4jLogChute();
		Whitebox.setInternalState(chute, Logger.class, logger);

		Throwable t = new Throwable("test throwable");

		logger.trace("trace message", t);
		logger.debug("debug message", t);
		logger.info("info message", t);
		logger.warn("warn message", t);
		logger.error("error message", t);

		replay(logger);
		chute.log(LogChute.TRACE_ID, "trace message", t);
		chute.log(LogChute.DEBUG_ID, "debug message", t);
		chute.log(LogChute.INFO_ID, "info message", t);
		chute.log(LogChute.WARN_ID, "warn message", t);
		chute.log(LogChute.ERROR_ID, "error message", t);
		verify(logger);
	}

	/**
	 * Test method for {@link org.apache.velocity.slf4j.Slf4jLogChute#isLevelEnabled(int)}.
	 */
	@Test
	public void testIsLevelEnabled() {
		Logger logger = createNiceMock(Logger.class);
		Slf4jLogChute chute = new Slf4jLogChute();
		Whitebox.setInternalState(chute, Logger.class, logger);

		expect(logger.isTraceEnabled()).andReturn(false);
		expect(logger.isDebugEnabled()).andReturn(false);
		expect(logger.isInfoEnabled()).andReturn(true);
		expect(logger.isWarnEnabled()).andReturn(true);
		expect(logger.isErrorEnabled()).andReturn(true);

		replay(logger);
		assertFalse(chute.isLevelEnabled(LogChute.TRACE_ID));
		assertFalse(chute.isLevelEnabled(LogChute.DEBUG_ID));
		assertTrue(chute.isLevelEnabled(LogChute.INFO_ID));
		assertTrue(chute.isLevelEnabled(LogChute.WARN_ID));
		assertTrue(chute.isLevelEnabled(LogChute.ERROR_ID));
		verify(logger);
	}

}
