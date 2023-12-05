# Bug Report for GraalVM

Showcase a bug where native image behaves different from the JVM where `.isAssignableFrom(someClazz)` delivers a
different result than the JVM. This only happens for optimization levels greater than `-O0`.

To reproduce, run `./gradlew test` and see a successful JVM run. Now run `./gradlew nativeTest` and see a raised error:
`java.lang.IllegalArgumentException: Unsupported response type: [B`.

This is due to the fact that now for the following simplified code, the `isAssignableFrom(type)` is not taken anymore:

```java
enum SupportedResponses {
  TEXT(String.class), INPUT_STREAM(InputStream.class), READER(Reader.class), BYTE_ARRAY(byte[].class);

  private final Class<?> type;

  <T> SupportedResponses(Class<T> type) {
    this.type = type;
  }

  public static SupportedResponses from(Class<?> type) {
    for (SupportedResponses sr : SupportedResponses.values()) {
      if (sr.type.isAssignableFrom(type)) {
        return sr;
      }
    }
    throw new IllegalArgumentException("Unsupported response type: " + type.getName());
  }
}
```

Find can find the issue at https://github.com/oracle/graal/issues/7966