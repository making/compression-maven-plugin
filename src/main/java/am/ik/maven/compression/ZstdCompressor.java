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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.luben.zstd.Zstd;

/**
 * {@link ResourceCompressor} implementation using Zstandard compression.
 */
public final class ZstdCompressor implements ResourceCompressor {

	private final int level;

	/**
	 * Creates a new Zstandard compressor with the specified compression level.
	 * @param level the Zstandard compression level. The practical range is 1-22, where 22
	 * is the maximum and levels 20-22 enable "ultra" mode (slower and more memory
	 * intensive but produces the smallest output).
	 */
	public ZstdCompressor(int level) {
		this.level = level;
	}

	@Override
	public String suffix() {
		return ".zst";
	}

	@Override
	public String label() {
		return "zst";
	}

	@Override
	public long compress(Path source, Path target) throws IOException {
		byte[] input = Files.readAllBytes(source);
		byte[] compressed = Zstd.compress(input, this.level);
		Files.write(target, compressed);
		return compressed.length;
	}

}
