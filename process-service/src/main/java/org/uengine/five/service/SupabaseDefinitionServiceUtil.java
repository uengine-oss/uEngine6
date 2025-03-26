package org.uengine.five.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.uengine.five.framework.ProcessTransactionContext;
import org.uengine.five.serializers.BpmnXMLParser;
import org.uengine.kernel.ProcessDefinition;

import java.io.IOException;

/**
 * Created by uengine on 2017. 11. 15..
 */
@Component
public class SupabaseDefinitionServiceUtil implements DefinitionServiceUtil {

    @Autowired
    DefinitionXMLService definitionService;

    static BpmnXMLParser bpmnXMLParser = new BpmnXMLParser();

    // Supabase API 설정
    private static final String SUPABASE_URL = "http://localhost:54321";

    private static final String SUPABASE_KEY = "";

    private final OkHttpClient httpClient = new OkHttpClient();

    public Object getDefinition(String defPath) throws Exception {
        return getDefinition(defPath, null);
    }

    public Object getDefinition(String defPath, String version) throws Exception {
        return getDefinition(defPath, version, SUPABASE_KEY);
    }

    public Object getDefinition(String defPath, String version, String authToken) throws Exception {

        if (!defPath.endsWith(".bpmn")) {
            defPath = defPath + ".bpmn";
        }

        ProcessTransactionContext tc = ProcessTransactionContext.getThreadLocalInstance();

        ProcessDefinition processDefinition = (ProcessDefinition) tc.getSharedContext("def:" + defPath + "@" + version);
        if (processDefinition == null) {
            // Supabase에서 bpmn 데이터를 가져옴
            String xml = fetchBpmnFromSupabase(defPath, version, authToken);
            if (xml == null) {
                throw new Exception("No BPMN data found for defPath: " + defPath + " and version: " + version);
            }

            processDefinition = bpmnXMLParser.parse(xml);
            tc.setSharedContext("def:" + defPath + "@" + version, processDefinition);
        }

        return processDefinition;
    }

    /**
     * Supabase에서 proc_def 테이블의 bpmn 컬럼 데이터를 가져오는 메서드
     */
    private String fetchBpmnFromSupabase(String defPath, String version, String authToken) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(SUPABASE_URL + "/rest/v1/proc_def")
                .newBuilder()
                .addQueryParameter("id", "eq." + defPath.replace(".bpmn", ""))
                .addQueryParameter("select", "bpmn");

        // if (version != null) {
        // urlBuilder.addQueryParameter("version", "eq." + version);
        // }

        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", authToken)
                .addHeader("Authorization", "Bearer " + authToken)
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code() + " - " + response.message());
            }

            String responseBody = response.body().string();
            JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();

            if (jsonArray.size() > 0) {
                JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
                return jsonObject.get("bpmn").getAsString();
            } else {
                return null;
            }
        }
    }
}
