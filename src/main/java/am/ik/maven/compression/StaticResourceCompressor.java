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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Compresses static resource files using configured {@link ResourceCompressor}
 * implementations. Files are selected by their extension within a given directory.
 */
public final class StaticResourceCompressor {

	/**
	 * Default file extensions targeted for compression. Includes common text-based web
	 * resource types that benefit from compression.
	 */
	public static final Set<String> DEFAULT_EXTENSIONS = Set.of("css", "csv", "htm", "html", "js", "json", "map", "mjs",
			"mts", "svg", "ts", "txt", "webmanifest", "xml", "yaml", "yml");

	private final Set<String> extensions;

	private final List<ResourceCompressor> compressors;

	private StaticResourceCompressor(Builder builder) {
		this.extensions = Collections.unmodifiableSet(new LinkedHashSet<>(builder.extensions));
		this.compressors = List.copyOf(builder.compressors);
	}

	/**
	 * Compresses all matching files in the given directory recursively.
	 * @param directory the directory to scan for compressible files
	 * @return a list of compression results
	 * @throws IOException if an I/O error occurs
	 */
	public List<CompressionResult> compress(Path directory) throws IOException {
		if (!Files.exists(directory) || !Files.isDirectory(directory)) {
			return List.of();
		}
		List<CompressionResult> results = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(directory)) {
			List<Path> files = paths.filter(Files::isRegularFile).filter(this::hasMatchingExtension).sorted().toList();
			for (Path file : files) {
				CompressionResult result = compressFile(file);
				results.add(result);
			}
		}
		return results;
	}

	private boolean hasMatchingExtension(Path file) {
		String fileName = file.getFileName().toString();
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex < 0) {
			return false;
		}
		String extension = fileName.substring(dotIndex + 1);
		return this.extensions.contains(extension);
	}

	private CompressionResult compressFile(Path file) throws IOException {
		long originalSize = Files.size(file);
		Map<String, Long> compressedSizes = new LinkedHashMap<>();
		for (ResourceCompressor compressor : this.compressors) {
			Path target = file.resolveSibling(file.getFileName().toString() + compressor.suffix());
			long compressedSize = compressor.compress(file, target);
			compressedSizes.put(compressor.label(), compressedSize);
		}
		return new CompressionResult(file, originalSize, compressedSizes);
	}

	/**
	 * Creates a new builder for {@link StaticResourceCompressor}.
	 * @return a new builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for {@link StaticResourceCompressor}.
	 */
	public static final class Builder {

		private Set<String> extensions = DEFAULT_EXTENSIONS;

		private final List<ResourceCompressor> compressors = new ArrayList<>();

		private Builder() {
		}

		/**
		 * Sets the file extensions to target for compression.
		 * @param extensions the set of file extensions (without dots)
		 * @return this builder
		 */
		public Builder extensions(Set<String> extensions) {
			this.extensions = extensions;
			return this;
		}

		/**
		 * Adds a {@link ResourceCompressor} to the compressor chain.
		 * @param compressor the compressor to add
		 * @return this builder
		 */
		public Builder addCompressor(ResourceCompressor compressor) {
			this.compressors.add(compressor);
			return this;
		}

		/**
		 * Builds a new {@link StaticResourceCompressor} instance.
		 * @return a new compressor
		 */
		public StaticResourceCompressor build() {
			return new StaticResourceCompressor(this);
		}

	}

}
