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
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

/**
 * {@link ResourceCompressor} implementation using Gzip compression.
 */
public final class GzipCompressor implements ResourceCompressor {

	@Override
	public String suffix() {
		return ".gz";
	}

	@Override
	public String label() {
		return "gz";
	}

	@Override
	public long compress(Path source, Path target) throws IOException {
		byte[] input = Files.readAllBytes(source);
		try (OutputStream fos = Files.newOutputStream(target); GZIPOutputStream gos = new GZIPOutputStream(fos)) {
			gos.write(input);
		}
		return Files.size(target);
	}

}
