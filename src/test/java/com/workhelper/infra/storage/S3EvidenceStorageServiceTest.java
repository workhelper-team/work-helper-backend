package com.workhelper.infra.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class S3EvidenceStorageServiceTest {
    private final S3Client s3Client = mock(S3Client.class);
    private final S3Presigner presigner = mock(S3Presigner.class);
    private S3EvidenceStorageService storage;

    @BeforeEach
    void setUp() {
        S3ClientBuilder clientBuilder = mock(S3ClientBuilder.class, RETURNS_SELF);
        S3Presigner.Builder presignerBuilder = mock(S3Presigner.Builder.class, RETURNS_SELF);
        when(clientBuilder.build()).thenReturn(s3Client);
        when(presignerBuilder.build()).thenReturn(presigner);
        try (var clients = mockStatic(S3Client.class); var presigners = mockStatic(S3Presigner.class)) {
            clients.when(S3Client::builder).thenReturn(clientBuilder);
            presigners.when(S3Presigner::builder).thenReturn(presignerBuilder);
            storage = new S3EvidenceStorageService("private-bucket", "ap-northeast-2", Duration.ofMinutes(10));
        }
    }

    @Test
    void fileUrlAndAiUrlUseTheSamePresignedGetPolicy() throws Exception {
        String objectKey = "evidences/3/image.png";
        String url = "https://private-bucket.example/image.png?X-Amz-Signature=example";
        PresignedGetObjectRequest signedRequest = mock(PresignedGetObjectRequest.class);
        when(signedRequest.url()).thenReturn(URI.create(url).toURL());
        when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(signedRequest);

        assertThat(storage.getFileUrl(objectKey)).isEqualTo(url);
        assertThat(storage.getPresignedGetUrl(objectKey)).isEqualTo(url);

        ArgumentCaptor<GetObjectPresignRequest> requests = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        verify(presigner, times(2)).presignGetObject(requests.capture());
        assertThat(requests.getAllValues()).allSatisfy(request -> {
            assertThat(request.signatureDuration()).isEqualTo(Duration.ofMinutes(10));
            assertThat(request.getObjectRequest().bucket()).isEqualTo("private-bucket");
            assertThat(request.getObjectRequest().key()).isEqualTo(objectKey);
        });
        verifyNoInteractions(s3Client);
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/jpeg", "image/png"})
    void uploadPreservesImageContentTypeWithoutPublicAcl(String contentType) {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", contentType, new byte[]{1, 2});

        String objectKey = storage.save(3L, file);

        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(request.capture(), any(RequestBody.class));
        assertThat(request.getValue().contentType()).isEqualTo(contentType);
        assertThat(request.getValue().key()).isEqualTo(objectKey);
        assertThat(objectKey).startsWith("evidences/3/").doesNotContain("?");
        assertThat(request.getValue().aclAsString()).isNull();
    }
}
