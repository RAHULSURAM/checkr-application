package com.example.CheckrApplication.controller;

import com.example.CheckrApplication.DTO.*;
import com.example.CheckrApplication.service.CandidateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateControllerTest {

    @InjectMocks
    private CandidateController candidateController;

    @Mock
    private CandidateService candidateService;

    private Page<CandidateResponseDTO> candidatePage;
    private CandidateDetailResponseDTO candidateDetailResponseDTO;
    private ApiResponse apiResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

// Initialize CandidateResponseDTO
        CandidateResponseDTO candidateResponseDTO = new CandidateResponseDTO();
        candidateResponseDTO.setId(1L);
        candidateResponseDTO.setName("Jane Doe");
        candidateResponseDTO.setEmail("jane@example.com");
        candidateResponseDTO.setStatus("CLEAR");
        candidateResponseDTO.setAdjudication("ENGAGED");
        candidateResponseDTO.setLocation("New York");
        candidateResponseDTO.setDateInfo(new Date());

// Initialize Page
        candidatePage = new PageImpl<>(Arrays.asList(candidateResponseDTO));

// Initialize CandidateDetailResponseDTO
        CandidateDTO candidateDTO = new CandidateDTO();
        candidateDTO.setId(1L);
        candidateDTO.setName("Jane Doe");
        candidateDTO.setEmail("jane@example.com");
        // ... set other fields as needed

        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setId(1L);
        reportDTO.setStatus("CLEAR");
        reportDTO.setAdjudication("ENGAGED");
        // ... set other fields as needed

        ViolationDTO violationDTO = new ViolationDTO();
        violationDTO.setId(1L);
        violationDTO.setType("SSN_VERIFICATION");
        violationDTO.setDescription("Valid SSN");
        violationDTO.setStatus("CLEAR");
        violationDTO.setDate(null); // Set appropriate date

        candidateDetailResponseDTO = new CandidateDetailResponseDTO();
        candidateDetailResponseDTO.setCandidate(candidateDTO);
        candidateDetailResponseDTO.setReport(reportDTO);
        candidateDetailResponseDTO.setViolations(Arrays.asList(violationDTO));

// Initialize ApiResponse
        apiResponse = new ApiResponse(true, "Candidate engaged successfully");
    }

    @Test
    void getAllCandidates_Success() {
        // Arrange
        when(candidateService.getAllCandidates(0, 10, null, null, null)).thenReturn(candidatePage);

// Act
        ResponseEntity<Page<CandidateResponseDTO>> response = candidateController.getAllCandidates(0, 10, null, null, null);

// Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(candidatePage, response.getBody());
        verify(candidateService, times(1)).getAllCandidates(0, 10, null, null, null);
    }

    @Test
    void getCandidateById_Success() {
        // Arrange
        when(candidateService.getCandidateDetail(1L)).thenReturn(candidateDetailResponseDTO);

// Act
        ResponseEntity<CandidateDetailResponseDTO> response = candidateController.getCandidateById(1L);

// Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(candidateDetailResponseDTO, response.getBody());
        verify(candidateService, times(1)).getCandidateDetail(1L);
    }

    @Test
    void initiatePreAdverseAction_Success() {
        // Arrange
        AdverseActionDTO adverseActionDTO = new AdverseActionDTO();
        adverseActionDTO.setUserId(1L);
        adverseActionDTO.setBody("Pre-adverse action body");
        // ... set other fields as needed

        doNothing().when(candidateService).initiatePreAdverseAction(1L, adverseActionDTO);

// Act
        ResponseEntity<?> response = candidateController.initiatePreAdverseAction(1L, adverseActionDTO);
        ApiResponse responseBody = (ApiResponse) response.getBody();

// Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(responseBody);
        assertEquals(apiResponse.isSuccess(), responseBody.isSuccess());
        verify(candidateService, times(1)).initiatePreAdverseAction(1L, adverseActionDTO);
    }

    @Test
    void engageCandidate_Success() {
        // Arrange
        doNothing().when(candidateService).engageCandidate(1L);

// Act
        ResponseEntity<?> response = candidateController.engageCandidate(1L);
        ApiResponse responseBody = (ApiResponse) response.getBody();

// Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(responseBody);
        assertEquals(apiResponse.isSuccess(), responseBody.isSuccess());
        verify(candidateService, times(1)).engageCandidate(1L);
    }
}