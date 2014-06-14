package de.is24.maven.enforcer.rules;

import org.apache.maven.plugin.logging.Log;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;


/**
 * ClassDependencyResolvingVisitor
 *
 * @author aschubert
 */
final class ClassDependencyResolvingVisitor implements ClassVisitor {
  private static final String VOID = "V";
  private final Repository repository;
  private final Log logger;
  private final AnnotationVisitor annotationVisitor = new ClassDependencyAnnotationVisitor();
  private final FieldVisitor fieldVisitor = new ClassDependencyFieldVisitor();
  private final MethodVisitor methodVisitor = new ClassDependencyMethodVisitor();
  private final SignatureVisitor signatureVisitor = new ClassDependencySignatureVisitor();


  public ClassDependencyResolvingVisitor(Repository repository, Log logger) {
    this.repository = repository;
    this.logger = logger;
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    final String className = readClassName(name);
    logger.debug("Add new type '" + className + "'.");
    repository.addType(className);

    final String superTypeName = readClassName(superName);
    addDependency("super type", superTypeName);

    if (interfaces != null) {
      for (String iface : interfaces) {
        final String interfaceType = readClassName(iface);
        addDependency("interface type", interfaceType);
      }
    }

    processSignature(signature);
  }

  @Override
  public void visitSource(String source, String debug) {
  }

  @Override
  public void visitOuterClass(String owner, String name, String desc) {
    logger.debug("visit outer class " + desc);
  }

  @Override
  public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
    final String fieldType = readTypeDescription(desc);
    addDependency("field type", fieldType);

    processSignature(signature);

    return fieldVisitor;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    return delegateToAnnotationVisitor(desc);
  }

  @Override
  public void visitAttribute(Attribute attr) {
  }

  @Override
  public void visitInnerClass(String name, String outerName, String innerName, int access) {
    final String innerClassName = readClassName(name);
    addDependency("inner class", innerClassName);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    final Type[] argumentTypes = Type.getArgumentTypes(desc);
    for (Type argumentType : argumentTypes) {
      final String parameterTypeName = readTypeName(argumentType);
      addDependency("annotation's method parameter type", parameterTypeName);
    }

    final Type returnType = Type.getReturnType(desc);
    final String returnTypeName = readTypeName(returnType);
    addDependency("annotation's method return type", returnTypeName);

    if (exceptions != null) {
      for (String exception : exceptions) {
        final String exceptionName = readClassName(exception);
        addDependency("exception type", exceptionName);
      }
    }
    processSignature(signature);

    return methodVisitor;
  }

  @Override
  public void visitEnd() {
  }

  private AnnotationVisitor delegateToAnnotationVisitor(String desc) {
    final String annotationType = readTypeDescription(desc);
    addDependency("annotation", annotationType);
    return annotationVisitor;
  }

  private void addDependency(String typeDescription, String typeName) {
    logger.debug("Add " + typeDescription + " type '" + typeName + "' as dependency.");
    repository.addDependency(typeName);
  }

  private String readTypeDescription(String description) {
    final Type type = Type.getType(description);
    return readTypeName(type);
  }

  private String readClassName(String name) {
    // sometimes we are called with null ;(
    if (name == null) {
      return VOID;
    }

    final Type type = Type.getObjectType(name);
    return readTypeName(type);
  }

  private static String readTypeName(Type type) {
    switch (type.getSort()) {
      case Type.ARRAY: {
        return readTypeName(type.getElementType());
      }

      case Type.OBJECT: {
        return type.getClassName();
      }

      default: {
        return VOID;
      }
    }
  }

  private void processSignature(String signature) {
    if (signature != null) {
      final SignatureReader signatureReader = new SignatureReader(signature);
      signatureReader.accept(signatureVisitor);
    }
  }

  private final class ClassDependencySignatureVisitor implements SignatureVisitor {
    @Override
    public void visitFormalTypeParameter(String name) {
    }

    @Override
    public SignatureVisitor visitClassBound() {
      return this;
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
      return this;
    }

    @Override
    public SignatureVisitor visitSuperclass() {
      return this;
    }

    @Override
    public SignatureVisitor visitInterface() {
      return this;
    }

    @Override
    public SignatureVisitor visitParameterType() {
      return this;
    }

    @Override
    public SignatureVisitor visitReturnType() {
      return this;
    }

    @Override
    public SignatureVisitor visitExceptionType() {
      return this;
    }

    @Override
    public void visitBaseType(char descriptor) {
      // primitive types are not of interest
    }

    @Override
    public void visitTypeVariable(String name) {
    }

    @Override
    public SignatureVisitor visitArrayType() {
      return this;
    }

    @Override
    public void visitClassType(String name) {
      final String classType = readClassName(name);
      addDependency("class type", classType);
    }

    @Override
    public void visitInnerClassType(String name) {
      final String innerClassType = readClassName(name);
      addDependency("inner class type", innerClassType);
    }

    @Override
    public void visitTypeArgument() {
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
      return this;
    }

    @Override
    public void visitEnd() {
    }
  }

  private final class ClassDependencyAnnotationVisitor implements AnnotationVisitor {
    @Override
    public void visit(String name, Object value) {
      final String valueType = value.getClass().getName();
      addDependency("annotation's value type", valueType);
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
      final String enumType = readTypeDescription(desc);
      addDependency("annotation's enum type", enumType);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
      final String annotationType = readTypeDescription(desc);
      addDependency("annotation's annotation type", annotationType);
      return this;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
      return this;
    }

    @Override
    public void visitEnd() {
    }

  }

  private final class ClassDependencyFieldVisitor implements FieldVisitor {
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return delegateToAnnotationVisitor(desc);
    }

    @Override
    public void visitAttribute(Attribute attr) {
    }

    @Override
    public void visitEnd() {
    }

  }

  private final class ClassDependencyMethodVisitor implements MethodVisitor {
    @Override
    public AnnotationVisitor visitAnnotationDefault() {
      return annotationVisitor;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return delegateToAnnotationVisitor(desc);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
      return delegateToAnnotationVisitor(desc);
    }

    @Override
    public void visitAttribute(Attribute attr) {
    }

    @Override
    public void visitCode() {
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
    }

    @Override
    public void visitInsn(int opcode) {
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
      final String typeName = readClassName(type);
      addDependency("Type instruction type", typeName);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
      final String fieldType = readTypeDescription(desc);
      addDependency("field instruction type", fieldType);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
      final String ownerType = readClassName(owner);
      addDependency("method owner", ownerType);

      final Type[] argumentTypes = Type.getArgumentTypes(desc);
      for (Type argumentType : argumentTypes) {
        final String parameterTypeName = readTypeName(argumentType);
        addDependency("method parameter type", parameterTypeName);
      }

      final Type returnType = Type.getReturnType(desc);
      final String returnTypeName = readTypeName(returnType);
      addDependency("method return type", returnTypeName);

    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
    }

    @Override
    public void visitLabel(Label label) {
    }

    @Override
    public void visitLdcInsn(Object cst) {
      final String constantTypeName;
      if (cst instanceof Type) {
        final Type type = (Type) cst;
        constantTypeName = type.getClassName();
      } else {
        constantTypeName = cst.getClass().getCanonicalName();
      }
      addDependency("constant's type", constantTypeName);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
      final String arrayType = readTypeDescription(desc);
      addDependency("array's type", arrayType);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
      final String exceptionType = readClassName(type);
      addDependency("exception type", exceptionType);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
      final String localVariableType = readTypeDescription(desc);
      addDependency("local variable", localVariableType);

      processSignature(signature);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
    }

    @Override
    public void visitEnd() {
    }

  }
}
