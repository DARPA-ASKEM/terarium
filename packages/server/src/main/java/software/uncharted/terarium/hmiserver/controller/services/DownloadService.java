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
	 * @param baseUrl     the base url
	 * @return a fully qualified url
	 * @throws URISyntaxException
	 */
	private String normalizeRelativeUrl(final String relativeUrl, final String baseUrl) throws URISyntaxException {
		final URI uri = new URI(baseUrl);
		return uri.getScheme() + "://" + uri.getHost() + relativeUrl;
	}

	/**
	 * Gets a PDF file from a given url
	 *
	 * @param url the url location (that may contain redirects)
	 * @return the pdf file
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public byte[] getPDF(final String url) throws IOException, URISyntaxException {
		CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build();

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
				final String pdfUrl = links.stream()
					.map(element -> element.attributes().get("href"))
					.map(String::toLowerCase)
					.filter(extractedUrl -> extractedUrl.endsWith(".pdf"))
					.findFirst().orElse(null);

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
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public String getPDFURL(final String url) throws IOException, URISyntaxException {
		CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build();

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
				final String pdfUrl = links.stream()
					.map(element -> element.attributes().get("href"))
					.map(String::toLowerCase)
					.filter(extractedUrl -> extractedUrl.endsWith(".pdf"))
					.findFirst().orElse(null);

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

	public String pdfNameFromUrl(String url) {
		if(url == null) return null;
		
		Pattern pattern = Pattern.compile("/([^/]+\\.pdf)$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(url);
	
		if (matcher.find() && matcher.group(1) != null) {
			return matcher.group(1);
		}
	
		return null;
	}
}
