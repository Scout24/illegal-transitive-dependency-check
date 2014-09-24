package de.is24.maven.enforcer.rules;

import org.objectweb.asm.Type;


public final class Types {
  private Types() {
  }

  static String readTypeName(Type type) {
    switch (type.getSort()) {
      case Type.ARRAY: {
        return readTypeName(type.getElementType());
      }

      default: {
        return type.getClassName();
      }
    }
  }

  static String getObjectValueType(Object value) {
    final Type type;
    if (value instanceof Type) {
      type = (Type) value;
    } else {
      type = Type.getType(value.getClass());
    }
    return readTypeName(type);
  }

  static String readTypeDescription(String description) {
    final Type type = Type.getType(description);

    return readTypeName(type);
  }

  static String readClassName(String name) {
    final Type type = Type.getObjectType(name);
    return readTypeName(type);
  }
}
