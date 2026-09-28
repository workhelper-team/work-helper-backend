package com.workhelper.domain.evidence.controller;

import com.workhelper.domain.evidence.service.EvidenceService;
import com.workhelper.global.security.jwt.JwtUserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

class EvidenceControllerTest {
    @Test
    void allowsPdfUpload() {
        EvidenceService service = mock(EvidenceService.class);
        EvidenceController controller = new EvidenceController(service);
        JwtUserPrincipal principal = new JwtUserPrincipal(7L, "user@example.com", List.of());
        MockMultipartFile file = new MockMultipartFile("file", "evidence.pdf",
                "application/pdf", "pdf content".getBytes());

        controller.uploadEvidence(principal, 3L, file, "description");

        verify(service).uploadEvidence(eq(7L), eq(3L), same(file), eq("description"));
    }
}
