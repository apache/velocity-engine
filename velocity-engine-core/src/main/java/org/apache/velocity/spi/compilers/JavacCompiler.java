package org.apache.velocity.spi.compilers;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.apache.velocity.util.ClassUtils;

public class JavacCompiler extends BaseCompiler {

  private final JavaCompiler compiler;
  private final DiagnosticCollector<JavaFileObject> diagnosticCollector;
  private final StandardJavaFileManager standardJavaFileManager;
  private final ClassLoaderImpl classLoader;
  private final JavaFileManagerImpl javaFileManager;
  private final List<String> options = new ArrayList<>();
  private final List<String> lintOptions = new ArrayList<>();

  public JavacCompiler() {
    compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new IllegalStateException(
          "Can not get system java compiler.");
    }
    diagnosticCollector = new DiagnosticCollector<JavaFileObject>();
    standardJavaFileManager = compiler.getStandardFileManager(diagnosticCollector, null, null);
    ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
    try {
      contextLoader.loadClass(JavacCompiler.class.getName());
    } catch (ClassNotFoundException e) {
      contextLoader = JavacCompiler.class.getClassLoader();
    }
    ClassLoader loader = contextLoader;
    Set<File> files = new HashSet<>();
    while (loader instanceof URLClassLoader
        && (!loader.getClass().getName().equals("sun.misc.Launcher$AppClassLoader"))) {
      URLClassLoader urlClassLoader = (URLClassLoader) loader;
      for (URL url : urlClassLoader.getURLs()) {
        files.add(new File(url.getFile()));
      }
      loader = loader.getParent();
    }
    if (files.size() > 0) {
      try {
        Iterable<? extends File> list = standardJavaFileManager.getLocation(StandardLocation.CLASS_PATH);
        for (File file : list) {
          files.add(file);
        }
        standardJavaFileManager.setLocation(StandardLocation.CLASS_PATH, files);
      } catch (IOException e) {
        throw new IllegalStateException(e.getMessage(), e);
      }
    }
    final ClassLoader parentLoader = contextLoader;
    classLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoaderImpl>() {
      public ClassLoaderImpl run() {
        return new ClassLoaderImpl(parentLoader);
      }
    });
    javaFileManager = new JavaFileManagerImpl(standardJavaFileManager, classLoader);
    lintOptions.add("-Xlint:unchecked");
  }

  @Override
  protected Class<?> doCompile(String name, String source) throws Exception {
    return doCompile(name, source, options);
  }

  private Class<?> doCompile(String name, String sourceCode, List<String> options) throws Exception {
    try {
      return classLoader.loadClass(name);
    } catch (ClassNotFoundException e) {
      int i = name.lastIndexOf('.');
      String packageName = i < 0 ? "" : name.substring(0, i);
      String className = i < 0 ? name : name.substring(i + 1);
      JavaFileObjectImpl javaFileObject = new JavaFileObjectImpl(className, sourceCode);
      javaFileManager.putFileForInput(StandardLocation.SOURCE_PATH, packageName,
          className + ClassUtils.JAVA_EXTENSION, javaFileObject);
      Boolean result = compiler.getTask(
          null,
          javaFileManager,
          diagnosticCollector,
          options,
          null,
          Collections.singletonList(javaFileObject)
      ).call();
      if (result == null || !result) {
        throw new IllegalStateException(
            "Compilation failed. class: " + name + ", diagnostics: " + diagnosticCollector.getDiagnostics());
      }
      if (compileDirectory != null) {
        saveBytecode(name, javaFileObject.getByteCode());
      }
      return classLoader.loadClass(name);
    }
  }

  private static final class JavaFileObjectImpl extends SimpleJavaFileObject {

    private final CharSequence source;
    private ByteArrayOutputStream bytecode;

    public JavaFileObjectImpl(final String baseName, final CharSequence source) {
      super(ClassUtils.toURI(baseName + ClassUtils.JAVA_EXTENSION), Kind.SOURCE);
      this.source = source;
    }

    public JavaFileObjectImpl(final String name, final Kind kind) {
      super(ClassUtils.toURI(name), kind);
      source = null;
    }

    public JavaFileObjectImpl(URI uri, Kind kind) {
      super(uri, kind);
      source = null;
    }

    @Override
    public CharSequence getCharContent(final boolean ignoreEncodingErrors) throws UnsupportedOperationException {
      if (source == null) {
        throw new UnsupportedOperationException("source == null");
      }
      return source;
    }

    @Override
    public InputStream openInputStream() {
      return new ByteArrayInputStream(getByteCode());
    }

    @Override
    public OutputStream openOutputStream() {
      return bytecode = new ByteArrayOutputStream();
    }

    public byte[] getByteCode() {
      return bytecode.toByteArray();
    }
  }

  private static final class JavaFileManagerImpl extends ForwardingJavaFileManager<JavaFileManager> {

    private final ClassLoaderImpl classLoader;
    private final Map<URI, JavaFileObject> fileObjects = new HashMap<URI, JavaFileObject>();

    public JavaFileManagerImpl(JavaFileManager fileManager, ClassLoaderImpl classLoader) {
      super(fileManager);
      this.classLoader = classLoader;
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
      FileObject o = fileObjects.get(uri(location, packageName, relativeName));
      if (o != null) {
        return o;
      }
      return super.getFileForInput(location, packageName, relativeName);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName, Kind kind, FileObject fo)
        throws IOException {
      JavaFileObject file = new JavaFileObjectImpl(qualifiedName, kind);
      classLoader.add(qualifiedName, file);
      return file;
    }

    @Override
    public ClassLoader getClassLoader(JavaFileManager.Location location) {
      return classLoader;
    }

    @Override
    public String inferBinaryName(Location loc, JavaFileObject file) {
      if (file instanceof JavaFileObjectImpl) {
        return file.getName();
      }
      return super.inferBinaryName(loc, file);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
        throws IOException {
      ArrayList<JavaFileObject> files = new ArrayList<JavaFileObject>();
      if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
        for (JavaFileObject file : fileObjects.values()) {
          if (file.getKind() == Kind.CLASS && file.getName().startsWith(packageName)) {
            files.add(file);
          }
        }
        files.addAll(classLoader.files());
      } else if (location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)) {
        for (JavaFileObject file : fileObjects.values()) {
          if (file.getKind() == Kind.SOURCE && file.getName().startsWith(packageName)) {
            files.add(file);
          }
        }
      }
      Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);
      for (JavaFileObject file : result) {
        files.add(file);
      }
      return files;
    }

    public void putFileForInput(StandardLocation location, String packageName, String relativeName,
        JavaFileObject file) {
      fileObjects.put(uri(location, packageName, relativeName), file);
    }

    private URI uri(Location location, String packageName, String relativeName) {
      return ClassUtils.toURI(location.getName() + '/' + packageName + '/' + relativeName);
    }

  }

  private static final class ClassLoaderImpl extends ClassLoader {

    private final Map<String, JavaFileObject> classes = new HashMap<String, JavaFileObject>();

    ClassLoaderImpl(final ClassLoader parentClassLoader) {
      super(parentClassLoader);
    }

    Collection<JavaFileObject> files() {
      return Collections.unmodifiableCollection(classes.values());
    }

    @Override
    protected Class<?> findClass(final String qualifiedClassName) throws ClassNotFoundException {
      try {
        return super.findClass(qualifiedClassName);
      } catch (ClassNotFoundException e) {
        JavaFileObject file = classes.get(qualifiedClassName);
        if (file != null) {
          byte[] bytes = ((JavaFileObjectImpl) file).getByteCode();
          return defineClass(qualifiedClassName, bytes, 0, bytes.length);
        }
        throw e;
      }
    }

    @Override
    public InputStream getResourceAsStream(final String name) {
      if (name.endsWith(ClassUtils.CLASS_EXTENSION)) {
        String qualifiedClassName = name.substring(0, name.length() - ClassUtils.CLASS_EXTENSION.length())
            .replace('/', '.');
        JavaFileObjectImpl file = (JavaFileObjectImpl) classes.get(qualifiedClassName);
        if (file != null) {
          return new ByteArrayInputStream(file.getByteCode());
        }
      }
      return super.getResourceAsStream(name);
    }

    public void add(final String qualifiedClassName, final JavaFileObject javaFile) {
      classes.put(qualifiedClassName, javaFile);
    }

  }
}
