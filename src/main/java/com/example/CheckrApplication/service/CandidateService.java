package com.example.CheckrApplication.service;

import com.example.CheckrApplication.DAO.AdverseActionDAO;
import com.example.CheckrApplication.DAO.CandidateDAO;
import com.example.CheckrApplication.DAO.ReportDAO;
import com.example.CheckrApplication.DAO.UserDAO;
import com.example.CheckrApplication.DTO.*;
import com.example.CheckrApplication.JPARepository.AdverseActionRepository;
import com.example.CheckrApplication.JPARepository.CandidateRepository;
import com.example.CheckrApplication.JPARepository.ReportRepository;
import com.example.CheckrApplication.JPARepository.UserRepository;
import com.example.CheckrApplication.config.ModelMapperConfig;
import com.example.CheckrApplication.enums.Adjudication;
import com.example.CheckrApplication.enums.Status;
import com.example.CheckrApplication.exception.BadRequestException;
import com.example.CheckrApplication.exception.ResourceNotFoundException;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.criteria.Predicate;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CandidateService {

    private static final Logger log = LoggerFactory.getLogger(CandidateService.class);

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AdverseActionRepository adverseActionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapperConfig modelMapperConfig;

    @Autowired
    private EmailService emailService;

    public Page<CandidateResponseDTO> getAllCandidates(int page, int size, String search, String status, String adjudication) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<CandidateDAO> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if(StringUtils.hasText(search)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
            }
            if(StringUtils.hasText(status)) {
                predicates.add(criteriaBuilder.equal(root.get("status"), Status.valueOf(status.toUpperCase())));
            }
            if(StringUtils.hasText(adjudication)) {
                predicates.add(criteriaBuilder.equal(root.get("adjudication"), Adjudication.valueOf(adjudication.toUpperCase())));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<CandidateDAO> candidates = candidateRepository.findAll(spec, pageable);
        return candidates.map(this::mapToCandidateResponse);
    }

    public CandidateDetailResponseDTO getCandidateDetail(Long id) {
        CandidateDAO candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", id));
        CandidateDetailResponseDTO response = new CandidateDetailResponseDTO();

        ModelMapper mapper = modelMapperConfig.modelMapper();
        if(mapper == null){
            mapper = new ModelMapper();
        }

        response.setCandidate(mapper.map(candidate, CandidateDTO.class));
        // Assuming each candidate has one report for simplicity
        ReportDAO report = candidate.getReports().stream().findFirst().orElse(null);
        if(report != null) {
            response.setReport(mapper.map(report, ReportDTO.class));
            List<ViolationDTO> violations = report.getViolations()
                    .stream()
                    .map(v -> modelMapperConfig.modelMapper().map(v, ViolationDTO.class))
                    .collect(Collectors.toList());
            response.setViolations(violations);
        }
        return response;
    }

    public void initiatePreAdverseAction(Long id, AdverseActionDTO adverseActionDTO){

        CandidateDAO candidateDAO = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", id));
        Long userId = adverseActionDTO.getUserId();
        UserDAO userDAO = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User","id",userId));
        AdverseActionDAO adverseActionDAO = getAdverseActionDAO(adverseActionDTO, candidateDAO, userDAO);
        adverseActionRepository.save(adverseActionDAO);

    }

    private static AdverseActionDAO getAdverseActionDAO(AdverseActionDTO adverseActionDTO, CandidateDAO candidateDAO, UserDAO userDAO) {
        AdverseActionDAO adverseActionDAO = new AdverseActionDAO();
        adverseActionDAO.setCandidateDAO(candidateDAO);
        adverseActionDAO.setUserDAO(userDAO);
        adverseActionDAO.setBody(adverseActionDTO.getBody());
        adverseActionDAO.setCharges(adverseActionDTO.getCharges());
        adverseActionDAO.setSubject(adverseActionDTO.getSubject());
        adverseActionDAO.setNumberOfDays(adverseActionDTO.getNumberOfDays());
        adverseActionDAO.setPostNoticeSentAt(adverseActionDTO.getPostNoticeSentAt());
        adverseActionDAO.setPreNoticeSentAt(adverseActionDTO.getPreNoticeSentAt());
        return adverseActionDAO;
    }

    public void engageCandidate(Long candidateId) {
        CandidateDAO candidateDAO = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", candidateId));
        List<ReportDAO> reports = reportRepository.findByCandidateDAO(candidateDAO);
        ReportDAO report = reports.stream()
                .max(Comparator.comparing(ReportDAO::getCreatedAt))
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", candidateId));

        report.setAdjudication(Adjudication.ENGAGED);
        report.setStatus(Status.CLEAR);
        candidateDAO.setAdjudication(Adjudication.ENGAGED);
        candidateDAO.setStatus(Status.CLEAR);
        reportRepository.save(report);
        candidateRepository.save(candidateDAO);
    }

    public void exportCandidatesPDF(LocalDate fromDate, LocalDate toDate) {
        try {
            // Get current user's email from security context
            String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            
            // Get candidates within date range
            ZonedDateTime fromDateTime = fromDate.atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime toDateTime = toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault());
            List<CandidateDAO> candidates = candidateRepository.findByCreatedAtBetween(
                fromDateTime.toLocalDateTime(),
                toDateTime.toLocalDateTime()
            );

            // Create PDF document
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            
            document.open();
            
            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Candidates Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));
            
            // Add report metadata
            document.add(new Paragraph("Generated by: " + userEmail));
            document.add(new Paragraph("Date Range: " + fromDate + " to " + toDate));
            document.add(new Paragraph("Total Candidates: " + candidates.size()));
            document.add(new Paragraph("\n"));
            
            // Create table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            
            // Add table headers
            String[] headers = {"Name", "Email", "Phone", "Created At", "Status", "Adjudication"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                cell.setBackgroundColor(new BaseColor(200, 200, 200));
                cell.setPadding(5);
                table.addCell(cell);
            }
            
            // Add candidate data
            for (CandidateDAO candidate : candidates) {
                table.addCell(candidate.getName() != null ? candidate.getName() : "N/A");
                table.addCell(candidate.getEmail() != null ? candidate.getEmail() : "N/A");
                table.addCell(candidate.getPhone() != null ? candidate.getPhone() : "N/A");
                table.addCell(candidate.getCreatedAt() != null ? candidate.getCreatedAt().toString() : "N/A");
                table.addCell(candidate.getStatus() != null ? candidate.getStatus().toString() : "N/A");
                table.addCell(candidate.getAdjudication() != null ? candidate.getAdjudication().toString() : "N/A");
            }
            
            document.add(table);
            document.close();
            
            // Send email with PDF attachment
            String subject = "Candidates Report (" + fromDate + " to " + toDate + ")";
            String body = "Please find attached the candidates report for the period " + fromDate + " to " + toDate + ".\n\n" +
                         "Total Candidates: " + candidates.size();
            
            emailService.sendEmailWithAttachment(
                userEmail,
                subject,
                body,
                baos.toByteArray(),
                "candidates_report.pdf"
            );
            
        } catch (Exception e) {
            log.error("Error generating and sending PDF report: " + e.getMessage(), e);
            throw new RuntimeException("Failed to generate and send PDF report", e);
        }
    }

    public Page<AdverseActionResponseDTO> getAdverseActions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdverseActionDAO> adverseActions = adverseActionRepository.findAll(pageable);
        
        return adverseActions.map(action -> {
            AdverseActionResponseDTO dto = new AdverseActionResponseDTO();
            dto.setCandidateName(action.getCandidateDAO().getName());
            dto.setStatus(action.getCandidateDAO().getStatus());
            dto.setPreNoticeSentAt(action.getPreNoticeSentAt());
            dto.setPostNoticeSentAt(action.getPostNoticeSentAt());
            return dto;
        });
    }

    private CandidateResponseDTO mapToCandidateResponse(CandidateDAO candidate) {
        CandidateResponseDTO response = new CandidateResponseDTO();
        response.setId(candidate.getId());
        response.setName(candidate.getName());
        response.setEmail(candidate.getEmail());
        response.setStatus(String.valueOf(candidate.getReports().get(0).getStatus()));
        response.setAdjudication(String.valueOf(candidate.getReports().get(0).getAdjudication()));
        response.setLocation(candidate.getLocation()); // Assuming location is based on zipcode
        response.setDateInfo(Date.from(candidate.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
        return response;
    }
}
