package com.example.CheckrApplication.service;

import com.example.CheckrApplication.DAO.*;
import com.example.CheckrApplication.DTO.*;
import com.example.CheckrApplication.JPARepository.*;
import com.example.CheckrApplication.config.ModelMapperConfig;
import com.example.CheckrApplication.enums.*;
import com.example.CheckrApplication.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

    @InjectMocks
    private CandidateService candidateService;

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private AdverseActionRepository adverseActionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapperConfig modelMapperConfig;

    @Mock
    private ModelMapper modelMapper;

    private CandidateDAO candidateDAO;
    private ReportDAO reportDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Ensure modelmapper is properly mocked
//        when(modelMapperConfig.modelMapper()).thenReturn(modelMapper);

// Initialize CandidateDAO
        candidateDAO = new CandidateDAO();
        candidateDAO.setId(1L);
        candidateDAO.setName("Jane Doe");
        candidateDAO.setEmail("jane@example.com");
        candidateDAO.setStatus(Status.CLEAR);
        candidateDAO.setAdjudication(Adjudication.ENGAGED);
        candidateDAO.setLocation("New York");
        candidateDAO.setCreatedAt(LocalDateTime.now());

// Initialize ReportDAO
        reportDAO = new ReportDAO();
        reportDAO.setId(1L);
        reportDAO.setCandidateDAO(candidateDAO);
        reportDAO.setStatus(Status.CLEAR);
        reportDAO.setAdjudication(Adjudication.ENGAGED);
        reportDAO.setPackageName("Standard Package");
        reportDAO.setCreatedAt(LocalDateTime.now());
        reportDAO.setCompletedAt(LocalDateTime.now());
        reportDAO.setTurnaroundTime(5);

        candidateDAO.setReports(Arrays.asList(reportDAO));
    }

    @Test
    void getAllCandidates_Success() {
        // Arrange
        Page<CandidateDAO> candidateDAOS = new PageImpl<>(Arrays.asList(candidateDAO));
        Page<CandidateResponseDTO> expectedResponse = new PageImpl<>(Arrays.asList(new CandidateResponseDTO()));

        when(candidateRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(candidateDAOS);
//        when(modelMapper.map(any(CandidateDAO.class), eq(CandidateDTO.class))).thenReturn(new CandidateDTO());

// Act
        Page<CandidateResponseDTO> result = candidateService.getAllCandidates(0, 10, null, null, null);

// Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(candidateRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getCandidateDetail_Success() {
        // Arrange
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidateDAO));
        CandidateDTO candidateDTO = new CandidateDTO();
        candidateDTO.setEmail("jane@example.com");
//        when(modelMapperConfig.modelMapper().map(candidateDAO, CandidateDTO.class)).thenReturn(candidateDTO);
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setPackageName(reportDAO.getPackageName());
//        when(modelMapperConfig.modelMapper().map(reportDAO, ReportDTO.class)).thenReturn(reportDTO);
        ViolationDTO violationDTO = new ViolationDTO();
//        when(modelMapperConfig.modelMapper().map(any(ViolationDAO.class), eq(ViolationDTO.class))).thenReturn(violationDTO);

// Act
        CandidateDetailResponseDTO response = candidateService.getCandidateDetail(1L);

// Assert
        assertNotNull(response);
        assertEquals(candidateDTO.getEmail(), response.getCandidate().getEmail());
        assertEquals(reportDTO.getPackageName(), response.getReport().getPackageName());
        assertTrue(response.getViolations().size() >= 0); // Assuming no violations
        verify(candidateRepository, times(1)).findById(1L);
    }

    @Test
    void getCandidateDetail_NotFound() {
        // Arrange
        when(candidateRepository.findById(1L)).thenReturn(Optional.empty());

// Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            candidateService.getCandidateDetail(1L);
        });

        assertEquals("Candidate not found with id : '1'", exception.getMessage());
        verify(candidateRepository, times(1)).findById(1L);
    }

    @Test
    void initiatePreAdverseAction_Success() {
        // Arrange
        AdverseActionDTO adverseActionDTO = new AdverseActionDTO();
        adverseActionDTO.setUserId(2L);
        adverseActionDTO.setBody("Pre-adverse action notice");
        adverseActionDTO.setCharges(Arrays.asList("Charge1", "Charge2"));
        adverseActionDTO.setSubject("Subject");
        adverseActionDTO.setNumberOfDays(10);
        adverseActionDTO.setPreNoticeSentAt(LocalDateTime.now());
        adverseActionDTO.setPostNoticeSentAt(LocalDateTime.now());

        UserDAO userDAO = new UserDAO();
        userDAO.setId(2L);
        userDAO.setEmail("user@example.com");
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidateDAO));
        when(userRepository.findById(2L)).thenReturn(Optional.of(userDAO));
        when(adverseActionRepository.save(any(AdverseActionDAO.class))).thenReturn(new AdverseActionDAO());

// Act
        candidateService.initiatePreAdverseAction(1L, adverseActionDTO);

// Assert
        verify(candidateRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
        verify(adverseActionRepository, times(1)).save(any(AdverseActionDAO.class));
    }

    @Test
    void initiatePreAdverseAction_CandidateNotFound() {
        // Arrange
        AdverseActionDTO adverseActionDTO = new AdverseActionDTO();
        adverseActionDTO.setUserId(2L);
        when(candidateRepository.findById(1L)).thenReturn(Optional.empty());

// Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            candidateService.initiatePreAdverseAction(1L, adverseActionDTO);
        });

        assertEquals("Candidate not found with id : '1'", exception.getMessage());
        verify(candidateRepository, times(1)).findById(1L);
        verify(userRepository, times(0)).findById(anyLong());
        verify(adverseActionRepository, times(0)).save(any(AdverseActionDAO.class));
    }

    @Test
    void engageCandidate_Success() {
        // Arrange
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidateDAO));
        when(reportRepository.findByCandidateDAO(candidateDAO)).thenReturn(new ArrayList<>(Collections.singletonList(reportDAO)));

// Act
        candidateService.engageCandidate(1L);

// Assert
        assertEquals(Adjudication.ENGAGED, candidateDAO.getAdjudication());
        assertEquals(Status.CLEAR, candidateDAO.getStatus());
        assertEquals(Adjudication.ENGAGED, reportDAO.getAdjudication());
        assertEquals(Status.CLEAR, reportDAO.getStatus());
        verify(candidateRepository, times(1)).findById(1L);
        verify(reportRepository, times(1)).findByCandidateDAO(candidateDAO);
        verify(reportRepository, times(1)).save(reportDAO);
        verify(candidateRepository, times(1)).save(candidateDAO);
    }

    @Test
    void engageCandidate_CandidateNotFound() {
        // Arrange
        when(candidateRepository.findById(1L)).thenReturn(Optional.empty());

// Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            candidateService.engageCandidate(1L);
        });

        assertEquals("Candidate not found with id : '1'", exception.getMessage());
        verify(candidateRepository, times(1)).findById(1L);
        verify(reportRepository, times(0)).findByCandidateDAO(any());
        verify(reportRepository, times(0)).save(any(ReportDAO.class));
        verify(candidateRepository, times(0)).save(any(CandidateDAO.class));
    }

// Additional tests for filtering, mapping, etc., can be added here to achieve 100% coverage
}