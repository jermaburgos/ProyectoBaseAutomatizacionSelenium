package report;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class SupabaseExecutionClient {

    private final HttpClient httpClient;
    private final String functionUrl;
    private final String ingestToken;

    public SupabaseExecutionClient(String functionUrl, String ingestToken) {
        this.httpClient = HttpClient.newHttpClient();
        this.functionUrl = functionUrl;
        this.ingestToken = ingestToken;
    }

    public String enviar(ExecutionRunPayload payload) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(functionUrl))
                .header("x-ingest-token", ingestToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toJson(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }

        throw new IOException(
                "Supabase function returned HTTP "
                        + response.statusCode()
                        + ": "
                        + response.body()
        );
    }

    public static String fileToBase64(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
