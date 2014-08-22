package de.is24.maven.enforcer.rules;

import org.apache.maven.plugin.logging.Log;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * ClassDependencyResolvingVisitor
 *
 * @author aschubert
 */
final class ClassDependencyResolvingVisitor extends ClassVisitor {
    private static final String VOID = "V";
    private final Repository repository;
    private final Log logger;
    private final AnnotationVisitor annotationVisitor =
            new ClassDependencyAnnotationVisitor();
    private final FieldVisitor fieldVisitor = new ClassDependencyFieldVisitor();
    private final MethodVisitor methodVisitor =
            new ClassDependencyMethodVisitor();
    private final SignatureVisitor signatureVisitor =
            new ClassDependencySignatureVisitor();


    ClassDependencyResolvingVisitor(Repository repository, Log logger) {
        super(Opcodes.ASM5);
        this.repository = repository;
        this.logger = logger;
    }

    @Override
    public void visit(int version, int access, String name,
                      String signature, String superName, String[] interfaces) {
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
    public void visitOuterClass(String owner, String name,
                                String desc) {
        logger.debug("visit outer class " + desc);
    }

    @Override
    public FieldVisitor visitField(int access, String name,
                                   String desc, String signature, Object value) {
        final String fieldType = readTypeDescription(desc);
        addDependency("field type", fieldType);

        processSignature(signature);

        return fieldVisitor;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc,
                                             boolean visible) {
        return delegateToAnnotationVisitor(desc);
    }

    @Override
    public void visitInnerClass(String name, String outerName,
                                String innerName, int access) {
        final String innerClassName = readClassName(name);
        addDependency("inner class", innerClassName);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name,
                                     String desc, String signature, String[] exceptions) {
        final Type[] argumentTypes = Type.getArgumentTypes(desc);

        for (Type argumentType : argumentTypes) {
            final String parameterTypeName = readTypeName(argumentType);
            addDependency("annotation's method parameter type",
                    parameterTypeName);
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

    private AnnotationVisitor delegateToAnnotationVisitor(String desc) {
        final String annotationType = readTypeDescription(desc);
        addDependency("annotation", annotationType);

        return annotationVisitor;
    }

    private void addDependency(String typeDescription, String typeName) {
        logger.debug("Add " + typeDescription + " type '" + typeName +
                "' as dependency.");

        // remove brackets in case of an array type..
        final String typeNameWithoutBrackets = typeName.replaceAll("(\\[|\\])", "");
        repository.addDependency(typeNameWithoutBrackets);
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
            final SignatureReader signatureReader = new SignatureReader(
                    signature);
            signatureReader.accept(signatureVisitor);
        }
    }

    private final class ClassDependencySignatureVisitor
            extends SignatureVisitor {

        private ClassDependencySignatureVisitor() {
            super(Opcodes.ASM5);
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
    }

    private final class ClassDependencyAnnotationVisitor
            extends AnnotationVisitor {

        private ClassDependencyAnnotationVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visit(String name, Object value) {
            final String valueType = value.getClass().getName();
            addDependency("annotation's value type", valueType);
        }

        @Override
        public void visitEnum(String name, String desc,
                              String value) {
            final String enumType = readTypeDescription(desc);
            addDependency("annotation's enum type", enumType);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name,
                                                 String desc) {
            final String annotationType = readTypeDescription(desc);
            addDependency("annotation's annotation type", annotationType);

            return this;
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return this;
        }
    }

    private final class ClassDependencyFieldVisitor extends FieldVisitor {

        private ClassDependencyFieldVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc,
                                                 boolean visible) {
            return delegateToAnnotationVisitor(desc);
        }
    }

    private final class ClassDependencyMethodVisitor extends MethodVisitor {

        private ClassDependencyMethodVisitor() {
            super(Opcodes.ASM5);
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            return annotationVisitor;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc,
                                                 boolean visible) {
            return delegateToAnnotationVisitor(desc);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(
                int parameter, String desc, boolean visible) {
            return delegateToAnnotationVisitor(desc);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            final String typeName = readClassName(type);
            addDependency("Type instruction type", typeName);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner,
                                   String name, String desc) {
            final String fieldType = readTypeDescription(desc);
            addDependency("field instruction type", fieldType);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner,
                                    String name, String desc, boolean itf) {
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
        public void visitMultiANewArrayInsn(String desc, int dims) {
            final String arrayType = readTypeDescription(desc);
            addDependency("array's type", arrayType);
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end,
                                       Label handler, String type) {
            final String exceptionType = readClassName(type);
            addDependency("exception type", exceptionType);
        }

        @Override
        public void visitLocalVariable(String name, String desc,
                                       String signature, Label start, Label end, int index) {
            final String localVariableType = readTypeDescription(desc);
            addDependency("local variable", localVariableType);

            processSignature(signature);
        }
    }
}
