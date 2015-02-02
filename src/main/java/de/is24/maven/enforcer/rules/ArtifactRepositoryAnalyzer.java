package de.is24.maven.enforcer.rules;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


final class ArtifactRepositoryAnalyzer {
  private static final String CLASS_SUFFIX = ".class";
  private static final Pattern JAR_FILE_PATTERN = Pattern.compile("^.+\\.(jar|war|JAR|WAR)$");

  private final Log logger;
  private final boolean analyzeDependencies;
  private final ClassFilter filter;

  private ArtifactRepositoryAnalyzer(Log logger, boolean analyzeDependencies, ClassFilter filter) {
    this.logger = logger;
    this.analyzeDependencies = analyzeDependencies;
    this.filter = filter;
  }

  static ArtifactRepositoryAnalyzer analyzeArtifacts(Log logger, boolean analyzeDependencies,
                                                     ClassFilter filter) {
    return new ArtifactRepositoryAnalyzer(logger, analyzeDependencies, filter);
  }

  Repository analyzeArtifacts(Iterable<Artifact> artifacts) {
    final Repository repository = new Repository(filter);

    for (Artifact artifact : artifacts) {
      final File artifactFile = artifact.getFile();
      if (artifactFile == null) {
        logger.info("Artifact '" + artifact + "' has no associated file, skip it.");
        continue;
      }

      if (artifactFile.isDirectory()) {
        analyzeClassesDirectory(repository, artifactFile);
      } else {
        final String absolutePath = artifactFile.getAbsolutePath();
        if (JAR_FILE_PATTERN.matcher(absolutePath).matches()) {
          analyzeJar(repository, artifactFile);
        } else {
          logger.info("Artifact '" + artifact + "' associated file '" + absolutePath + "', is skipped.");
        }
      }
    }
    return repository;
  }

  private void analyzeJar(Repository repository, File jar) {
    final ClassVisitor classVisitor = new ClassDependencyResolvingVisitor(repository, logger);

    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile(jar.getAbsolutePath());

      final Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        final ZipEntry entry = entries.nextElement();
        final String fileName = entry.getName();
        if (fileName.endsWith(CLASS_SUFFIX)) {
          if (logger.isDebugEnabled()) {
            logger.debug("Analyze class '" + fileName + "' in JAR '" + jar + "'.");
          }

          final ClassReader classReader = new ClassReader(zipFile.getInputStream(entry));

          final String className = classReader.getClassName().replace('/', '.');
          if (analyzeDependencies) {
            if (filter.isConsideredType(className)) {
              classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);
            }
          } else {
            repository.addType(className);
          }
        }
      }
    } catch (IOException e) {
      throw logAndWrapIOException(e, jar, "artifact");
    } finally {
      if (zipFile != null) {
        try {
          zipFile.close();
        } catch (IOException e) {
          throw logAndWrapIOException(e, jar, "artifact");
        }
      }
    }
  }

  private IllegalStateException logAndWrapIOException(IOException e, File file, final String description) {
    final String error = "Unable to read class(es) from " + description + " '" + file + "'.";
    logger.error(error, e);
    return new IllegalStateException(error, e);
  }

  private void analyzeClassesDirectory(Repository repository, File classesDirectory) {
    final ClassVisitor classVisitor = new ClassDependencyResolvingVisitor(repository, logger);
    analyzeClassesDirectory(repository, classesDirectory, classVisitor);
  }

  private void analyzeClassesDirectory(Repository repository, File directory, ClassVisitor classVisitor) {
    if (directory.isDirectory()) {
      final String[] entries = directory.list();
      for (String entry : entries) {
        analyzeClassesDirectory(repository, new File(directory, entry), classVisitor);
      }
    }

    final String path = directory.getPath();
    if (path.endsWith(CLASS_SUFFIX)) {
      if (logger.isDebugEnabled()) {
        logger.debug("Analyze class '" + path + "'.");
      }

      FileInputStream classFileStream = null;
      try {
        classFileStream = new FileInputStream(directory);
        final ClassReader classReader = new ClassReader(classFileStream);
        String className = classReader.getClassName().replace('/', '.');
        if (analyzeDependencies) {
          if (filter.isConsideredType(className)) {
            classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);
          }
        } else {
          repository.addType(className);
        }
      } catch (IOException e) {
        throw logAndWrapIOException(e, directory, "file");
      } finally {
        try {
          if (classFileStream != null) {
            classFileStream.close();
          }
        } catch (IOException e) {
          throw logAndWrapIOException(e, directory, "file");
        }
      }
    }
  }
}
