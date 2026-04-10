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

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.encoder.Encoder;

/**
 * {@link ResourceCompressor} implementation using Brotli compression.
 */
public final class BrotliCompressor implements ResourceCompressor {

	private final int quality;

	/**
	 * Creates a new Brotli compressor with the specified quality.
	 * @param quality the Brotli compression quality (0-11)
	 */
	public BrotliCompressor(int quality) {
		this.quality = quality;
	}

	@Override
	public String suffix() {
		return ".br";
	}

	@Override
	public String label() {
		return "br";
	}

	@Override
	public long compress(Path source, Path target) throws IOException {
		Brotli4jLoader.ensureAvailability();
		Encoder.Parameters params = new Encoder.Parameters().setQuality(this.quality);
		byte[] input = Files.readAllBytes(source);
		byte[] compressed = Encoder.compress(input, params);
		Files.write(target, compressed);
		return compressed.length;
	}

}
