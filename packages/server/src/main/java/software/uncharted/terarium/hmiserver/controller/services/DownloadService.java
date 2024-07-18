package software.uncharted.terarium.hmiserver.controller.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

	/**
	 * Normalizes a relative url fragment and a base url to a fully qualified Url
	 *
	 * @param relativeUrl the fragment
	 * @param baseUrl the base url
	 * @return a fully qualified url
	 * @throws URISyntaxException if the url is not a valid URI
	 */
	private static String normalizeRelativeUrl(final String relativeUrl, final String baseUrl) throws URISyntaxException {
		final URI uri = new URI(baseUrl);
		return uri.getScheme() + "://" + uri.getHost() + relativeUrl;
	}

	/**
	 * Checks if the given byte array is a PDF file
	 *
	 * @param data the byte array
	 * @throws IOException if the data is not a valid PDF
	 */
	public static Boolean IsPdf(final byte[] data) {
		// check if data is null or less than 5 bytes
		if (data == null || data.length < 8) {
			return false;
		}

		// check if data starts with %PDF
		if (data[0] != 0x25 || data[1] != 0x50 || data[2] != 0x44 || data[3] != 0x46) {
			return false;
		}

		// for pdf version 1.3
		// check if data ends with %%EOF
		if (data[4] == 0x31 && data[5] == 0x2E && data[6] == 0x33) {
			return (
				data[data.length - 7] == 0x25 &&
				data[data.length - 6] == 0x25 &&
				data[data.length - 5] == 0x45 &&
				data[data.length - 4] == 0x4F &&
				data[data.length - 3] == 0x46 &&
				data[data.length - 2] == 0x20 &&
				data[data.length - 1] == 0x0A
			);
		}
		// for pdf version 1.4
		// check if data ends with %%EOF
		else if (data[4] == 0x31 && data[5] == 0x2E && data[6] == 0x34) {
			return (
				data[data.length - 6] == 0x25 &&
				data[data.length - 5] == 0x25 &&
				data[data.length - 4] == 0x45 &&
				data[data.length - 3] == 0x4F &&
				data[data.length - 2] == 0x46 &&
				data[data.length - 1] == 0x0A
			);
		}

		return true;
	}

	/**
	 * Gets a PDF file from a given url
	 *
	 * @param url the url location (that may contain redirects)
	 * @return the pdf file
	 * @throws IOException if the url is not reachable
	 * @throws URISyntaxException if the url is not a valid URI
	 */
	public static byte[] getPDF(String url) throws IOException, URISyntaxException {
		final CloseableHttpClient httpclient = HttpClients.custom().disableRedirectHandling().build();

		url = url.replace(" ", "%20");

		final HttpGet get = new HttpGet(url);
		final HttpResponse response = httpclient.execute(get);

		// Follow redirects until we actually get a document
		if (response.getStatusLine().getStatusCode() >= 300 && response.getStatusLine().getStatusCode() <= 310) {
			final String redirect = response.getFirstHeader("Location").getValue();
			if (!redirect.startsWith("http")) {
				return getPDF(normalizeRelativeUrl(redirect, url));
			} else {
				return getPDF(redirect);
			}
		} else {
			// We actually have a document, if it's an HTML page with the content, look for
			// a link to the pdf itself and follow
			// it
			final String contentType = response.getEntity().getContentType().getValue();
			if (contentType.contains("html")) {
				final String html = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
				final Document document = Jsoup.parse(html);
				final Elements links = document.select("a");
				final String pdfUrl = links
					.stream()
					.map(element -> element.attributes().get("href"))
					.map(String::toLowerCase)
					.filter(extractedUrl -> extractedUrl.endsWith(".pdf"))
					.findFirst()
					.orElse(null);

				if (pdfUrl == null) {
					return null;
				}

				if (!pdfUrl.startsWith("http")) {
					final URI uri = new URI(url);
					return getPDF(uri.getScheme() + "://" + uri.getHost() + pdfUrl);
				} else {
					return getPDF(pdfUrl);
				}
			}
		}
		return IOUtils.toByteArray(response.getEntity().getContent());
	}

	/**
	 * Gets a PDF URL from a given url
	 *
	 * @param url the url location (that may contain redirects)
	 * @return the pdf url
	 * @throws IOException if the url is not reachable
	 * @throws URISyntaxException if the url is not a valid URI
	 */
	public static String getPDFURL(String url) throws IOException, URISyntaxException {
		final CloseableHttpClient httpclient = HttpClients.custom().disableRedirectHandling().build();

		url = url.replace(" ", "%20");

		final HttpGet get = new HttpGet(url);
		final HttpResponse response = httpclient.execute(get);

		// Follow redirects until we actually get a document
		if (response.getStatusLine().getStatusCode() >= 300 && response.getStatusLine().getStatusCode() <= 310) {
			final String redirect = response.getFirstHeader("Location").getValue();
			if (!redirect.startsWith("http")) {
				return getPDFURL(normalizeRelativeUrl(redirect, url));
			} else {
				return getPDFURL(redirect);
			}
		} else {
			// We actually have a document, if it's an HTML page with the content, look for
			// a link to the pdf itself and follow
			// it
			final String contentType = response.getEntity().getContentType().getValue();
			if (contentType.contains("html")) {
				final String html = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
				final Document document = Jsoup.parse(html);
				final Elements links = document.select("a");
				final String pdfUrl = links
					.stream()
					.map(element -> element.attributes().get("href"))
					.map(String::toLowerCase)
					.filter(extractedUrl -> extractedUrl.endsWith(".pdf"))
					.findFirst()
					.orElse(null);

				if (pdfUrl == null) {
					return null;
				}

				if (!pdfUrl.startsWith("http")) {
					final URI uri = new URI(url);
					return getPDFURL(uri.getScheme() + "://" + uri.getHost() + pdfUrl);
				} else {
					return getPDFURL(pdfUrl);
				}
			}
		}
		return url;
	}

	public static String pdfNameFromUrl(final String url) {
		if (url == null) return null;

		final String[] parts = url.split("\\?"); // Remove query parameters
		final String urlWithoutParams = parts[0];
		final Pattern pattern = Pattern.compile("/([^/]+\\.pdf)$", Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(urlWithoutParams);

		if (matcher.find() && matcher.group(1) != null) {
			return matcher.group(1);
		}

		return "missingname.pdf";
	}
}
