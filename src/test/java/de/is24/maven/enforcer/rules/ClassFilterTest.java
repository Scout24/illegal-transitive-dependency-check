package de.is24.maven.enforcer.rules;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ClassFilterTest {
  private final Log logger = new LogStub();


  @Test
  public void testSuppressionOfNativeTypes() throws Exception {
    final ClassFilter filter = new ClassFilter(logger, true);
    assertThat(filter.isConsideredType("byte"), is(false));
    assertThat(filter.isConsideredType("int"), is(false));
    assertThat(filter.isConsideredType("long"), is(false));

    final Set<String> types = new HashSet<>();
    filter.addFiltered(types, "char");
    filter.addFiltered(types, "float");
    filter.addFiltered(types, "double");
    assertThat(types.isEmpty(), is(true));
  }

  @Test
  public void testSuppressionOfJdkTypes() {
    final ClassFilter filter = new ClassFilter(logger, true);
    final Set<String> types = new HashSet<>();

    // add a package not in the current JDK
    assertThat(filter.isConsideredType(StringUtils.class.getName()), is(true));

    // add a package that is part of all JDKs
    assertThat(filter.isConsideredType(DataSource.class.getName()), is(false));

    // the same tests for filtered adding
    filter.addFiltered(types, StringUtils.class.getName());
    filter.addFiltered(types, DataSource.class.getName());

    assertThat(types.size(), is(1));
    assertThat(types.iterator().next(), is(StringUtils.class.getName()));
  }

  @Test
  public void testSuppressionOfClasses() {
    final ClassFilter filter = new ClassFilter(logger, false, "de\\.is24\\.suppress.*", ".*SuppressMe.*");

    assertThat(filter.isConsideredType("de.is24.package.Type"), is(true));
    assertThat(filter.isConsideredType("de.is24.package.subpackage.Type"), is(true));
    assertThat(filter.isConsideredType("de.is24.package.Type$Subtype"), is(true));

    assertThat(filter.isConsideredType("de.is24.suppress.subpackage.Type"), is(false));
    assertThat(filter.isConsideredType("de.is24.suppress.Type"), is(false));
    assertThat(filter.isConsideredType("de.is24.suppress.Type$SubType"), is(false));

    assertThat(filter.isConsideredType("de.is24.SuppressMe"), is(false));
    assertThat(filter.isConsideredType("de.is24.SuppressMe$Subtype"), is(false));
  }
}