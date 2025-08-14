package org.xedox.webaide.devtools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;

public class WebManager {

    public void download(String url, OnDownloadListener downloadListener) throws IOException {
        HttpsURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL urlObj = new URL(url);
            connection = (HttpsURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
            connection.setInstanceFollowRedirects(true);

            String contentType = connection.getContentType();
            String charset = StandardCharsets.UTF_8.name();
            if (contentType != null && contentType.contains("charset=")) {
                charset = contentType.substring(contentType.indexOf("charset=") + 8);
            }

            int contentLength = connection.getContentLength();
            downloadListener.onDownloadStart(contentLength);

            StringBuilder content = new StringBuilder();
            int totalRead = 0;

            try (InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, charset);
                    BufferedReader br = new BufferedReader(isr)) {

                char[] buffer = new char[8192];
                int bytesRead;

                while ((bytesRead = br.read(buffer)) != -1) {
                    content.append(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                    downloadListener.onDownload(totalRead, contentLength);
                }
            }

            downloadListener.onDownloadFinished(content.toString());
        } catch (IOException e) {
            if (downloadListener != null) {
                downloadListener.onDownloadError(e);
            }
            throw e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public List<String> extractJsScripts(String html) {
        List<String> scripts = new ArrayList<>();
        Matcher matcher =
                Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
                        .matcher(html);

        while (matcher.find()) {
            String scriptContent = matcher.group(1).trim();
            if (!scriptContent.isEmpty()) {
                scripts.add(scriptContent);
            }
        }
        return scripts;
    }
    public List<String> extractJsScriptLinks(String html) {
        List<String> scripts = new ArrayList<>();
        Matcher matcher =
                Pattern.compile("<script src=\"(.*)\">[\\s]+</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
                        .matcher(html);

        while (matcher.find()) {
            String scriptContent = matcher.group(1).trim();
            if (!scriptContent.isEmpty()) {
                scripts.add(scriptContent);
            }
        }
        return scripts;
    }

    public interface OnDownloadListener {
        void onDownloadStart(int pageLength);

        void onDownload(int downloaded, int pageLength);

        void onDownloadFinished(String downloaded);

        default void onDownloadError(IOException e) {
            e.printStackTrace();
        }
    }
}
