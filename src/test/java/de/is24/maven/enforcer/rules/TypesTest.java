package de.is24.maven.enforcer.rules;

import org.junit.Test;
import org.objectweb.asm.Type;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TypesTest {

  @Test
  public void testReadType() {
    assertThat(Types.readType(Type.BOOLEAN_TYPE), is("boolean"));
    assertThat(Types.readType(Type.getType(getClass())), is(getClass().getName()));

    final int[][] intIntArray = new int[0][0];
    assertThat(Types.readType(Type.getType(intIntArray.getClass())), is("int"));

    final TypesTest[] typesTestArray = new TypesTest[0];
    assertThat(Types.readType(Type.getType(typesTestArray.getClass())), is(getClass().getName()));
  }

  @Test
  public void testReadValueType() {
    assertThat(Types.readValueType(Type.BOOLEAN_TYPE), is("boolean"));
    assertThat(Types.readValueType(Boolean.TRUE), is(Boolean.class.getName()));
    assertThat(Types.readValueType(Type.VOID_TYPE), is("void"));

    final int i = 0;
    assertThat(Types.readValueType(i), is(Integer.class.getName()));

    final int[] ia = {i};
    assertThat(Types.readValueType(ia), is("int"));
  }

  @Test
  public void testReadTypeDescription() {

  }

  @Test
  public void testReadInternalTypeName() {
    assertThat(Types.readInternalTypeName("int"), is("int"));

    final int[][] intIntArray = new int[0][0];
    assertThat(Types.readInternalTypeName(intIntArray.getClass().getName()), is("int"));
  }
}