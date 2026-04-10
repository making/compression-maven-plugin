package com.example;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.zip.GZIPInputStream;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class StaticResourceCompressionTest {

	@LocalServerPort
	int port;

	@Test
	void brotliCompressedResourceIsServed() throws Exception {
		Brotli4jLoader.ensureAvailability();
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:" + this.port + "/app.js"))
			.header("Accept-Encoding", "br")
			.GET()
			.build();
		HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.headers().firstValue("Content-Encoding")).hasValue("br");
		try (InputStream bis = new BrotliInputStream(new ByteArrayInputStream(response.body()))) {
			String decoded = new String(bis.readAllBytes());
			assertThat(decoded).contains("console.log(\"hello world\");");
		}
	}

	@Test
	void gzipCompressedResourceIsServed() throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:" + this.port + "/app.js"))
			.header("Accept-Encoding", "gzip")
			.GET()
			.build();
		HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.headers().firstValue("Content-Encoding")).hasValue("gzip");
		try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(response.body()))) {
			String decoded = new String(gis.readAllBytes());
			assertThat(decoded).contains("console.log(\"hello world\");");
		}
	}

}
