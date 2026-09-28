package com.workhelper.infra.ai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DocumentDraftAiResponse(
        Boolean success,
        Long caseId,
        Complainant complainant,
        Respondent respondent,
        Facts facts,
        Content content) {

    public record Complainant(
            String name,
            LocalDate birthDate,
            String address,
            String phone,
            String mobilePhone,
            String email,
            Boolean receiveStatus) {
    }

    public record Respondent(
            String companyName,
            String name,
            String phone,
            String address,
            BusinessType businessType,
            String employeeCount) {
    }

    public enum BusinessType {
        BUSINESS,
        CONSTRUCTION
    }

    public record Facts(
            LocalDate hireDate,
            LocalDate resignationDate,
            EmploymentStatus employmentStatus,
            String jobDescription,
            String payDay,
            ContractType contractType,
            BigDecimal unpaidWages,
            BigDecimal unpaidSeverancePay,
            BigDecimal unpaidOtherAmount) {
    }

    public enum EmploymentStatus {
        EMPLOYED,
        RESIGNED
    }

    public enum ContractType {
        WRITTEN,
        VERBAL
    }

    public record Content(
            String claimReason,
            String targetLaborOffice,
            BigDecimal totalUnpaidAmount) {
    }
}
