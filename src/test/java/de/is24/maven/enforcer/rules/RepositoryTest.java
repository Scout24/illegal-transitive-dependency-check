package de.is24.maven.enforcer.rules;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class RepositoryTest {
  private final Log logger = new LogStub();

  @Test
  public void testAddType() {
    final Repository repository = new Repository(new ClassFilter(logger, false));

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
    final Repository repository = new Repository(new ClassFilter(logger, false));

    assertThat(repository.getTypes().size(), is(0));

    repository.addType("1");
    assertThat(repository.getTypes().size(), is(0));

    repository.addType("123$2");
    assertThat(repository.getTypes().size(), is(0));
  }

  @Test
  public void testAddDependency() {
    final Repository repository = new Repository(new ClassFilter(logger, false));
    assertThat(repository.getDependencies().size(), is(0));

    repository.addDependency("char");
    assertThat(repository.getDependencies().size(), is(0));

    repository.addDependency("java.lang.Fake");
    assertThat(repository.getDependencies().size(), is(0));

    repository.addDependency("de.is24.Type");
    assertThat(repository.getDependencies().size(), is(1));
    assertThat(repository.getDependencies().iterator().next(), is("de.is24.Type"));
  }
}
