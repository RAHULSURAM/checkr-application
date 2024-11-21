package com.example.CheckrApplication;

import com.example.CheckrApplication.JPARepository.CandidateRepository;
import com.example.CheckrApplication.JPARepository.ReportRepository;
import com.example.CheckrApplication.JPARepository.UserRepository;
import com.example.CheckrApplication.controller.AuthenticationController;
import com.example.CheckrApplication.controller.CandidateController;
import com.example.CheckrApplication.service.AuthenticationService;
import com.example.CheckrApplication.service.CandidateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class CheckrApplicationTests {

	@Autowired
	private AuthenticationController authenticationController;

	@Autowired
	private CandidateController candidateController;

	@Autowired
	private CandidateRepository candidateRepository;

	@Autowired
	private ReportRepository reportRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private CandidateService candidateService;


	@Test
	void contextLoads() {
		assertThat(authenticationController).isNotNull();
		assertThat(candidateController).isNotNull();
		assertThat(authenticationService).isNotNull();
		assertThat(candidateService).isNotNull();

		assertThat(candidateRepository).isNotNull();
		assertThat(reportRepository).isNotNull();
		assertThat(userRepository).isNotNull();
	}

}
