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
profile and it will automatically verify the dependencies have not changed using the lockfiles saved in this commit.
```shell
mvn clean install
```
You can inspect the maven output to confirm the lockfile validation was run and is successful.
```console
[INFO] --- lockfile:5.1.0:validate (default) @ combine-submodules ---
[INFO] Validating lock file ...
[WARNING] No config was found in the lock file. Using default config.
[INFO] Generating lock file for project combine-submodules
[INFO] Lockfile successfully validated.
```

### conflicting-change
The Maven project after a conflicting transitive dependency is made. This will clearly show the conflicting transitive dependencies in the lockfile validation.
```shell
mvn clean install -Pconflicting-change
```
You can see the lockfile validation clearly fails in the maven console output
```console
[INFO] --- lockfile:5.1.0:validate (default) @ sub-module-a ---
[INFO] Validating lock file ...
[WARNING] No config was found in the lock file. Using default config.
[INFO] Generating lock file for project sub-module-a
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for transitive-depednencies 0.0.1-SNAPSHOT:
[INFO]
[INFO] transitive-depednencies ............................ SUCCESS [  0.593 s]
[INFO] sub-module-a ....................................... FAILURE [  1.227 s]
[INFO] sub-module-b ....................................... SKIPPED
[INFO] combine-submodules ................................. SKIPPED
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.916 s
[INFO] Finished at: 2024-06-18T05:58:20-06:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal io.github.chains-project:maven-lockfile:5.1.0:validate (default) on project sub-module-a: Failed verifying lock fileLock file validation failed. Differences:
[ERROR] Your lockfile from file is for:org.demo:sub-module-a:0.0.1-SNAPSHOT
[ERROR] Your generated lockfile is for:org.demo:sub-module-a:0.0.1-SNAPSHOT
[ERROR] Missing dependencies in lock file:
[ERROR]  [
[ERROR]   {
[ERROR]     "groupId": "com.google.guava",
[ERROR]     "artifactId": "guava",
[ERROR]     "version": "22.0",
[ERROR]     "checksumAlgorithm": "SHA-256",
[ERROR]     "checksum": "1158e94c7de4da480873f0b4ab4a1da14c0d23d4b1902cc94a58a6f0f9ab579e",
[ERROR]     "scope": "compile",
[ERROR]     "selectedVersion": "22.0",
[ERROR]     "included": true,
[ERROR]     "id": "com.google.guava:guava:22.0",
[ERROR]     "children": [
[ERROR]       {
[ERROR]         "groupId": "com.google.code.findbugs",
[ERROR]         "artifactId": "jsr305",
[ERROR]         "version": "1.3.9",
[ERROR]         "checksumAlgorithm": "SHA-256",
[ERROR]         "checksum": "905721a0eea90a81534abb7ee6ef4ea2e5e645fa1def0a5cd88402df1b46c9ed",
[ERROR]         "scope": "compile",
[ERROR]         "selectedVersion": "1.3.9",
[ERROR]         "included": true,
[ERROR]         "id": "com.google.code.findbugs:jsr305:1.3.9",
[ERROR]         "parent": "com.google.guava:guava:22.0",
[ERROR]         "children": []
[ERROR]       },
[ERROR]       {
[ERROR]         "groupId": "com.google.errorprone",
[ERROR]         "artifactId": "error_prone_annotations",
[ERROR]         "version": "2.0.18",
[ERROR]         "checksumAlgorithm": "SHA-256",
[ERROR]         "checksum": "cb4cfad870bf563a07199f3ebea5763f0dec440fcda0b318640b1feaa788656b",
[ERROR]         "scope": "compile",
[ERROR]         "selectedVersion": "2.0.18",
[ERROR]         "included": true,
[ERROR]         "id": "com.google.errorprone:error_prone_annotations:2.0.18",
[ERROR]         "parent": "com.google.guava:guava:22.0",
[ERROR]         "children": []
[ERROR]       },
[ERROR]       {
[ERROR]         "groupId": "com.google.j2objc",
[ERROR]         "artifactId": "j2objc-annotations",
[ERROR]         "version": "1.1",
[ERROR]         "checksumAlgorithm": "SHA-256",
[ERROR]         "checksum": "2994a7eb78f2710bd3d3bfb639b2c94e219cedac0d4d084d516e78c16dddecf6",
[ERROR]         "scope": "compile",
[ERROR]         "selectedVersion": "1.1",
[ERROR]         "included": true,
[ERROR]         "id": "com.google.j2objc:j2objc-annotations:1.1",
[ERROR]         "parent": "com.google.guava:guava:22.0",
[ERROR]         "children": []
[ERROR]       },
[ERROR]       {
[ERROR]         "groupId": "org.codehaus.mojo",
[ERROR]         "artifactId": "animal-sniffer-annotations",
[ERROR]         "version": "1.14",
[ERROR]         "checksumAlgorithm": "SHA-256",
[ERROR]         "checksum": "2068320bd6bad744c3673ab048f67e30bef8f518996fa380033556600669905d",
[ERROR]         "scope": "compile",
[ERROR]         "selectedVersion": "1.14",
[ERROR]         "included": true,
[ERROR]         "id": "org.codehaus.mojo:animal-sniffer-annotations:1.14",
[ERROR]         "parent": "com.google.guava:guava:22.0",
[ERROR]         "children": []
[ERROR]       }
[ERROR]     ]
[ERROR]   }
[ERROR] ]
[ERROR] Missing dependencies in project:
[ERROR]  []
[ERROR] Missing plugins in lockfile:
[ERROR]  []
[ERROR] Missing plugins in project:
[ERROR]  []
[ERROR]
[ERROR] -> [Help 1]
[ERROR]
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR]
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
[ERROR]
[ERROR] After correcting the problems, you can resume the build with the command
[ERROR]   mvn <args> -rf :sub-module-a
```

### conflict-resolved

The Maven project after the conflicting transitive dependency is resolved. You'll see the first build fails the lockfile check for the repo because submodule-a now includes guava:22.0
```shell
mvn clean install -Pconflict-resolved
```
_NOTE: Error output not shown to keep this README short._

To correct this we need to regenerate the lockfile.
```shell
mvn clean install -Pconflict-resolved -Pgenerate-lockfile
```
After running the above command you'll notice that git shows the sub-module-a lockfile.json has been updated. It now includes guava:22.0 and it's dependencies. But also notice the combine-submodules lockfile.json has not changed even though its dependency sub-module-a has changed. This is due to the exclusion of guava in the sub-module-a dependency. 

Run the build again to confirm it now passes the new lockfiles work with the build.
```shell
mvn clean install -Pconflict-resolved
```
If you inspect the git diff you'll see the change to the submodule-a lockfile.json only and the combine-submodules lockfile.json has not changed due to the exclusion. At this point with the changes working and the new lockfiles generated the work can be commited including the new lockfiles and pushed into the repo so going forward a `mvn clean install` will build and verify the lockfiles.

## Command Line Maven-Lockfile

How [Maven-Lockfile](https://github.com/chains-project/maven-lockfile) can help assure all dependencies including transitive build exactly the same regardless of build
environment. Ideally this will be done for each released version. NOTE: you can see maven-lockfile is actively maintained at [MVN Repository](https://mvnrepository.com/artifact/io.github.chains-project/maven-lockfile-parent) and is the same project for 

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
