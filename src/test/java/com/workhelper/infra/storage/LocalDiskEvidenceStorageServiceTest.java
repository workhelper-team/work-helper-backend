package com.workhelper.infra.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalDiskEvidenceStorageServiceTest {
    @TempDir
    Path storageRoot;

    @Test
    void fileUrlKeepsExistingLocalUriWithoutRequiringPresigning() {
        EvidenceStorageService storage = new LocalDiskEvidenceStorageService(storageRoot.toString());
        String objectKey = "evidences/3/image.png";

        assertThat(storage.getFileUrl(objectKey))
                .isEqualTo(storageRoot.resolve(objectKey).toUri().toString())
                .startsWith("file:");
        assertThatThrownBy(() -> storage.getPresignedGetUrl(objectKey))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
