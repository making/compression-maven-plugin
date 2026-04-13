/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.maven.compression;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class StaticResourceCompressorTest {

	@TempDir
	Path tempDir;

	@Test
	void compressesMatchingFiles() throws Exception {
		Path staticDir = this.tempDir.resolve("static");
		Files.createDirectories(staticDir);
		Files.writeString(staticDir.resolve("app.js"), "console.log('hello');");
		Files.writeString(staticDir.resolve("style.css"), "body { margin: 0; }");
		Files.writeString(staticDir.resolve("icon.svg"), "<svg></svg>");

		StaticResourceCompressor compressor = StaticResourceCompressor.builder()
			.addCompressor(new BrotliCompressor(11))
			.addCompressor(new GzipCompressor())
			.build();
		List<CompressionResult> results = compressor.compress(staticDir);

		assertThat(results).hasSize(3);
		assertThat(staticDir.resolve("app.js.br")).exists();
		assertThat(staticDir.resolve("app.js.gz")).exists();
		assertThat(staticDir.resolve("style.css.br")).exists();
		assertThat(staticDir.resolve("style.css.gz")).exists();
		assertThat(staticDir.resolve("icon.svg.br")).exists();
		assertThat(staticDir.resolve("icon.svg.gz")).exists();
	}

	@Test
	void compressesWithAllThreeCompressors() throws Exception {
		Path staticDir = this.tempDir.resolve("static");
		Files.createDirectories(staticDir);
		Files.writeString(staticDir.resolve("app.js"), "console.log('hello');");

		StaticResourceCompressor compressor = StaticResourceCompressor.builder()
			.addCompressor(new BrotliCompressor(11))
			.addCompressor(new GzipCompressor())
			.addCompressor(new ZstdCompressor(22))
			.build();
		List<CompressionResult> results = compressor.compress(staticDir);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).compressedSize("br")).isGreaterThan(0);
		assertThat(results.get(0).compressedSize("gz")).isGreaterThan(0);
		assertThat(results.get(0).compressedSize("zst")).isGreaterThan(0);
		assertThat(staticDir.resolve("app.js.br")).exists();
		assertThat(staticDir.resolve("app.js.gz")).exists();
		assertThat(staticDir.resolve("app.js.zst")).exists();
	}

	@Test
	void doesNotCompressNonMatchingExtensions() throws Exception {
		Path staticDir = this.tempDir.resolve("static");
		Files.createDirectories(staticDir);
		Files.writeString(staticDir.resolve("image.png"), "fake png data");
		Files.writeString(staticDir.resolve("app.js"), "console.log('hello');");

		StaticResourceCompressor compressor = StaticResourceCompressor.builder()
			.addCompressor(new BrotliCompressor(11))
			.addCompressor(new GzipCompressor())
			.build();
		List<CompressionResult> results = compressor.compress(staticDir);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).sourceFile().getFileName().toString()).isEqualTo("app.js");
		assertThat(staticDir.resolve("image.png.br")).doesNotExist();
		assertThat(staticDir.resolve("image.png.gz")).doesNotExist();
	}

	@Test
	void brotliOnlyWhenGzipNotAdded() throws Exception {
		Path staticDir = this.tempDir.resolve("static");
		Files.createDirectories(staticDir);
		Files.writeString(staticDir.resolve("app.js"), "console.log('hello');");

		StaticResourceCompressor compressor = StaticResourceCompressor.builder()
			.addCompressor(new BrotliCompressor(11))
			.build();
		List<CompressionResult> results = compressor.compress(staticDir);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).compressedSize("br")).isGreaterThan(0);
		assertThat(results.get(0).compressedSize("gz")).isEqualTo(-1);
		assertThat(staticDir.resolve("app.js.br")).exists();
		assertThat(staticDir.resolve("app.js.gz")).doesNotExist();
	}

	@Test
	void gzipOnlyWhenBrotliNotAdded() throws Exception {
		Path staticDir = this.tempDir.resolve("static");
		Files.createDirectories(staticDir);
		Files.writeString(staticDir.resolve("app.js"), "console.log('hello');");

		StaticResourceCompressor compressor = StaticResourceCompressor.builder()
			.addCompressor(new GzipCompressor())
			.build();
		List<CompressionResult> results = compressor.compress(staticDir);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).compressedSize("br")).isEqualTo(-1);
		assertThat(results.get(0).compressedSize("gz")).isGreaterThan(0);
		assertThat(staticDir.resolve("app.js.br")).doesNotExist();
		assertThat(staticDir.resolve("app.js.gz")).exists();
	}

	@Test
	void customExtensions() throws Exception {
		Path staticDir = this.tempDir.resolve("static");
		Files.createDirectories(staticDir);
		Files.writeString(staticDir.resolve("index.html"), "<html></html>");
		Files.writeString(staticDir.resolve("app.js"), "console.log('hello');");

		StaticResourceCompressor compressor = StaticResourceCompressor.builder()
			.extensions(Set.of("html"))
			.addCompressor(new BrotliCompressor(11))
			.build();
		List<CompressionResult> results = compressor.compress(staticDir);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).sourceFile().getFileName().toString()).isEqualTo("index.html");
		assertThat(staticDir.resolve("index.html.br")).exists();
		assertThat(staticDir.resolve("app.js.br")).doesNotExist();
	}

	@Test
	void emptyDirectoryReturnsEmptyList() throws Exception {
		Path staticDir = this.tempDir.resolve("static");
		Files.createDirectories(staticDir);

		StaticResourceCompressor compressor = StaticResourceCompressor.builder()
			.addCompressor(new GzipCompressor())
			.build();
		List<CompressionResult> results = compressor.compress(staticDir);

		assertThat(results).isEmpty();
	}

	@Test
	void nonExistentDirectoryReturnsEmptyList() throws Exception {
		Path nonExistent = this.tempDir.resolve("nonexistent");

		StaticResourceCompressor compressor = StaticResourceCompressor.builder()
			.addCompressor(new GzipCompressor())
			.build();
		List<CompressionResult> results = compressor.compress(nonExistent);

		assertThat(results).isEmpty();
	}

	@Test
	void compressesFilesInSubdirectories() throws Exception {
		Path staticDir = this.tempDir.resolve("static");
		Path subDir = staticDir.resolve("sub");
		Files.createDirectories(subDir);
		Files.writeString(staticDir.resolve("app.js"), "console.log('root');");
		Files.writeString(subDir.resolve("nested.js"), "console.log('nested');");

		StaticResourceCompressor compressor = StaticResourceCompressor.builder()
			.addCompressor(new BrotliCompressor(11))
			.addCompressor(new GzipCompressor())
			.build();
		List<CompressionResult> results = compressor.compress(staticDir);

		assertThat(results).hasSize(2);
		assertThat(staticDir.resolve("app.js.br")).exists();
		assertThat(subDir.resolve("nested.js.br")).exists();
	}

	@Test
	void compressionResultContainsCorrectSizes() throws Exception {
		Path staticDir = this.tempDir.resolve("static");
		Files.createDirectories(staticDir);
		String content = "console.log('hello world');";
		Files.writeString(staticDir.resolve("app.js"), content);

		StaticResourceCompressor compressor = StaticResourceCompressor.builder()
			.addCompressor(new BrotliCompressor(11))
			.addCompressor(new GzipCompressor())
			.build();
		List<CompressionResult> results = compressor.compress(staticDir);

		assertThat(results).hasSize(1);
		CompressionResult result = results.get(0);
		assertThat(result.originalSize()).isEqualTo(content.getBytes().length);
		assertThat(result.compressedSize("br")).isGreaterThan(0);
		assertThat(result.compressedSize("gz")).isGreaterThan(0);
	}

	@Test
	void noCompressorsProducesEmptyCompressedSizes() throws Exception {
		Path staticDir = this.tempDir.resolve("static");
		Files.createDirectories(staticDir);
		Files.writeString(staticDir.resolve("app.js"), "console.log('hello');");

		StaticResourceCompressor compressor = StaticResourceCompressor.builder().build();
		List<CompressionResult> results = compressor.compress(staticDir);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).compressedSizes()).isEmpty();
	}

}
