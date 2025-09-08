package io.hpp.noosphere.agent.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import io.hpp.noosphere.agent.IntegrationTest;
import org.springframework.test.context.web.WebAppConfiguration;

@CucumberContextConfiguration
@IntegrationTest
@WebAppConfiguration
public class CucumberTestContextConfiguration {}
