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
import java.nio.file.Path;

/**
 * Interface for a resource compressor that produces a compressed variant of a file.
 */
public interface ResourceCompressor {

	/**
	 * Returns the file extension suffix appended to the compressed file (e.g.,
	 * {@code ".br"}, {@code ".gz"}).
	 * @return the file extension suffix
	 */
	String suffix();

	/**
	 * Returns a short label used in log output (e.g., {@code "br"}, {@code "gz"}).
	 * @return the label
	 */
	String label();

	/**
	 * Compresses the source file and writes the result to the target file.
	 * @param source the file to compress
	 * @param target the output compressed file
	 * @return the size of the compressed file in bytes
	 * @throws IOException if an I/O error occurs
	 */
	long compress(Path source, Path target) throws IOException;

}
