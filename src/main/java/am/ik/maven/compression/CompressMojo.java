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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Compresses static resource files using configured compressors. Scans configured
 * resource directories for files matching the specified extensions and creates compressed
 * variants.
 */
@Mojo(name = "compress", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, threadSafe = true)
public class CompressMojo extends AbstractMojo {

	/**
	 * The base output directory containing the resource directories to compress.
	 */
	@Parameter(defaultValue = "${project.build.outputDirectory}", property = "compression.outputDirectory")
	private File outputDirectory;

	/**
	 * Subdirectories under the output directory to scan for compressible files.
	 */
	@Parameter(property = "compression.resourceDirectories")
	private List<String> resourceDirectories;

	/**
	 * File extensions to target for compression (without dots).
	 */
	@Parameter(property = "compression.fileExtensions")
	private List<String> fileExtensions;

	/**
	 * Brotli compression quality (0-11). Higher values produce smaller files but take
	 * longer.
	 */
	@Parameter(property = "compression.brotli.quality", defaultValue = "11")
	private int brotliQuality;

	/**
	 * Whether to skip compression.
	 */
	@Parameter(property = "compression.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * Whether to enable Brotli compression.
	 */
	@Parameter(property = "compression.brotli.enabled", defaultValue = "true")
	private boolean brotliEnabled;

	/**
	 * Whether to enable Gzip compression.
	 */
	@Parameter(property = "compression.gzip.enabled", defaultValue = "true")
	private boolean gzipEnabled;

	@Override
	public void execute() throws MojoExecutionException {
		if (this.skip) {
			getLog().info("Compression plugin is skipped.");
			return;
		}

		List<String> directories = (this.resourceDirectories != null && !this.resourceDirectories.isEmpty())
				? this.resourceDirectories : List.of("META-INF/resources", "resources", "static", "public");
		Set<String> extensions = (this.fileExtensions != null && !this.fileExtensions.isEmpty())
				? new LinkedHashSet<>(this.fileExtensions) : StaticResourceCompressor.DEFAULT_EXTENSIONS;

		StaticResourceCompressor.Builder compressorBuilder = StaticResourceCompressor.builder().extensions(extensions);
		if (this.brotliEnabled) {
			compressorBuilder.addCompressor(new BrotliCompressor(this.brotliQuality));
		}
		if (this.gzipEnabled) {
			compressorBuilder.addCompressor(new GzipCompressor());
		}
		StaticResourceCompressor compressor = compressorBuilder.build();

		Path basePath = this.outputDirectory.toPath();
		for (String directory : directories) {
			Path resourceDir = basePath.resolve(directory);
			try {
				List<CompressionResult> results = compressor.compress(resourceDir);
				for (CompressionResult result : results) {
					logResult(result);
				}
			}
			catch (IOException ex) {
				throw new MojoExecutionException("Failed to compress resources in " + resourceDir, ex);
			}
		}
	}

	private void logResult(CompressionResult result) {
		String fileName = result.sourceFile().getFileName().toString();
		StringBuilder message = new StringBuilder();
		message.append("Compressed ").append(fileName).append(": ").append(result.originalSize()).append(" ->");
		for (Map.Entry<String, Long> entry : result.compressedSizes().entrySet()) {
			message.append(" ").append(entry.getKey()).append(":").append(entry.getValue());
		}
		getLog().info(message.toString());
	}

}
