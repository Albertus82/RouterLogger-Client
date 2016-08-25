package it.albertus.router.client.http;

import it.albertus.router.client.dto.RouterDataDto;
import it.albertus.router.client.dto.StatusDto;
import it.albertus.router.client.dto.ThresholdsDto;
import it.albertus.router.client.dto.transformer.DataTransformer;
import it.albertus.router.client.dto.transformer.StatusTransformer;
import it.albertus.router.client.dto.transformer.ThresholdsTransformer;
import it.albertus.router.client.engine.RouterData;
import it.albertus.router.client.engine.RouterLoggerClientConfiguration;
import it.albertus.router.client.engine.RouterLoggerStatus;
import it.albertus.router.client.engine.ThresholdsReached;
import it.albertus.router.client.gui.RouterLoggerGui;
import it.albertus.router.client.resources.Resources;
import it.albertus.router.client.util.Logger;
import it.albertus.util.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;

public class HttpPollingThread extends Thread {

	private static final String CFG_KEY_HTTP_PASSWORD = "http.password";
	private static final String CFG_KEY_HTTP_USERNAME = "http.username";
	private static final String CFG_KEY_HTTP_PORT = "http.port";
	private static final String CFG_KEY_HTTP_HOST = "http.host";
	private static final String CFG_KEY_CLIENT_PROTOCOL = "client.protocol";
	private static final String CFG_KEY_HTTP_IGNORE_CERTIFICATE = "http.ignore.certificate";

	public interface Defaults {
		int REFRESH_SECS = 0;
		boolean AUTHENTICATION = true;
		int PORT = 8080;
		boolean IGNORE_CERTIFICATE = false;
	}

	private final Configuration configuration = RouterLoggerClientConfiguration.getInstance();
	private final RouterLoggerGui gui;

	private int iteration = 0;
	private String eTag;

	private volatile boolean exit = false;

	public HttpPollingThread(final RouterLoggerGui gui) {
		this.setDaemon(true);
		this.gui = gui;
		if (configuration.getBoolean(CFG_KEY_HTTP_IGNORE_CERTIFICATE, Defaults.IGNORE_CERTIFICATE)) {
			try {
				final SSLContext sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null, new TrustManager[] { new DummyTrustManager() }, new SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void interrupt() {
		exit = true;
		super.interrupt();
	}

	@Override
	public void run() {
		String scheme = configuration.getString(CFG_KEY_CLIENT_PROTOCOL).trim().toLowerCase();
		if (!scheme.contains("http")) {
			return;
		}
		String host = configuration.getString(CFG_KEY_HTTP_HOST);
		int port = configuration.getInt(CFG_KEY_HTTP_PORT, Defaults.PORT);
		String username = configuration.getString(CFG_KEY_HTTP_USERNAME);
		char[] password = configuration.getCharArray(CFG_KEY_HTTP_PASSWORD);

		String baseUrl = scheme + "://" + host + ":" + port;

		Logger.getInstance().log(Resources.get("msg.http.polling", baseUrl));

		while (true) {
			scheme = configuration.getString(CFG_KEY_CLIENT_PROTOCOL).trim().toLowerCase();
			if (!scheme.contains("http")) {
				break;
			}
			host = configuration.getString(CFG_KEY_HTTP_HOST);
			port = configuration.getInt(CFG_KEY_HTTP_PORT, Defaults.PORT);
			username = configuration.getString(CFG_KEY_HTTP_USERNAME);
			password = configuration.getCharArray(CFG_KEY_HTTP_PASSWORD);

			baseUrl = scheme + "://" + host + ":" + port;

			if (configuration.getBoolean(CFG_KEY_HTTP_IGNORE_CERTIFICATE, Defaults.IGNORE_CERTIFICATE)) {
				HttpsURLConnection.setDefaultHostnameVerifier(new RouterLoggerHostnameVerifier(host));
			}

			final String authenticationHeader;
			if (configuration.getBoolean("http.authentication", Defaults.AUTHENTICATION) && username != null && !username.isEmpty() && password != null && password.length > 0) {
				authenticationHeader = "Basic " + DatatypeConverter.printBase64Binary((username + ":" + String.valueOf(password)).getBytes());
			}
			else {
				authenticationHeader = null;
			}

			// RouterLoggerStatus
			Reader httpReader = null;
			int refresh = configuration.getInt("http.refresh.secs", Defaults.REFRESH_SECS);
			try {
				URL url = new URL(baseUrl + "/json/status");
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.addRequestProperty("Accept", "application/json");
				if (authenticationHeader != null) {
					urlConnection.addRequestProperty("Authorization", authenticationHeader);
				}
				InputStream is = urlConnection.getInputStream();
				httpReader = new InputStreamReader(is);
				final StatusDto statusDto = new Gson().fromJson(httpReader, StatusDto.class);
				httpReader.close();
				final RouterLoggerStatus rls = StatusTransformer.fromDto(statusDto);
				gui.setStatus(rls);

				// RouterData
				url = new URL(baseUrl + "/json/data");
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.addRequestProperty("Accept", "application/json");
				if (authenticationHeader != null) {
					urlConnection.addRequestProperty("Authorization", authenticationHeader);
				}

				if (eTag != null) {
					urlConnection.addRequestProperty("If-None-Match", eTag);
				}

				// Logging
				if (Logger.getInstance().isDebugEnabled()) {
					Logger.getInstance().log(Resources.get("msg.http.response.code", urlConnection.getResponseCode()));
				}

				for (final String header : urlConnection.getHeaderFields().keySet()) {
					if (header != null) {
						if (header.equalsIgnoreCase("Etag")) {
							eTag = urlConnection.getHeaderField(header);
						}
						else if (refresh <= 0 && header.equalsIgnoreCase("Refresh")) {
							refresh = Integer.parseInt(urlConnection.getHeaderField(header));
						}
					}
				}
				if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK || urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_AUTHORITATIVE) {
					is = urlConnection.getInputStream();
					httpReader = new InputStreamReader(is);
					final RouterDataDto routerDataDto = new Gson().fromJson(httpReader, RouterDataDto.class);
					httpReader.close();
					final RouterData data = DataTransformer.fromDto(routerDataDto);

					// ThresholdsReached
					url = new URL(baseUrl + "/json/thresholds");
					urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.addRequestProperty("Accept", "application/json");
					if (authenticationHeader != null) {
						urlConnection.addRequestProperty("Authorization", authenticationHeader);
					}
					is = urlConnection.getInputStream();
					httpReader = new InputStreamReader(is);
					final ThresholdsDto thresholdsDto = new Gson().fromJson(httpReader, ThresholdsDto.class);
					httpReader.close();
					final ThresholdsReached thresholdsReached = ThresholdsTransformer.fromDto(thresholdsDto);

					// Update GUI
					gui.getDataTable().addRow(++iteration, data, thresholdsReached.getReached());
					gui.getTrayIcon().updateTrayItem(rls.getStatus(), data);
				}
			}
			catch (final IOException ioe) {
				ioe.printStackTrace();
			}
			if (exit) {
				break;
			}
			else {
				if (refresh <= 0) {
					Logger.getInstance().log(Resources.get("err.http.refresh.auto"));
					break;
				}
				try {
					Thread.sleep(refresh * 1000);
				}
				catch (final InterruptedException e) {
					break;
				}
			}
		}
	}
}