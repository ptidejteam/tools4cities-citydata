import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpGetAsyncWithJWT {
    
    public static void main(String[] args) throws Exception {
    	
        String token = getJwtToken();
        
        // This runnerId is just an example; the user should use an actually existing runnerId from a Runner they created.
        
        String runnerId = "d593c930-7fed-4c7b-ac52-fff946b78c32";
        String requestUrl = "http://localhost:8082/apply/async/" + runnerId;
        
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        
        int responseCode = connection.getResponseCode();
        System.out.println("GET Response Code: " + responseCode);
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        }
    }
    
    private static String getJwtToken() throws Exception {
        URL url = new URL("http://localhost:8082/authenticate");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        
        String json = "{\"username\":\"citydata\",\"password\":\"citydata\"}";
        connection.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return reader.readLine(); // JWT token returned directly as string
    }
}
