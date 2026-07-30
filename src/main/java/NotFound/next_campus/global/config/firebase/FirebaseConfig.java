package NotFound.next_campus.global.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${fcm.firebase.config.path}")
    private String SERVICE_ACCOUNT_PATH;
    @Value("${fcm.firebase.storage-bucket}")
    private String STORAGE_BUCKET;

    @Bean
    public FirebaseApp firebaseApp() {
        try(FileInputStream serviceAccount = new FileInputStream(SERVICE_ACCOUNT_PATH)) {

            /*FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(
                            GoogleCredentials.fromStream(
                                    new ClassPathResource(SERVICE_ACCOUNT_PATH).getInputStream()
                            )
                    )
                    .build();*/

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(STORAGE_BUCKET)
                    .build();

            log.info("Successfully initialized firebase app");
            return FirebaseApp.initializeApp(options);
        } catch (IOException exception) {
            log.error("Fail to initialize firebase app{}", exception.getMessage());
            return null;
        }
    }
}
