package org.springframework.data.crate;

import com.google.common.base.Throwables;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.crate.testing.CrateTestCluster;
import io.crate.testing.CrateTestServer;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.BeforeClass;
import org.junit.ClassRule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CrateIntegrationTest {

    private static final String CRATE_SERVER_VERSION = clientVersion();
    private CloseableHttpClient httpClient = HttpClients.createDefault();
    protected static CrateTestServer server;


    private static String clientVersion() {
        String cp = System.getProperty("java.class.path");
        Matcher m = Pattern.compile("crate-client-([\\d\\.]{5,})\\.jar").matcher(cp);

        if (m.find()) {
            String version = m.group(1);
            return version;
        }
        throw new RuntimeException("unable to get version of crate-client");
    }

    @ClassRule
    public static CrateTestCluster testCluster = CrateTestCluster.fromVersion(CRATE_SERVER_VERSION)
            .keepWorkingDir(false)
            .build();

    @BeforeClass
    public static void beforeClass() {
        server = testCluster.randomServer();
    }

    private void waitForZeroCount(String stmt) {
        for (int i = 1; i < 10; i++) {
            try {
                JsonArray rows = queryHttpEndPoint(stmt).getAsJsonArray("rows");
                JsonArray rowsNestedArray = rows.get(0).getAsJsonArray();
                int numShards = rowsNestedArray.get(0).getAsInt();
                if (numShards == 0) {
                    return;
                }
                Thread.sleep(i * 100);
            } catch (IOException ignored) {
            } catch (InterruptedException e) {
                Throwables.propagate(e);
            }
        }
        throw new RuntimeException("waiting for zero result timed out");
    }

    protected void ensureGreen() {
        waitForZeroCount("select count(*) from sys.shards where state <> 'STARTED'");
    }

    private JsonObject queryHttpEndPoint(String stmt) throws IOException {
        HttpPost request = new HttpPost(String.format(Locale.ENGLISH, "http://%s:%d/_sql", server.crateHost(), server.httpPort()));
        StringEntity entity = new StringEntity(String.format("{\"stmt\": \"%s\"}", stmt));
        request.addHeader("Content-Type", "application/json");
        request.setEntity(entity);
        HttpResponse response = httpClient.execute(request);
        return parseResponse(response.getEntity().getContent());
    }

    private static JsonObject parseResponse(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder res = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            res.append(line);
        }
        br.close();
        return new JsonParser().parse(res.toString()).getAsJsonObject();
    }
}
