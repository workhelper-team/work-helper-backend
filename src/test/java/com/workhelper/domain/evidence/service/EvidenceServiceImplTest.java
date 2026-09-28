package com.workhelper.domain.evidence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workhelper.domain.evidence.entity.AnalysisStatus;
import com.workhelper.domain.evidence.entity.Evidence;
import com.workhelper.domain.evidence.repository.EvidenceRepository;
import com.workhelper.domain.laborcase.entity.LaborCase;
import com.workhelper.domain.laborcase.repository.LaborCaseRepository;
import com.workhelper.infra.ai.AiClient;
import com.workhelper.infra.ai.AiIntegrationException;
import com.workhelper.infra.ai.dto.EvidenceAnalysisAiRequest;
import com.workhelper.infra.ai.dto.EvidenceAnalysisAiResponse;
import com.workhelper.infra.storage.EvidenceStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EvidenceServiceImplTest {
    private final EvidenceRepository evidenceRepository = mock(EvidenceRepository.class);
    private final LaborCaseRepository laborCaseRepository = mock(LaborCaseRepository.class);
    private final EvidenceStorageService storage = mock(EvidenceStorageService.class);
    private final AiClient aiClient = mock(AiClient.class);
    private final PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    private final EvidenceServiceImpl service = new EvidenceServiceImpl(evidenceRepository,
            laborCaseRepository, storage, aiClient, new ObjectMapper(), transactions);
    private Evidence evidence;

    @BeforeEach
    void setUp() {
        LaborCase laborCase = LaborCase.builder().userId(7L).build();
        ReflectionTestUtils.setField(laborCase, "caseId", 3L);
        evidence = Evidence.builder().laborCase(laborCase).storagePath("evidences/3/file.pdf")
                .mimeType("application/pdf").originalName("file.pdf")
                .description("context").analysisStatus(AnalysisStatus.PENDING).build();
        ReflectionTestUtils.setField(evidence, "evidenceId", 9L);
        when(laborCaseRepository.findByCaseIdAndUserId(3L, 7L)).thenReturn(Optional.of(laborCase));
        when(evidenceRepository.findByEvidenceIdAndLaborCase_CaseId(9L, 3L))
                .thenReturn(Optional.of(evidence));
        when(transactions.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        when(storage.getPresignedGetUrl("evidences/3/file.pdf"))
                .thenReturn("https://private.example/file?signature=example");
    }

    @Test
    void sendsPresignedUrlAndDescriptionAndCommitsResult() {
        when(aiClient.analyzeEvidence(any())).thenReturn(new EvidenceAnalysisAiResponse("ocr", "summary"));

        var response = service.analyzeEvidence(7L, 3L, 9L);

        ArgumentCaptor<EvidenceAnalysisAiRequest> request = ArgumentCaptor.forClass(EvidenceAnalysisAiRequest.class);
        verify(aiClient).analyzeEvidence(request.capture());
        assertThat(request.getValue().fileUrl()).isEqualTo("https://private.example/file?signature=example");
        assertThat(request.getValue().userContext()).isEqualTo("context");
        assertThat(response.extractedText()).isEqualTo("ocr");
        assertThat(response.analysisResult().path("analysisSummary").asText()).isEqualTo("summary");
        assertThat(evidence.getAnalysisStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        InOrder order = inOrder(transactions, aiClient);
        order.verify(transactions).commit(any());
        order.verify(aiClient).analyzeEvidence(any());
        order.verify(transactions).commit(any());
    }

    @Test
    void nullAndBlankDescriptionBecomeEmptyContext() {
        when(aiClient.analyzeEvidence(any())).thenReturn(new EvidenceAnalysisAiResponse("ocr", "summary"));
        for (String description : new String[]{null, "  "}) {
            ReflectionTestUtils.setField(evidence, "description", description);
            service.analyzeEvidence(7L, 3L, 9L);
        }
        ArgumentCaptor<EvidenceAnalysisAiRequest> requests = ArgumentCaptor.forClass(EvidenceAnalysisAiRequest.class);
        verify(aiClient, times(2)).analyzeEvidence(requests.capture());
        assertThat(requests.getAllValues()).allSatisfy(request -> assertThat(request.userContext()).isEmpty());
    }

    @Test
    void aiFailureCommitsFailedWithoutSavingResult() {
        AiIntegrationException failure = new AiIntegrationException(
                AiIntegrationException.Kind.TIMEOUT, null, "timeout", null);
        when(aiClient.analyzeEvidence(any())).thenThrow(failure);

        assertThatThrownBy(() -> service.analyzeEvidence(7L, 3L, 9L)).isSameAs(failure);
        assertThat(evidence.getAnalysisStatus()).isEqualTo(AnalysisStatus.FAILED);
        assertThat(evidence.getExtractedText()).isNull();
        assertThat(evidence.getAnalysisResult()).isNull();
        verify(transactions, times(2)).commit(any());
    }

    @Test
    void patchOnlyChangesExtractedTextAndKeepsAnalysisSummary() {
        evidence.updateAnalysisResult("original", new ObjectMapper().createObjectNode()
                .put("analysisSummary", "prior summary"), AnalysisStatus.COMPLETED);
        when(storage.getFileUrl("evidences/3/file.pdf")).thenReturn("https://example/file");

        var response = service.updateExtractedText(7L, 3L, 9L, "corrected");

        assertThat(response.extractedText()).isEqualTo("corrected");
        assertThat(response.analysisResult().path("analysisSummary").asText()).isEqualTo("prior summary");
        assertThat(evidence.getAnalysisStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        verifyNoInteractions(aiClient);
        verify(storage, never()).getPresignedGetUrl(any());
    }

    @Test
    void patchRejectsEvidenceWhileAnalysisIsProcessing() {
        evidence.updateAnalysisStatus(AnalysisStatus.PROCESSING);

        assertThatThrownBy(() -> service.updateExtractedText(7L, 3L, 9L, "corrected"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(evidence.getExtractedText()).isNull();
        verifyNoInteractions(aiClient);
        verify(storage, never()).getFileUrl(any());
        verify(storage, never()).getPresignedGetUrl(any());
    }
}
