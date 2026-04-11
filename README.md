# Compression Maven Plugin

A Maven plugin that compresses static resources using [Brotli](https://github.com/google/brotli) and Gzip.

## When to use this plugin

If your web application serves static resources (JavaScript, CSS, SVG, etc.) and your web server supports serving pre-compressed files, this plugin generates `.br` and `.gz` variants at build time so the server can serve them directly without compressing on every request.

This plugin is designed to work with Spring Framework's `ResourceResolver` chain. The default resource directories and file extensions match Spring Boot's conventions out of the box. See [Spring Boot integration](#spring-boot-integration) for setup details.

## Usage

Add the plugin to your `pom.xml`:

```xml
<plugin>
    <groupId>am.ik.maven</groupId>
    <artifactId>compression-maven-plugin</artifactId>
    <version>0.2.0</version>
    <executions>
        <execution>
            <goals>
                <goal>compress</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

By default, this compresses all text-based static resource files under Spring Boot's standard static resource directories (`META-INF/resources`, `resources`, `static`, `public`) in `${project.build.outputDirectory}` during the `compile` phase. For each file, it creates:

- `<filename>.br` -- Brotli compressed (quality 11)
- `<filename>.gz` -- Gzip compressed

### Example: Custom extensions and directories

```xml
<configuration>
    <fileExtensions>
        <fileExtension>js</fileExtension>
        <fileExtension>css</fileExtension>
        <fileExtension>svg</fileExtension>
        <fileExtension>html</fileExtension>
        <fileExtension>json</fileExtension>
    </fileExtensions>
    <resourceDirectories>
        <resourceDirectory>static</resourceDirectory>
        <resourceDirectory>public</resourceDirectory>
    </resourceDirectories>
</configuration>
```

### Example: Brotli only

```xml
<configuration>
    <gzipEnabled>false</gzipEnabled>
</configuration>
```

### Example: Gzip only

```xml
<configuration>
    <brotliEnabled>false</brotliEnabled>
</configuration>
```

### Example: Lower Brotli quality for faster builds

```xml
<configuration>
    <brotliQuality>6</brotliQuality>
</configuration>
```

Or using Maven properties:

```xml
<properties>
    <compression.brotli.quality>6</compression.brotli.quality>
</properties>
```

## Configuration

All configuration parameters can be set either in the plugin `<configuration>` block or as Maven properties.

| Parameter | Property | Default | Description |
|---|---|---|---|
| `outputDirectory` | `compression.outputDirectory` | `${project.build.outputDirectory}` | Base directory containing the resource directories |
| `resourceDirectories` | `compression.resourceDirectories` | `META-INF/resources`, `resources`, `static`, `public` | Subdirectories under the output directory to scan |
| `fileExtensions` | `compression.fileExtensions` | (see below) | File extensions to compress (without dots) |
| `brotliQuality` | `compression.brotli.quality` | `11` | Brotli compression quality (0-11) |
| `brotliEnabled` | `compression.brotli.enabled` | `true` | Enable Brotli compression |
| `gzipEnabled` | `compression.gzip.enabled` | `true` | Enable Gzip compression |
| `skip` | `compression.skip` | `false` | Skip the plugin |

### Default file extensions

The following text-based file extensions are compressed by default:

`css`, `csv`, `htm`, `html`, `js`, `json`, `map`, `mjs`, `mts`, `svg`, `ts`, `txt`, `wasm`, `webmanifest`, `xml`, `yaml`, `yml`

These are common web resource types that benefit significantly from compression. Binary formats (images, fonts, etc.) are already compressed and should not be included.

### Brotli quality

The `brotliQuality` parameter controls the trade-off between compression ratio and build time:

| Quality | Speed | Compression ratio |
|---|---|---|
| 0-4 | Fast | Lower |
| 5-9 | Moderate | Good |
| 10-11 | Slow | Best |

The default value of 11 gives the best compression. For faster builds (e.g., during development), a lower value like 4-6 may be preferable.

## Spring Boot integration

This plugin generates pre-compressed files that Spring Framework's `EncodedResourceResolver` can serve automatically. To enable this, add the following property to your `application.properties`:

```properties
spring.web.resources.chain.enabled=true
spring.web.resources.chain.compressed=true
```

When this property is enabled, Spring Boot will look for `.br` or `.gz` variants of the requested resource and serve the compressed version if the client supports it (via the `Accept-Encoding` header).

For more details, see the Spring documentation:

- [Spring MVC - Static Resources](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/static-resources.html)
- [Spring Boot - Static Content](https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.spring-mvc.static-content)

## Requirements

- Java 17+
- Maven 3.9+

## License

Licensed under the Apache License, Version 2.0.
