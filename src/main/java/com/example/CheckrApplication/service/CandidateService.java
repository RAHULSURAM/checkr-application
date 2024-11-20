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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AdverseActionRepository adverseActionRepository;

//    @Autowired
//    private AdverseActionDTO adverseActionDTO;

//    @Autowired
//    private UserDAO userDAO;
//
//    @Autowired
//    private CandidateDAO candidateDAO;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapperConfig modelMapperConfig;

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
        AdverseActionDAO adverseActionDAO = new AdverseActionDAO();
        adverseActionDAO.setCandidateDAO(candidateDAO);
        adverseActionDAO.setUserDAO(userDAO);
        adverseActionDAO.setBody(adverseActionDTO.getBody());
        adverseActionDAO.setCharges(adverseActionDTO.getCharges());
        adverseActionDAO.setSubject(adverseActionDTO.getSubject());
        adverseActionDAO.setNumberOfDays(adverseActionDTO.getNumberOfDays());
        adverseActionDAO.setPostNoticeSentAt(adverseActionDTO.getPostNoticeSentAt());
        adverseActionDAO.setPreNoticeSentAt(adverseActionDTO.getPreNoticeSentAt());
//        modelMapperConfig.modelMapper().map(adverseActionDTO, AdverseActionDAO.class);

        adverseActionRepository.save(adverseActionDAO);

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

//    public CandidateDTO createCandidate(CandidateDTO candidateDTO) {
//        if(candidateRepository.findByEmail(candidateDTO.getEmail()).isPresent()) {
//            throw new BadRequestException("Email is already in use.");
//        }
//        CandidateDAO candidate = modelMapper.map(candidateDTO, CandidateDAO.class);
//        CandidateDAO savedCandidate = candidateRepository.save(candidate);
//        return modelMapper.map(savedCandidate, CandidateDTO.class);
//    }

//    public CandidateDTO updateCandidate(Long id, CandidateDTO candidateDTO) {
//        CandidateDAO candidate = candidateRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", id));
//        // Update fields
//        candidate.setName(candidateDTO.getName());
//        candidate.setEmail(candidateDTO.getEmail());
//        // ... other fields
//        CandidateDAO updatedCandidate = candidateRepository.save(candidate);
//        return modelMapper.map(updatedCandidate, CandidateDTO.class);
//    }

//    public void deleteCandidate(Long id) {
//        CandidateDAO candidate = candidateRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", id));
//        candidateRepository.delete(candidate);
//    }

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
