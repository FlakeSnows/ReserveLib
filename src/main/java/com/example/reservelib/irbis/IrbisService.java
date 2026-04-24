package com.example.reservelib.irbis;

import com.example.reservelib.irbis.dto.IrbisLibrariesResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IrbisService {

    private static final Pattern BLOCK_PATTERN = Pattern.compile(
            "<p[^>]*>\\s*<strong>\\s*Местонахождение\\s+документа[^<]*</strong>\\s*(.*?)</p>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern ANCHOR_TEXT_PATTERN = Pattern.compile("<a[^>]*>([^<]+)</a>", Pattern.CASE_INSENSITIVE);
    private static final Pattern HREF_PATTERN = Pattern.compile(
            "<a\\b[^>]*\\bhref\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset\\s*=\\s*['\"]?([^;'\"]+)", Pattern.CASE_INSENSITIVE);
    private static final List<Pattern> LOCATION_PATTERNS = List.of(
            Pattern.compile("(?:Библиотека|Филиал|Отдел|Место\\s+хранения)\\s*[:\\-]\\s*([^\\n;,.]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:Сигла\\s+хранения|Хранение)\\s*[:\\-]\\s*([^\\n;,.]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:Местонахождение\\s+документа[^:]*?)\\s*:\\s*([^\\n]+)", Pattern.CASE_INSENSITIVE)
    );

    private static final TrustManager[] TRUST_ALL_MANAGERS = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
    };

    private static final HostnameVerifier TRUST_ALL_HOSTNAMES = (hostname, session) -> true;

    private final IrbisProperties properties;

    public IrbisService(IrbisProperties properties) {
        this.properties = properties;
    }

    public IrbisLibrariesResponse findLibrariesByTitle(String title, Boolean deepScanOverride) {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
        try {
            Set<String> libraries = findBookLibraries(title.trim(), deepScanOverride);
            List<String> sorted = libraries.stream().sorted(Comparator.naturalOrder()).toList();
            return new IrbisLibrariesResponse(title.trim(), sorted.size(), sorted);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ошибка доступа к webirbis: " + e.getMessage(), e);
        }
    }

    private Set<String> findBookLibraries(String title, Boolean deepScanOverride) throws IOException {
        IrbisProperties.Provider provider = properties.getProvider();
        IrbisProperties.Search search = properties.getSearch();

        String searchUrl = buildSearchUrl(
                provider.getBaseUrl(),
                provider.getDatabase(),
                provider.getEncoding(),
                search.getLimit(),
                title
        );

        String searchPage = fetchHtml(searchUrl, provider.getTimeoutSeconds(), provider.isInsecure());
        Set<String> libraries = extractLibraries(searchPage);

        boolean deepScan = deepScanOverride != null ? deepScanOverride : search.isDeepScan();
        if (!deepScan) {
            return libraries;
        }

        List<String> recordLinks = extractRecordLinks(provider.getBaseUrl(), searchPage);
        if (recordLinks.isEmpty()) {
            return libraries;
        }

        ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, search.getWorkers()));
        try {
            List<Future<Set<String>>> futures = new ArrayList<>();
            for (String link : recordLinks) {
                futures.add(executor.submit(() -> {
                    try {
                        String recordPage = fetchHtml(link, provider.getTimeoutSeconds(), provider.isInsecure());
                        return extractLibraries(recordPage);
                    } catch (IOException ignored) {
                        return Set.of();
                    }
                }));
            }

            for (Future<Set<String>> future : futures) {
                try {
                    libraries.addAll(future.get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Сканирование было прервано", e);
                } catch (ExecutionException e) {
                    throw new IOException("Ошибка сканирования записей", e.getCause());
                }
            }
            return libraries;
        } finally {
            executor.shutdown();
        }
    }

    private String buildSearchUrl(String baseUrl, String database, String encoding, int limit, String query) {
        Charset charset = resolveCharset(encoding);
        Map<String, String> params = new LinkedHashMap<>();
        params.put("C21COM", "S");
        params.put("I21DBN", database);
        params.put("P21DBN", database);
        params.put("S21FMT", "fullwebr");
        params.put("S21CNR", String.valueOf(limit));
        params.put("S21ALL", "(<.>T=" + query + "$<.>)");

        StringBuilder sb = new StringBuilder(baseUrl).append("?");
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                sb.append("&");
            }
            first = false;
            sb.append(URLEncoder.encode(entry.getKey(), charset));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), charset));
        }
        return sb.toString();
    }

    private String fetchHtml(String url, int timeoutSeconds, boolean insecure) throws IOException {
        URLConnection rawConnection = URI.create(url).toURL().openConnection();
        if (!(rawConnection instanceof HttpURLConnection connection)) {
            throw new IOException("Unsupported protocol for URL: " + url);
        }

        connection.setConnectTimeout(timeoutSeconds * 1_000);
        connection.setReadTimeout(timeoutSeconds * 1_000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; webirbis-parser/1.0)");
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml");

        if (insecure && connection instanceof HttpsURLConnection httpsConnection) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, TRUST_ALL_MANAGERS, new java.security.SecureRandom());
                httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                httpsConnection.setHostnameVerifier(TRUST_ALL_HOSTNAMES);
            } catch (GeneralSecurityException e) {
                throw new IOException("Не удалось отключить SSL-проверку", e);
            }
        }

        try (var inputStream = connection.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            Charset charset = resolveResponseCharset(connection.getContentType());
            return new String(bytes, charset);
        } finally {
            connection.disconnect();
        }
    }

    private List<String> extractRecordLinks(String baseUrl, String html) {
        Matcher matcher = HREF_PATTERN.matcher(html);
        Set<String> unique = new LinkedHashSet<>();

        while (matcher.find()) {
            String href = firstNonBlank(matcher.group(1), matcher.group(2), matcher.group(3));
            if (href == null) {
                continue;
            }
            String absolute = URI.create(baseUrl).resolve(HtmlUtils.htmlUnescape(href)).toString();
            if (!absolute.contains("C21COM=")) {
                continue;
            }
            String query;
            try {
                query = URI.create(absolute).getQuery();
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            if (query == null) {
                continue;
            }
            if (query.contains("C21COM=2") || query.contains("C21COM=F")) {
                unique.add(absolute);
            }
        }
        return List.copyOf(unique);
    }

    private Set<String> extractLibraries(String html) {
        Set<String> result = new LinkedHashSet<>();

        Matcher blockMatcher = BLOCK_PATTERN.matcher(html);
        while (blockMatcher.find()) {
            String block = blockMatcher.group(1);
            Matcher anchorMatcher = ANCHOR_TEXT_PATTERN.matcher(block);
            boolean hasAnchors = false;
            while (anchorMatcher.find()) {
                hasAnchors = true;
                addLocation(HtmlUtils.htmlUnescape(anchorMatcher.group(1)), result);
            }
            if (!hasAnchors) {
                String plain = TAG_PATTERN.matcher(block).replaceAll(" ");
                addLocation(HtmlUtils.htmlUnescape(plain), result);
            }
        }

        String plainText = TAG_PATTERN.matcher(html).replaceAll("\n");
        plainText = HtmlUtils.htmlUnescape(plainText);
        for (Pattern pattern : LOCATION_PATTERNS) {
            Matcher matcher = pattern.matcher(plainText);
            while (matcher.find()) {
                addLocation(matcher.group(1), result);
            }
        }

        return result;
    }

    private void addLocation(String rawValue, Set<String> storage) {
        String value = rawValue == null ? "" : rawValue.replaceAll("\\s+", " ").trim();
        if (value.length() <= 1) {
            return;
        }
        String[] parts = value.split("\\s*,\\s*");
        if (parts.length > 1) {
            for (String part : parts) {
                String item = part.trim();
                if (!item.isEmpty()) {
                    storage.add(item);
                }
            }
            return;
        }
        storage.add(value);
    }

    private Charset resolveResponseCharset(String contentType) {
        if (contentType != null) {
            Matcher matcher = CHARSET_PATTERN.matcher(contentType);
            if (matcher.find()) {
                return resolveCharset(matcher.group(1));
            }
        }
        return StandardCharsets.UTF_8;
    }

    private Charset resolveCharset(String charset) {
        try {
            return Charset.forName(charset);
        } catch (Exception ignored) {
            return StandardCharsets.UTF_8;
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}

