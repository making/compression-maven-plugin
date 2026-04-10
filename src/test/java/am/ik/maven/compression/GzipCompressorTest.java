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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class GzipCompressorTest {

	@TempDir
	Path tempDir;

	@Test
	void compressCreatesGzipFile() throws Exception {
		Path source = this.tempDir.resolve("test.js");
		Files.writeString(source, "console.log('hello world');");

		Path target = this.tempDir.resolve("test.js.gz");
		GzipCompressor compressor = new GzipCompressor();
		long compressedSize = compressor.compress(source, target);

		assertThat(target).exists();
		assertThat(compressedSize).isGreaterThan(0);
		assertThat(compressedSize).isEqualTo(Files.size(target));
	}

	@Test
	void compressedContentCanBeDecompressed() throws Exception {
		String originalContent = "body { margin: 0; padding: 0; }";
		Path source = this.tempDir.resolve("style.css");
		Files.writeString(source, originalContent);

		Path target = this.tempDir.resolve("style.css.gz");
		GzipCompressor compressor = new GzipCompressor();
		compressor.compress(source, target);

		try (InputStream fis = Files.newInputStream(target); GZIPInputStream gis = new GZIPInputStream(fis)) {
			String decompressed = new String(gis.readAllBytes());
			assertThat(decompressed).isEqualTo(originalContent);
		}
	}

	@Test
	void suffixAndLabel() {
		GzipCompressor compressor = new GzipCompressor();
		assertThat(compressor.suffix()).isEqualTo(".gz");
		assertThat(compressor.label()).isEqualTo("gz");
	}

}
