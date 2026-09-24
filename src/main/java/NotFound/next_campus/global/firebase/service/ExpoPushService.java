package NotFound.next_campus.global.firebase.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExpoPushService {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
    private static final RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    public void sendToMultiple(List<String> tokens, String title, String body, Map<String, String> data) {

        try {
            for (String token : tokens) {

                Map<String, Object> message = new HashMap<>();
                message.put("to", token);
                message.put("title", title);
                message.put("body", body);
                if(data != null && !data.isEmpty()) {
                    message.put("data", data);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> request = new HttpEntity<>(
                        objectMapper.writeValueAsString(message),
                        headers
                );

                ResponseEntity<String> response =
                        restTemplate.postForEntity(EXPO_PUSH_URL, request, String.class);

                log.info("Expo push 전송 성공");
            }
        } catch(Exception e) {
            log.error("Expo push 전송 실패", e);
        }
    }
}
