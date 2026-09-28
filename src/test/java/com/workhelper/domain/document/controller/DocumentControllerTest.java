package com.workhelper.domain.document.controller;

import com.workhelper.domain.document.service.DocumentService;
import com.workhelper.global.security.jwt.JwtUserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DocumentControllerTest {
    @Test
    void downloadReturnsPdfAttachment() {
        DocumentService service = mock(DocumentService.class);
        byte[] pdf = "%PDF-test".getBytes(StandardCharsets.US_ASCII);
        when(service.getPdf(7L, 3L, 10L)).thenReturn(pdf);
        DocumentController controller = new DocumentController(service);

        var response = controller.download(new JwtUserPrincipal(7L, "user@example.com", List.of()), 3L, 10L);

        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getContentDisposition().getType()).isEqualTo("attachment");
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("labor-complaint-10.pdf");
        assertThat(response.getBody()).isSameAs(pdf);
    }
}
