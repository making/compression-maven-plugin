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

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Result of compressing a single file with one or more compressors.
 *
 * @param sourceFile the original file path
 * @param originalSize the original file size in bytes
 * @param compressedSizes a map from compressor label to compressed size in bytes
 */
public record CompressionResult(Path sourceFile, long originalSize, Map<String, Long> compressedSizes) {

	/**
	 * Creates a new {@link CompressionResult} with an unmodifiable copy of the given map.
	 * @param sourceFile the original file path
	 * @param originalSize the original file size in bytes
	 * @param compressedSizes a map from compressor label to compressed size in bytes
	 */
	public CompressionResult(Path sourceFile, long originalSize, Map<String, Long> compressedSizes) {
		this.sourceFile = sourceFile;
		this.originalSize = originalSize;
		this.compressedSizes = Collections.unmodifiableMap(new LinkedHashMap<>(compressedSizes));
	}

	/**
	 * Returns the compressed size for the given compressor label, or -1 if not compressed
	 * by that compressor.
	 * @param label the compressor label (e.g., "br", "gz")
	 * @return the compressed size in bytes, or -1
	 */
	public long compressedSize(String label) {
		return this.compressedSizes.getOrDefault(label, -1L);
	}

}
