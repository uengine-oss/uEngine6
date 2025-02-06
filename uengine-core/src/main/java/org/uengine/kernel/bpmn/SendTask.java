package org.uengine.kernel.bpmn;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.uengine.kernel.*;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by uengine on 2017. 12. 4..
 */
public class SendTask extends DefaultActivity {
    public static final String SEND_TYPE_REST_API = "rest_api";
    public static final String SEND_TYPE_EMAIL = "email";

    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_PATCH = "PATCH";
    public static final String METHOD_DELETE = "DELETE";

    HttpHeader[] headers;
    String sendType;
    String selectedOut;
    String method;
    String API;

    @Override
    protected void executeActivity(ProcessInstance instance) throws Exception {

        if (SEND_TYPE_REST_API.equals(sendType)) {
            String payload = evaluateContent(instance, getInputPayloadTemplate()).toString();

            // RestTemplate을 사용하여 REST API 호출
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            // headers 배열을 사용하여 추가 헤더 설정
            if (headers != null) {
                for (HttpHeader header : headers) {
                    httpHeaders.set(header.getName(), evaluateContent(instance, header.getValue()).toString());
                }
            }

            HttpEntity<String> entity = new HttpEntity<>(payload, httpHeaders);

            try {
                ResponseEntity<String> response;
                if (METHOD_POST.equals(method)) {
                    response = restTemplate.postForEntity(new URL(API).toString(), entity, String.class);
                } else if (METHOD_GET.equals(method)) {
                    response = restTemplate.getForEntity(new URL(API).toString(), String.class);
                } else {
                    throw new UEngineException("지원되지 않는 HTTP 메서드: " + method);
                }
                String result = response.getBody();

                // 결과를 JSON 텍스트로 처리하는 로직 추가
                if (result != null) {
                    // JSON 텍스트로 결과를 selectedOut에 설정
                    instance.setBeanProperty(selectedOut, (Serializable) result);
                }
            } catch (Exception e) {
                throw new UEngineException("REST API 호출 중 오류 발생: ", e);
            }

        } else if (SEND_TYPE_EMAIL.equals(sendType)) {

            String payload = evaluateContent(instance, getInputPayloadTemplate()).toString();

            if (getDataInput() != null) {
                getDataInput().set(instance, "", payload);
            }

            List<String> messages = getMessageQueue(instance);
            if (messages == null) {
                messages = new ArrayList<String>();
                instance.getRootProcessInstance().getProcessTransactionContext().setSharedContext("messages", messages);
            }

            messages.add(payload);

        }

        super.executeActivity(instance);
    }

    public static List<String> getMessageQueue(ProcessInstance instance) throws Exception {
        return (List<String>) instance.getRootProcessInstance().getProcessTransactionContext()
                .getSharedContext("messages");
    }

    String inputPayloadTemplate;

    public String getInputPayloadTemplate() {
        return inputPayloadTemplate;
    }

    public void setInputPayloadTemplate(String inputPayloadTemplate) {
        this.inputPayloadTemplate = inputPayloadTemplate;
    }

    ProcessVariable dataInput;

    public ProcessVariable getDataInput() {
        return dataInput;
    }

    public void setDataInput(ProcessVariable dataInput) {
        this.dataInput = dataInput;
    }

}
