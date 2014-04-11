package de.is24.maven.enforcer.rules;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * RepositoryTest
 *
 * @author aschubert
 */
public class RepositoryTest {
  private static final Logger LOG = LoggerFactory.getLogger(RepositoryTest.class);

  private Log logger;

  @Before
  public void setup() {
    final Log logger = new Log() {
      @Override
      public boolean isDebugEnabled() {
        return true;
      }

      @Override
      public void debug(CharSequence content) {
        LOG.info(String.valueOf(content));
      }

      @Override
      public void debug(CharSequence content, Throwable error) {
        LOG.info(String.valueOf(content), error);
      }

      @Override
      public void debug(Throwable error) {
        LOG.info("", error);
      }

      @Override
      public boolean isInfoEnabled() {
        return true;
      }

      @Override
      public void info(CharSequence content) {
        LOG.info(String.valueOf(content));
      }

      @Override
      public void info(CharSequence content, Throwable error) {
        LOG.info(String.valueOf(content), error);
      }

      @Override
      public void info(Throwable error) {
        LOG.info("", error);
      }

      @Override
      public boolean isWarnEnabled() {
        return true;
      }

      @Override
      public void warn(CharSequence content) {
        LOG.info(String.valueOf(content));
      }

      @Override
      public void warn(CharSequence content, Throwable error) {
        LOG.info(String.valueOf(content), error);
      }

      @Override
      public void warn(Throwable error) {
        LOG.info("", error);
      }

      @Override
      public boolean isErrorEnabled() {
        return true;
      }

      @Override
      public void error(CharSequence content) {
        LOG.info(String.valueOf(content));
      }

      @Override
      public void error(CharSequence content, Throwable error) {
        LOG.info(String.valueOf(content), error);
      }

      @Override
      public void error(Throwable error) {
        LOG.info("", error);
      }
    };

    this.logger = logger;
  }

  @Test
  public void testAddType() {
    final Repository repository = new Repository(logger);

    assertThat(repository.getTypes().size(), is(0));

    repository.addType("B");
    assertThat(repository.getTypes().size(), is(0));

    repository.addType("java.lang.Fake");
    assertThat(repository.getTypes().size(), is(0));

    repository.addType("de.is24.Type");
    assertThat(repository.getTypes().size(), is(1));
    assertThat(repository.getTypes().iterator().next(), is("de.is24.Type"));
  }

  @Test
  public void testAddDependency() {
    final Repository repository = new Repository(logger);
    assertThat(repository.getDependencies().size(), is(0));

    repository.addDependency("B");
    assertThat(repository.getDependencies().size(), is(0));

    repository.addDependency("java.lang.Fake");
    assertThat(repository.getDependencies().size(), is(0));

    repository.addDependency("de.is24.Type");
    assertThat(repository.getDependencies().size(), is(1));
    assertThat(repository.getDependencies().iterator().next(), is("de.is24.Type"));
  }

  @Test
  public void testSuppressionOfClasses() {
    final Repository repository = new Repository(logger, "de\\.is24\\.suppress.*", ".*SuppressMe.*");
    assertThat(repository.getDependencies().size(), is(0));

    repository.addDependency("de.is24.package.Type");
    repository.addDependency("de.is24.package.subpackage.Type");
    repository.addDependency("de.is24.package.Type$Subtype");
    assertThat(repository.getDependencies().size(), is(3));

    repository.addDependency("java.is24.suppress.subpackage.Type");
    repository.addDependency("java.is24.suppress.Type");
    repository.addDependency("java.is24.suppress.Type$SubType");
    assertThat(repository.getDependencies().size(), is(3));

    repository.addDependency("de.is24.SuppressMe");
    repository.addDependency("de.is24.SuppressMe$Subtype");
    assertThat(repository.getDependencies().size(), is(3));
  }
}
