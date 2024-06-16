# Catching Breaking Transitive Dependency Changes

A demonstration of how maven-lockfile can be used to help identify when transitive dependencies have unexpectedly
changed and how to assure this version of the build is exactly the same regardless of where or how it was run.

Profiles are being used to more easily demonstrate an issue that would happen from a previous version to a new version
of this project.

## Explanation of the change that results in a breaking transitive dependency

### [ConflictTest](./combine-submodules/src/test/java/org/demo/ConflictTest.java) is dependent on method Futures.immediateVoidFuture() found in guava:29.0-jre

```java

@Test
public void whenVersionCollisionDoesNotExist_thenShouldCompile() {
    assertNotNull(Futures.immediateVoidFuture());
}
```

### [sub-module-a](./sub-module-a/pom.xml) uses profiles to initially have no dependency changes and later have a breaking dependency change on guava:22.0 which does not include method Futures.immediateVoidFuture() so the junit test produces an error

#### before-change

```xml

<dependencies>
</dependencies>
```

#### conflicting-change

```xml

<dependencies>
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>22.0</version>
    </dependency>
</dependencies>
```

#### conflict-resolved

The dependencies are not changed in the resolution profile because this isn't where it needs to be fixed. It needs to be
fixed in [combine-submodules](./combine-submodules/pom.xml)

```xml

<dependencies>
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>22.0</version>
    </dependency>
</dependencies>
```

## Explanation of the resolution to the breaking transitive dependency

The breaking transitive dependency change is due to a dependency change in [sub-module-a](./sub-module-a/pom.xml) that
adds guava:22.0. The solution is to exclude guava from this dependency in [combine-submodules](./combine-submodules/pom.xml)  because that is where the
conflict exists and causes problems.

#### conflict-resolved
```xml

<dependencies>
    <dependency>
        <groupId>org.demo</groupId>
        <artifactId>sub-module-a</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <exclusions>
            <exclusion>
                <groupId>com.google.guava</groupId>
                <artifactId>guava</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.demo</groupId>
        <artifactId>sub-module-b</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>

    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-engine</artifactId>
        <version>5.9.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Demonstrating a transitive dependency issue and how to detect it using maven-lockfile

### before-change

The Maven project that builds fine before a conflicting transitive dependency change is made. This is the default
profile. To demonstrate it builds fine use:

```shell
mvn clean install
```

### conflicting-change

The Maven project after a conflicting transitive dependency is made. To demonstrate:

```shell
mvn clean install -Pconflicting-change
```

### conflict-resolved

The Maven project after the conflicting transitive dependency is resolved. To demonstrate:

```shell
mvn clean install -Pconflict-resolved
```

## Maven-Lockfile

How Maven-Lockfile can help assure all dependencies including transitive build exactly the same regardless of build
environment. Ideally this will be done for each released version.

### Build the lockfile for the current working version

```shell
mvn io.github.chains-project:maven-lockfile:generate
```

### Test the lockfile for the current working version

```shell
mvn io.github.chains-project:maven-lockfile:validate
```

### Test the lockfile after a new dependency is added that breaks transitive dependencies

This will demonstrate how lockfile will identify transitive dependency issues

```shell
mvn io.github.chains-project:maven-lockfile:validate -Pconflicting-change
```
