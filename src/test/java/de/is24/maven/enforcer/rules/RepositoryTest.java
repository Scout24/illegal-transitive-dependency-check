package de.is24.maven.enforcer.rules;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import javax.sql.DataSource;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class RepositoryTest {
  private final Log logger = new LogStub();

  @Test
  public void testAddType() {
    final Repository repository = new Repository(logger, false);

    assertThat(repository.getTypes().size(), is(0));

    repository.addType("byte");
    assertThat(repository.getTypes().size(), is(0));

    repository.addType("java.lang.Fake");
    assertThat(repository.getTypes().size(), is(0));

    repository.addType("de.is24.Type");
    assertThat(repository.getTypes().size(), is(1));
    assertThat(repository.getTypes().iterator().next(), is("de.is24.Type"));
  }

  @Test
  public void testSuppressionOfAnonymousTypeName() {
    final Repository repository = new Repository(logger, false);

    assertThat(repository.getTypes().size(), is(0));

    repository.addType("1");
    assertThat(repository.getTypes().size(), is(0));

    repository.addType("123$2");
    assertThat(repository.getTypes().size(), is(0));
  }


  @Test
  public void testAddDependency() {
    final Repository repository = new Repository(logger, false);
    assertThat(repository.getDependencies().size(), is(0));

    repository.addDependency("char");
    assertThat(repository.getDependencies().size(), is(0));

    repository.addDependency("java.lang.Fake");
    assertThat(repository.getDependencies().size(), is(0));

    repository.addDependency("de.is24.Type");
    assertThat(repository.getDependencies().size(), is(1));
    assertThat(repository.getDependencies().iterator().next(), is("de.is24.Type"));
  }

  @Test
  public void testSuppressionOfClasses() {
    final Repository repository = new Repository(logger, false, "de\\.is24\\.suppress.*", ".*SuppressMe.*");
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

  @Test
  public void testSuppressionOfJdkTypes() {
    final Repository repository = new Repository(logger, true);
    assertThat(repository.getTypes().size(), is(0));

    // add a package not in the current JDK
    repository.addType(StringUtils.class.getName());

    // add a package that is part of all JDKs
    repository.addType(DataSource.class.getName());

    assertThat(repository.getTypes().size(), is(1));
    assertThat(repository.getTypes().iterator().next(), is(StringUtils.class.getName()));
  }
}
