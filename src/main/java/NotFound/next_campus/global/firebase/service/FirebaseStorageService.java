package NotFound.next_campus.global.firebase.service;

import NotFound.next_campus.global.exception.BusinessException;
import NotFound.next_campus.global.exception.ErrorCode;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
public class FirebaseStorageService {

    private static final String UPLOAD_FOLDER = "post-images";

    public String upload(MultipartFile file) {

        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }

        String objectName = UPLOAD_FOLDER + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Bucket bucket = StorageClient.getInstance().bucket();

        try (InputStream inputStream = file.getInputStream()) {

            Blob blob = bucket.create(objectName, inputStream, file.getContentType());
            blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
        } catch (IOException e) {
            log.error("Firebase 업로드 실패 objectName={}", objectName, e);
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }

        return objectName;
    }

    public void delete(String objectName) {

        if (objectName == null || objectName.isBlank()) {
            return;
        }

        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.get(objectName);

        if (blob != null) {
            blob.delete();
        }
    }

    public String getPublicUrl(String objectName) {

        String bucketName = StorageClient.getInstance().bucket().getName();
        String encodedObjectName = URLEncoder.encode(objectName, StandardCharsets.UTF_8);

        return "https://storage.googleapis.com/" + bucketName + "/" + encodedObjectName;
    }
}
