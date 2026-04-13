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

import com.github.luben.zstd.ZstdInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class ZstdCompressorTest {

	@TempDir
	Path tempDir;

	@Test
	void compressCreatesZstFile() throws Exception {
		Path source = this.tempDir.resolve("test.js");
		Files.writeString(source, "console.log('hello world');");

		Path target = this.tempDir.resolve("test.js.zst");
		ZstdCompressor compressor = new ZstdCompressor(22);
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

		Path target = this.tempDir.resolve("style.css.zst");
		ZstdCompressor compressor = new ZstdCompressor(22);
		compressor.compress(source, target);

		try (ZstdInputStream zis = new ZstdInputStream(Files.newInputStream(target))) {
			String decompressed = new String(zis.readAllBytes());
			assertThat(decompressed).isEqualTo(originalContent);
		}
	}

	@Test
	void compressWithLowLevel() throws Exception {
		Path source = this.tempDir.resolve("test.js");
		Files.writeString(source, "console.log('hello world');");

		Path target = this.tempDir.resolve("test.js.zst");
		ZstdCompressor compressor = new ZstdCompressor(1);
		long compressedSize = compressor.compress(source, target);

		assertThat(target).exists();
		assertThat(compressedSize).isGreaterThan(0);
	}

	@Test
	void suffixAndLabel() {
		ZstdCompressor compressor = new ZstdCompressor(22);
		assertThat(compressor.suffix()).isEqualTo(".zst");
		assertThat(compressor.label()).isEqualTo("zst");
	}

}
