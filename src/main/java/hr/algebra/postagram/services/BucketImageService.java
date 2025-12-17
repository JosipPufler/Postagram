package hr.algebra.postagram.services;

import hr.algebra.postagram.models.ImageData;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.UUID;

@Service
@Profile("s3-storage")
public class BucketImageService implements ImageService {

    private final S3Client s3;
    private final String bucket = "postagram-139176429013";

    public BucketImageService(S3Client s3) {
        this.s3 = s3;
    }

    @Override
    public String store(byte[] data, String contentType) {
        String key = UUID.randomUUID().toString();

        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(data)
        );

        return key;
    }

    @Override
    public ImageData load(String key) {
        ResponseBytes<GetObjectResponse> object = s3.getObject(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build(),
                ResponseTransformer.toBytes()
        );

        byte[] data = object.asByteArray();
        String contentType = object.response().contentType();

        return new ImageData(data, contentType);
    }

    @Override
    public void delete(String key) {
        s3.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
    }
}
