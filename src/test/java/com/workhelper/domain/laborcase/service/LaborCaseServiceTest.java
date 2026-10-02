package com.workhelper.domain.laborcase.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.workhelper.domain.consultation.repository.ConsultationMessageRepository;
import com.workhelper.domain.laborcase.dto.LaborCaseResponseDto;
import com.workhelper.domain.laborcase.entity.CaseCategory;
import com.workhelper.domain.laborcase.entity.CaseStatus;
import com.workhelper.domain.laborcase.entity.LaborCase;
import com.workhelper.domain.laborcase.repository.LaborCaseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LaborCaseServiceTest {

    private final LaborCaseRepository cases = mock(LaborCaseRepository.class);
    private final ConsultationMessageRepository messages = mock(ConsultationMessageRepository.class);
    private final LaborCaseService service = new LaborCaseService(cases, messages);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void listAndDetailIncludeEntityTimestampsInJson() throws Exception {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-09-28T10:15:30+09:00");
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-09-29T11:20:45+09:00");
        LaborCase laborCase = LaborCase.builder()
                .userId(7L)
                .title("임금 체불")
                .category(CaseCategory.WAGE)
                .status(CaseStatus.CREATED)
                .build();
        ReflectionTestUtils.setField(laborCase, "caseId", 3L);
        ReflectionTestUtils.setField(laborCase, "createdAt", createdAt);
        ReflectionTestUtils.setField(laborCase, "updatedAt", updatedAt);

        when(cases.findByUserId(eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(laborCase)));
        when(cases.findByCaseIdAndUserId(3L, 7L)).thenReturn(Optional.of(laborCase));

        LaborCaseResponseDto listItem = service.getCases(7L, null, 0, 20).getContent().get(0);
        LaborCaseResponseDto detail = service.getCase(3L, 7L);

        for (LaborCaseResponseDto response : List.of(listItem, detail)) {
            assertThat(response.getCreatedAt()).isEqualTo(createdAt);
            assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
            JsonNode json = objectMapper.valueToTree(response);
            assertThat(OffsetDateTime.parse(json.path("createdAt").asText())).isEqualTo(createdAt);
            assertThat(OffsetDateTime.parse(json.path("updatedAt").asText())).isEqualTo(updatedAt);
            assertThat(json.path("caseId").asLong()).isEqualTo(3L);
        }
    }
}
