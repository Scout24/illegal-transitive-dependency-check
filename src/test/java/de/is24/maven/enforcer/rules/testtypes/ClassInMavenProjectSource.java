package de.is24.maven.enforcer.rules.testtypes;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("all")
public class ClassInMavenProjectSource<X extends String> implements Serializable {
    private final double[][] doubleDoubleArray = new double[1][1];

    private final byte b = 0;
    private final short s = 1;
    private final char c = 2;
    private final float f = 3;
    private final double d = 4;
    private final int i = 5;
    private final Object[][] l = {{6}, {6}};

    ClassInDirectDependency referenceClassInDirectAndTransitiveDependency(
            ClassInTransitiveDependency referenceToClassInTransitiveDependency, double d, Float f, X string) {
        final ClassInTransitiveDependency localReference = referenceToClassInTransitiveDependency;

        try {
            final Long longValue = someTee(Long.class);
        } catch (SQLException e) {
            //
        }

        final int k = 3 + 4;

        final Date date = new Date(System.currentTimeMillis());

        float m = Math.max(1, 2);

        final Class<String[]> stringArrayClass = String[].class;
        final Class<Long[][]> longArrayArrayClass = Long[][].class;

        final ClassInDirectDependency directDependency = new ClassInDirectDependency();
        final String s = String.valueOf(directDependency.getOne());
        return directDependency;
    }

    public static <T extends Number> T someTee(@SuppressWarnings("egal") Class<T> someTeeClass) throws SQLException {
        try {
            return someTeeClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @ClassInTransitiveDependency.SomeUsefulAnnotation(arrayParameter = {"eins", "zwei", "drei"})
    public ClassInTransitiveDependency referenceToTransitiveClass;
    public ClassInAnotherTransitiveDependency classInAnotherTransitiveDependency;

    public final int integerValue = 0;
    public final long[] integerValueArray = {};

    public final Double doubleValue = 0d;
    public final Double[] doubleValueArray = {};

    public final Set<ClassInTransitiveDependency.SomeUsefulAnnotation> set = new HashSet<>();

    private static final String[][] XXX = {
            {"A", "B"}
    };
}
