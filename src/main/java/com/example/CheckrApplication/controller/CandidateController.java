package com.example.CheckrApplication.controller;

import com.example.CheckrApplication.DTO.*;
import com.example.CheckrApplication.service.CandidateService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/checkr/candidates/v1")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @GetMapping
    public ResponseEntity<Page<CandidateResponseDTO>> getAllCandidates(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "adjudication", required = false) String adjudication
    ) {
        log.info("started / in CandidateController.getAllCandidates");
        Page<CandidateResponseDTO> candidates = candidateService.getAllCandidates(page, size, search, status, adjudication);
        return ResponseEntity.ok(candidates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateDetailResponseDTO> getCandidateById(@PathVariable Long id) {
        log.info("started /{id} in CandidateController.getCandidateById");
        CandidateDetailResponseDTO candidate = candidateService.getCandidateDetail(id);
        return ResponseEntity.ok(candidate);
    }

    @PostMapping("/{id}/pre-adverse-action")
    public ResponseEntity<?> initiatePreAdverseAction(@PathVariable Long id, @Valid @RequestBody AdverseActionDTO adverseActionDTO){
        log.info("started /id/pre-adverse-action in CandidateController.initiatePreAdverseAction");
        candidateService.initiatePreAdverseAction(id, adverseActionDTO);
        return ResponseEntity.ok(new ApiResponse(true,"Candidate Pre-Adverse Action notice sent"));
    }


    @PostMapping("/{id}/engage")
    public ResponseEntity<?> engageCandidate(@PathVariable Long id) {
        log.info("started /id/engage in CandidateController.engageCandidate");
        candidateService.engageCandidate(id);
        return ResponseEntity.ok(new ApiResponse(true, "Candidate engaged successfully"));
    }


    @GetMapping("/export")
    public ResponseEntity<?> exportCandidates(
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        try {
            candidateService.exportCandidatesPDF(fromDate, toDate);
            return ResponseEntity.ok()
                .body(Map.of("message", "Report has been generated and sent to your email"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate and send report: " + e.getMessage()));
        }
    }

    @GetMapping("/adverse-action")
    public ResponseEntity<Page<AdverseActionResponseDTO>> getAdverseActions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Started /adverse-action in CandidateController.getAdverseActions");
            Page<AdverseActionResponseDTO> adverseActions = candidateService.getAdverseActions(page, size);
            return ResponseEntity.ok(adverseActions);
        } catch (Exception e) {
            log.error("Error in /adverse-action: " + e.getMessage(), e);
            throw new RuntimeException("Failed to fetch adverse actions", e);
        }
    }
}
