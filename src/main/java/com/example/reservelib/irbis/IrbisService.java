package com.example.reservelib.irbis;

import com.example.reservelib.irbis.dto.IrbisLibrariesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IrbisService {
    private static final Logger log = LoggerFactory.getLogger(IrbisService.class);

    private static final Pattern BLOCK_PATTERN = Pattern.compile(
            "<p[^>]*>\\s*<strong>\\s*\\u041C\\u0435\\u0441\\u0442\\u043E\\u043D\\u0430\\u0445\\u043E\\u0436\\u0434\\u0435\\u043D\\u0438\\u0435\\s+\\u0434\\u043E\\u043A\\u0443\\u043C\\u0435\\u043D\\u0442\\u0430[^<]*</strong>\\s*(.*?)</p>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern ANCHOR_TEXT_PATTERN = Pattern.compile("<a[^>]*>([^<]+)</a>", Pattern.CASE_INSENSITIVE);
    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset\\s*=\\s*['\"]?([^;'\"]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern META_CHARSET_PATTERN = Pattern.compile(
            "<meta[^>]+charset\\s*=\\s*['\"]?([^\\s'\">/;]+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final List<Pattern> LOCATION_PATTERNS = List.of(
            Pattern.compile(
                    "(?:\\u0411\\u0438\\u0431\\u043B\\u0438\\u043E\\u0442\\u0435\\u043A\\u0430|\\u0424\\u0438\\u043B\\u0438\\u0430\\u043B|\\u041E\\u0442\\u0434\\u0435\\u043B|\\u041C\\u0435\\u0441\\u0442\\u043E\\s+\\u0445\\u0440\\u0430\\u043D\\u0435\\u043D\\u0438\\u044F)\\s*[:\\-]\\s*([^\\n;,.]+)",
                    Pattern.CASE_INSENSITIVE
            ),
            Pattern.compile(
                    "(?:\\u0421\\u0438\\u0433\\u043B\\u0430\\s+\\u0445\\u0440\\u0430\\u043D\\u0435\\u043D\\u0438\\u044F|\\u0425\\u0440\\u0430\\u043D\\u0435\\u043D\\u0438\\u0435)\\s*[:\\-]\\s*([^\\n;,.]+)",
                    Pattern.CASE_INSENSITIVE
            ),
            Pattern.compile(
                    "(?:\\u041C\\u0435\\u0441\\u0442\\u043E\\u043D\\u0430\\u0445\\u043E\\u0436\\u0434\\u0435\\u043D\\u0438\\u0435\\s+\\u0434\\u043E\\u043A\\u0443\\u043C\\u0435\\u043D\\u0442\\u0430[^:]*?)\\s*:\\s*([^\\n]+)",
                    Pattern.CASE_INSENSITIVE
            )
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
    private final JdbcTemplate jdbcTemplate;

    public IrbisService(IrbisProperties properties, JdbcTemplate jdbcTemplate) {
        this.properties = properties;
        this.jdbcTemplate = jdbcTemplate;
    }

    public IrbisLibrariesResponse findLibrariesByTitle(String title) {
        return findLibrariesByTitle(title, false);
    }

    public IrbisLibrariesResponse findLibrariesByTitle(String title, boolean forceRefresh) {
        String normalizedTitle = normalizeTitle(title);
        if (!forceRefresh) {
            try {
                IrbisLibrariesResponse cached = readFromMainTables(normalizedTitle);
                if (cached != null) {
                    return cached;
                }
            } catch (DataAccessException e) {
                log.warn("DB is unavailable while reading cached libraries: {}", e.getMessage());
            }
        }

        IrbisLibrariesResponse response = fetchLibraries(normalizedTitle);
        try {
            saveToMainTables(response);
        } catch (DataAccessException e) {
            log.warn("DB is unavailable while saving parsed libraries: {}", e.getMessage());
        }
        return response;
    }

    public IrbisLibrariesResponse refreshLibrariesByTitle(String title) {
        return findLibrariesByTitle(title, true);
    }

    public IrbisLibrariesResponse fetchLibrariesByTitle(String title) {
        return fetchLibraries(normalizeTitle(title));
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
        return title.trim();
    }

    private IrbisLibrariesResponse fetchLibraries(String title) {
        try {
            Set<String> libraries = findBookLibraries(title);
            List<String> sorted = libraries.stream().sorted(Comparator.naturalOrder()).toList();
            return new IrbisLibrariesResponse(title, sorted.size(), sorted);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error while reading webirbis response: " + e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void refreshCacheDaily() {
        List<String> titles = jdbcTemplate.query(
                "SELECT title FROM books WHERE last_libraries_sync_at IS NOT NULL",
                (rs, rowNum) -> rs.getString("title")
        );

        if (titles.isEmpty()) {
            return;
        }

        for (String title : titles) {
            try {
                findLibrariesByTitle(title, true);
            } catch (Exception e) {
                log.warn("Failed to refresh IRBIS cache for title '{}': {}", title, e.getMessage());
            }
        }
    }

    private Set<String> findBookLibraries(String title) throws IOException {
        IrbisProperties.Provider provider = properties.getProvider();
        IrbisProperties.Search search = properties.getSearch();

        List<String> encodings = encodingCandidates(provider.getEncoding());
        Set<String> best = Set.of();
        IOException lastError = null;

        for (String encoding : encodings) {
            try {
                String searchUrl = buildSearchUrl(
                        provider.getBaseUrl(),
                        provider.getDatabase(),
                        encoding,
                        search.getLimit(),
                        title
                );
                String searchPage = fetchHtml(searchUrl, provider.getTimeoutSeconds(), provider.isInsecure());
                Set<String> libraries = extractLibraries(searchPage);
                if (!libraries.isEmpty()) {
                    return libraries;
                }
                best = libraries;
            } catch (IOException e) {
                lastError = e;
            }
        }

        if (lastError != null) {
            throw lastError;
        }
        return best;
    }

    private String buildSearchUrl(String baseUrl, String database, String encoding, int limit, String query) {
        Charset charset = resolveCharset(encoding, StandardCharsets.UTF_8);
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
                throw new IOException("Failed to initialize insecure SSL context", e);
            }
        }

        try (var inputStream = connection.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            Charset contentTypeCharset = extractCharsetFromContentType(connection.getContentType());
            Charset metaCharset = extractCharsetFromMeta(bytes);
            Charset preferredCharset = resolveCharset(properties.getProvider().getEncoding(), StandardCharsets.UTF_8);
            return decodeBestEffort(bytes, contentTypeCharset, metaCharset, preferredCharset);
        } finally {
            connection.disconnect();
        }
    }

    private Set<String> extractLibraries(String html) {
        Set<String> result = new LinkedHashSet<>();
        String normalizedHtml = normalizeMojibake(html);

        Matcher blockMatcher = BLOCK_PATTERN.matcher(normalizedHtml);
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

        String plainText = TAG_PATTERN.matcher(normalizedHtml).replaceAll("\n");
        plainText = HtmlUtils.htmlUnescape(plainText);
        plainText = normalizeMojibake(plainText);
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
        String[] parts = value.split("\\s*[,;]\\s*");
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

    private Charset extractCharsetFromContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        Matcher matcher = CHARSET_PATTERN.matcher(contentType);
        if (!matcher.find()) {
            return null;
        }
        return resolveCharset(matcher.group(1), null);
    }

    private Charset extractCharsetFromMeta(byte[] bytes) {
        String latin = new String(bytes, StandardCharsets.ISO_8859_1);
        Matcher matcher = META_CHARSET_PATTERN.matcher(latin);
        if (!matcher.find()) {
            return null;
        }
        return resolveCharset(matcher.group(1), null);
    }

    private String decodeBestEffort(byte[] bytes, Charset contentTypeCharset, Charset metaCharset, Charset preferredCharset) {
        List<Charset> candidates = new ArrayList<>();
        addIfPresent(candidates, contentTypeCharset);
        addIfPresent(candidates, metaCharset);
        addIfPresent(candidates, preferredCharset);
        addIfPresent(candidates, resolveCharset("windows-1251", null));
        addIfPresent(candidates, resolveCharset("cp1251", null));
        addIfPresent(candidates, StandardCharsets.UTF_8);
        addIfPresent(candidates, resolveCharset("koi8-r", null));
        addIfPresent(candidates, StandardCharsets.ISO_8859_1);

        String bestText = new String(bytes, StandardCharsets.UTF_8);
        int bestScore = Integer.MIN_VALUE;

        for (Charset charset : candidates) {
            String decoded = new String(bytes, charset);
            int score = textScore(decoded);
            if (score > bestScore) {
                bestScore = score;
                bestText = decoded;
            }
        }
        return normalizeMojibake(bestText);
    }

    private int textScore(String value) {
        String lower = value.toLowerCase();
        int score = 0;
        score += keywordScore(lower, "местонахождение", 20);
        score += keywordScore(lower, "документа", 10);
        score += keywordScore(lower, "библиотека", 8);
        score += keywordScore(lower, "филиал", 8);
        score += keywordScore(lower, "сигла хранения", 6);
        score += keywordScore(lower, "хранение", 6);
        score += keywordScore(lower, "ирбис", 4);
        score += keywordScore(lower, "рџ", -8);
        score += keywordScore(lower, "ã", -8);
        score += keywordScore(lower, "â", -8);

        long replacementCount = value.chars().filter(ch -> ch == '\uFFFD').count();
        score -= (int) Math.min(1000, replacementCount * 2);
        return score;
    }

    private int keywordScore(String value, String keyword, int weight) {
        int count = 0;
        int from = 0;
        while (true) {
            int idx = value.indexOf(keyword, from);
            if (idx < 0) {
                break;
            }
            count++;
            from = idx + keyword.length();
        }
        return count * weight;
    }

    private String normalizeMojibake(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (!looksBroken(value)) {
            return value;
        }

        String utfCandidate = recode(value, StandardCharsets.ISO_8859_1, StandardCharsets.UTF_8);
        String cpCandidate = recode(value, StandardCharsets.ISO_8859_1, resolveCharset("windows-1251", StandardCharsets.UTF_8));

        int originalScore = textScore(value);
        int utfScore = textScore(utfCandidate);
        int cpScore = textScore(cpCandidate);

        if (utfScore >= cpScore && utfScore > originalScore) {
            return utfCandidate;
        }
        if (cpScore > originalScore) {
            return cpCandidate;
        }
        return value;
    }

    private boolean looksBroken(String value) {
        return value.contains("Ã")
                || value.contains("Â")
                || value.contains("Ð")
                || value.contains("Ñ")
                || value.contains("Рџ")
                || value.contains("\uFFFD");
    }

    private String recode(String value, Charset src, Charset dst) {
        byte[] bytes = value.getBytes(src);
        return new String(bytes, dst);
    }

    private List<String> encodingCandidates(String preferred) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        if (preferred != null && !preferred.isBlank()) {
            values.add(preferred.trim());
        }
        values.add("windows-1251");
        values.add("cp1251");
        values.add("utf-8");
        values.add("koi8-r");
        return List.copyOf(values);
    }

    private Charset resolveCharset(String charset, Charset fallback) {
        if (charset == null || charset.isBlank()) {
            return fallback;
        }
        try {
            return Charset.forName(charset.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private void addIfPresent(List<Charset> list, Charset charset) {
        if (charset == null || list.contains(charset)) {
            return;
        }
        list.add(charset);
    }

    private IrbisLibrariesResponse readFromMainTables(String title) {
        Integer bookId = jdbcTemplate.query("""
                        SELECT id
                        FROM books
                        WHERE lower(title) = lower(?)
                          AND last_libraries_sync_at IS NOT NULL
                          AND last_libraries_sync_at >= (CURRENT_TIMESTAMP - INTERVAL '5 days')
                        ORDER BY id
                        LIMIT 1
                    """,
                rs -> rs.next() ? rs.getInt("id") : null,
                title
        );

        if (bookId == null) {
            return null;
        }

        List<String> libraries = jdbcTemplate.query("""
                        SELECT l.name
                        FROM libraries l
                        JOIN book_libraries bl ON bl.library_id = l.id
                        WHERE bl.book_id = ?
                        ORDER BY l.name
                    """,
                (rs, rowNum) -> rs.getString("name"),
                bookId
        );

        return new IrbisLibrariesResponse(title, libraries.size(), libraries);
    }

    @Transactional
    private void saveToMainTables(IrbisLibrariesResponse response) {
        String titleForDb = limit(response.title(), 500);
        Integer bookId = jdbcTemplate.query("""
                        SELECT id
                        FROM books
                        WHERE lower(title) = lower(?)
                        ORDER BY id
                        LIMIT 1
                    """,
                rs -> rs.next() ? rs.getInt("id") : null,
                titleForDb
        );

        if (bookId == null) {
            bookId = jdbcTemplate.queryForObject("""
                            INSERT INTO books (title, last_libraries_sync_at)
                            VALUES (?, CURRENT_TIMESTAMP)
                            RETURNING id
                        """,
                    Integer.class,
                    titleForDb
            );
        }

        jdbcTemplate.update("DELETE FROM book_libraries WHERE book_id = ?", bookId);

        for (String libraryName : response.libraries()) {
            String normalizedLibraryName = limit(libraryName == null ? "" : libraryName.trim(), 500);
            if (normalizedLibraryName.isBlank()) {
                continue;
            }
            jdbcTemplate.update("""
                            INSERT INTO libraries (name)
                            VALUES (?)
                            ON CONFLICT (name) DO NOTHING
                        """,
                    normalizedLibraryName
            );

            Integer libraryId = jdbcTemplate.queryForObject(
                    "SELECT id FROM libraries WHERE name = ?",
                    Integer.class,
                    normalizedLibraryName
            );

            jdbcTemplate.update("""
                            INSERT INTO book_libraries (book_id, library_id)
                            VALUES (?, ?)
                            ON CONFLICT (book_id, library_id) DO NOTHING
                        """,
                    bookId,
                    libraryId
            );
        }

        jdbcTemplate.update("""
                        UPDATE books
                        SET last_libraries_sync_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                    """,
                bookId
        );
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
    }
}
