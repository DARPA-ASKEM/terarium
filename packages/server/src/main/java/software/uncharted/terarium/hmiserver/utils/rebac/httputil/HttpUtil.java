/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.uncharted.terarium.hmiserver.utils.rebac.httputil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.SSLContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.keycloak.util.JsonSerialization;

/** @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a> */
@Slf4j
public class HttpUtil {

	public static final String APPLICATION_XML = "application/xml";
	public static final String APPLICATION_JSON = "application/json";
	public static final String APPLICATION_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
	public static final String UTF_8 = "utf-8";

	private static HttpClient httpClient;
	private static SSLConnectionSocketFactory sslsf;
	private static final AtomicBoolean tlsWarningEmitted = new AtomicBoolean();

	public static InputStream doGet(final String url, final String acceptType, final String authorization) {
		try {
			final HttpGet request = new HttpGet(url);
			request.setHeader(HttpHeaders.ACCEPT, acceptType);
			return doRequest(authorization, request);
		} catch (final IOException e) {
			throw new RuntimeException("Failed to send request - " + e.getMessage(), e);
		}
	}

	public static InputStream doPost(
		final String url,
		final String contentType,
		final String acceptType,
		final String content,
		final String authorization
	) {
		try {
			return doPostOrPut(contentType, acceptType, content, authorization, new HttpPost(url));
		} catch (final IOException e) {
			throw new RuntimeException("Failed to send request - " + e.getMessage(), e);
		}
	}

	public static InputStream doPut(
		final String url,
		final String contentType,
		final String acceptType,
		final String content,
		final String authorization
	) {
		try {
			return doPostOrPut(contentType, acceptType, content, authorization, new HttpPut(url));
		} catch (final IOException e) {
			throw new RuntimeException("Failed to send request - " + e.getMessage(), e);
		}
	}

	public static void doDelete(final String url, final String authorization) {
		try {
			final HttpDelete request = new HttpDelete(url);
			doRequest(authorization, request);
		} catch (final IOException e) {
			throw new RuntimeException("Failed to send request - " + e.getMessage(), e);
		}
	}

	public static HeadersBodyStatus doGet(final String url, final HeadersBody request) throws IOException {
		return doRequest("get", url, request);
	}

	public static HeadersBodyStatus doPost(final String url, final HeadersBody request) throws IOException {
		return doRequest("post", url, request);
	}

	public static HeadersBodyStatus doPut(final String url, final HeadersBody request) throws IOException {
		return doRequest("put", url, request);
	}

	public static HeadersBodyStatus doDelete(final String url, final HeadersBody request) throws IOException {
		return doRequest("delete", url, request);
	}

	public static HeadersBodyStatus doRequest(final String type, final String url, final HeadersBody request)
		throws IOException {
		final HttpRequestBase req;
		switch (type) {
			case "get":
				req = new HttpGet(url);
				break;
			case "post":
				req = new HttpPost(url);
				break;
			case "put":
				req = new HttpPut(url);
				break;
			case "delete":
				req = new HttpDelete(url);
				break;
			case "options":
				req = new HttpOptions(url);
				break;
			case "head":
				req = new HttpHead(url);
				break;
			default:
				throw new RuntimeException("Method not supported: " + type);
		}
		addHeaders(req, request.getHeaders());

		if (request.getBody() != null) {
			if (!(req instanceof HttpEntityEnclosingRequestBase)) {
				throw new RuntimeException("Request type does not support body: " + type);
			}
			((HttpEntityEnclosingRequestBase) req).setEntity(new InputStreamEntity(request.getBody()));
		}

		final HttpResponse res = getHttpClient().execute(req);
		final InputStream responseStream;
		if (res.getEntity() != null) {
			responseStream = res.getEntity().getContent();
		} else {
			responseStream = new InputStream() {
				@Override
				public int read() throws IOException {
					return -1;
				}
			};
		}

		final Headers headers = new Headers();
		final HeaderIterator it = res.headerIterator();
		while (it.hasNext()) {
			final org.apache.http.Header header = it.nextHeader();
			headers.add(header.getName(), header.getValue());
		}

		return new HeadersBodyStatus(res.getStatusLine().toString(), headers, responseStream);
	}

	private static void addHeaders(final HttpRequestBase request, final Headers headers) {
		for (final Header header : headers) {
			request.setHeader(header.getName(), header.getValue());
		}
	}

	private static InputStream doPostOrPut(
		final String contentType,
		final String acceptType,
		final String content,
		final String authorization,
		final HttpEntityEnclosingRequestBase request
	) throws IOException {
		request.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
		request.setHeader(HttpHeaders.ACCEPT, acceptType);
		if (content != null) {
			request.setEntity(new StringEntity(content));
		}

		return doRequest(authorization, request);
	}

	private static InputStream doRequest(final String authorization, final HttpRequestBase request) throws IOException {
		addAuth(request, authorization);

		final HttpResponse response = getHttpClient().execute(request);
		InputStream responseStream = null;
		if (response.getEntity() != null) {
			responseStream = response.getEntity().getContent();
		}

		final int code = response.getStatusLine().getStatusCode();
		if (code >= 200 && code < 300) {
			return responseStream;
		} else {
			Map<String, String> error = null;
			try {
				final org.apache.http.Header header = response.getEntity().getContentType();
				if (header != null && APPLICATION_JSON.equals(header.getValue())) {
					error = JsonSerialization.readValue(responseStream, Map.class);
				}
			} catch (final Exception e) {
				throw new RuntimeException("Failed to read error response " + e);
			} finally {
				if (responseStream != null) {
					responseStream.close();
				}
			}

			String message = null;
			if (error != null) {
				message = error.get("error_description") + " [" + error.get("error") + "]";
			}
			throw new RuntimeException(
				message != null
					? message
					: response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase()
			);
		}
	}

	private static void addAuth(final HttpRequestBase request, final String authorization) {
		if (authorization != null) {
			request.setHeader(HttpHeaders.AUTHORIZATION, authorization);
		}
	}

	public static HttpClient getHttpClient() {
		if (httpClient == null) {
			if (sslsf != null) {
				httpClient = HttpClientBuilder.create().useSystemProperties().setSSLSocketFactory(sslsf).build();
			} else {
				httpClient = HttpClientBuilder.create().useSystemProperties().build();
			}
		}
		return httpClient;
	}

	public static String urlencode(final String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	public static void setTruststore(final File file, final String password)
		throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
		if (!file.isFile()) {
			throw new RuntimeException("Truststore file not found: " + file.getAbsolutePath());
		}
		final SSLContext theContext = SSLContexts.custom()
			.setProtocol("TLS")
			.loadTrustMaterial(file, password == null ? null : password.toCharArray(), TrustSelfSignedStrategy.INSTANCE)
			.build();
		sslsf = new SSLConnectionSocketFactory(theContext);
	}

	public static void setSkipCertificateValidation() {
		if (!tlsWarningEmitted.getAndSet(true)) {
			// Since this is a static util, it may happen that TLS is setup many times in
			// one command
			// invocation (e.g. when a command requires logging in). However, we would like
			// to
			// prevent this warning from appearing multiple times. That's why we need to
			// guard it with a boolean.
			log.error("The server is configured to use TLS but there is no truststore specified.");
			log.error("The tool will skip certificate validation. This is highly discouraged for production use cases");
		}

		final SSLContextBuilder builder = new SSLContextBuilder();
		try {
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			sslsf = new SSLConnectionSocketFactory(builder.build());
		} catch (final Exception e) {
			throw new RuntimeException("Failed setting up TLS", e);
		}
	}

	public static String extractIdFromLocation(final String location) {
		final int last = location.lastIndexOf("/");
		if (last != -1) {
			return location.substring(last + 1);
		}
		return null;
	}

	public static String addQueryParamsToUri(final String uri, final String... queryParams) {
		if (queryParams == null) {
			return uri;
		}

		if (queryParams.length % 2 != 0) {
			throw new RuntimeException("Value missing for query parameter: " + queryParams[queryParams.length - 1]);
		}

		final Map<String, String> params = new LinkedHashMap<>();
		for (int i = 0; i < queryParams.length; i += 2) {
			params.put(queryParams[i], queryParams[i + 1]);
		}
		return addQueryParamsToUri(uri, params);
	}

	public static String addQueryParamsToUri(final String uri, final Map<String, String> queryParams) {
		if (queryParams.size() == 0) {
			return uri;
		}

		final StringBuilder query = new StringBuilder();
		for (final Map.Entry<String, String> params : queryParams.entrySet()) {
			try {
				if (query.length() > 0) {
					query.append("&");
				}
				query.append(params.getKey()).append("=").append(URLEncoder.encode(params.getValue(), StandardCharsets.UTF_8));
			} catch (final Exception e) {
				throw new RuntimeException("Failed to encode query params: " + params.getKey() + "=" + params.getValue());
			}
		}

		return uri + (uri.indexOf("?") == -1 ? "?" : "&") + query;
	}

	public static String composeResourceUrl(final String adminRoot, final String realm, String uri) {
		if (!uri.startsWith("http:") && !uri.startsWith("https:")) {
			if ("realms".equals(uri) || uri.startsWith("realms/")) {
				uri = normalize(adminRoot) + uri;
			} else if ("serverinfo".equals(uri)) {
				uri = normalize(adminRoot) + uri;
			} else {
				uri = normalize(adminRoot) + "realms/" + realm + "/" + uri;
			}
		}
		return uri;
	}

	public static String normalize(final String value) {
		return value.endsWith("/") ? value : value + "/";
	}

	public static void checkSuccess(final String url, final HeadersBodyStatus response) {
		try {
			response.checkSuccess();
		} catch (final HttpResponseException e) {
			if (e.getStatusCode() == 404) {
				throw new RuntimeException("Resource not found for url: " + url, e);
			}
			throw e;
		}
	}

	public static <T> T doGetJSON(final Class<T> type, final String resourceUrl, final String auth) {
		final Headers headers = new Headers();
		if (auth != null) {
			headers.add("Authorization", auth);
		}
		headers.add("Accept", "application/json");

		final HeadersBodyStatus response;
		try {
			response = HttpUtil.doRequest("get", resourceUrl, new HeadersBody(headers));
		} catch (final IOException e) {
			throw new RuntimeException("HTTP request failed: GET " + resourceUrl, e);
		}

		checkSuccess(resourceUrl, response);

		final T result;
		try {
			result = JsonSerialization.readValue(response.getBody(), type);
		} catch (final IOException e) {
			throw new RuntimeException("Failed to read JSON response", e);
		}

		return result;
	}

	public static void doPostJSON(final String resourceUrl, final String auth, final Object content) {
		final Headers headers = new Headers();
		if (auth != null) {
			headers.add("Authorization", auth);
		}
		headers.add("Content-Type", "application/json");

		final HeadersBodyStatus response;

		final byte[] body;
		try {
			body = JsonSerialization.writeValueAsBytes(content);
		} catch (final IOException e) {
			throw new RuntimeException("Failed to serialize JSON", e);
		}

		try {
			response = HttpUtil.doRequest("post", resourceUrl, new HeadersBody(headers, new ByteArrayInputStream(body)));
		} catch (final IOException e) {
			throw new RuntimeException("HTTP request failed: POST " + resourceUrl + "\n" + new String(body), e);
		}

		checkSuccess(resourceUrl, response);
	}

	public static void doDeleteJSON(final String resourceUrl, final String auth, final Object content) {
		final Headers headers = new Headers();
		if (auth != null) {
			headers.add("Authorization", auth);
		}
		headers.add("Content-Type", "application/json");

		final HeadersBodyStatus response;

		final byte[] body;
		try {
			body = JsonSerialization.writeValueAsBytes(content);
		} catch (final IOException e) {
			throw new RuntimeException("Failed to serialize JSON", e);
		}

		try {
			response = HttpUtil.doRequest("delete", resourceUrl, new HeadersBody(headers, new ByteArrayInputStream(body)));
		} catch (final IOException e) {
			throw new RuntimeException("HTTP request failed: DELETE " + resourceUrl + "\n" + new String(body), e);
		}

		checkSuccess(resourceUrl, response);
	}

	public static String singularize(final String value) {
		return value.substring(0, value.length() - 1);
	}
}
