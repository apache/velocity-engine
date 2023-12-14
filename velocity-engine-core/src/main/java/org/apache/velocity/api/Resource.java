package org.apache.velocity.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;
import org.apache.velocity.runtime.RuntimeInstance;

public interface Resource {

  /**
   * Get the resource name.
   *
   * @return name
   */
  String getName();

  /**
   * Get the resource encoding.
   *
   * @return encoding
   */
  String getEncoding();

  /**
   * Get the resource locale.
   *
   * @return locale
   */
  Locale getLocale();

  /**
   * Get the resource last modified time.
   *
   * @return last modified time
   */
  long getLastModified();

  /**
   * Get the resource length.
   *
   * @return source length
   */
  long getLength();

  /**
   * Get the template source.
   *
   * @return source
   * @throws IOException - If an I/O error occurs
   */
  String getSource() throws IOException;

  /**
   * Get the template source reader.
   * <p/>
   * NOTE: Don't forget close the reader.
   * <p/>
   * <pre>
   * Reader reader = resource.openReader();
   * try {
   * 	 // do something ...
   * } finally {
   * 	 reader.close();
   * }
   * </pre>
   *
   * @return source reader
   * @throws IOException - If an I/O error occurs
   */
  Reader openReader() throws IOException;

  /**
   * Get the template source input stream.
   * <p/>
   * NOTE: Don't forget close the input stream.
   * <p/>
   * <pre>
   * InputStream stream = resource.openStream();
   * try {
   * 	 // do something ...
   * } finally {
   * 	 stream.close();
   * }
   * </pre>
   *
   * @return source input stream
   * @throws IOException - If an I/O error occurs
   */
  InputStream openStream() throws IOException;

  /**
   * Get the template engine.
   *
   * @return engine
   */
  RuntimeInstance getEngine();

}
