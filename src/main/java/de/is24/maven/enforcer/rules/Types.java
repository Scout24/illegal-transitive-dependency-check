package de.is24.maven.enforcer.rules;

import org.objectweb.asm.Type;


final class Types {
  private Types() {
  }

  static String readType(Type type) {
    switch (type.getSort()) {
      case Type.ARRAY: {
        return readType(type.getElementType());
      }

      default: {
        return type.getClassName();
      }
    }
  }

  static String readValueType(Object value) {
    final Type type;
    if (value instanceof Type) {
      type = (Type) value;
    } else {
      type = Type.getType(value.getClass());
    }
    return readType(type);
  }

  static String readTypeDescription(String description) {
    final Type type = Type.getType(description);
    return readType(type);
  }

  static String readInternalTypeName(String internalName) {
    final Type type = Type.getObjectType(internalName);
    return readType(type);
  }
}
