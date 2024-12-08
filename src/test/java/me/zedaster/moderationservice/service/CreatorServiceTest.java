package me.zedaster.moderationservice.service;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import me.zedaster.moderationservice.configuration.microservice.AuthServiceConfiguration;
import me.zedaster.moderationservice.dto.Creator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.net.URI;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {CreatorService.class, CreatorServiceTest.TestConfig.class})
@WireMockTest(httpPort = 8080, proxyMode = true)
public class CreatorServiceTest {

    @Autowired
    private CreatorService creatorService;

    @TestConfiguration
    public static class TestConfig {
        @Bean
        @Primary
        public AuthServiceConfiguration authServiceConfiguration() {
            AuthServiceConfiguration configuration = Mockito.mock(AuthServiceConfiguration.class);
            Mockito.when(configuration.getUri()).thenReturn(URI.create("http://auth-service:8080"));
            return configuration;
        }
    }

    /**
     * Test for getting creators by their IDs
     */
    @Test
    public void getCreatorsByIds() {
        String returnJson = """
                ["one", "two"]""";

        stubFor(get(urlEqualTo("/internal/profile/usernames?ids=1&ids=2"))
                .withHost(equalTo("auth-service"))
                .willReturn(okJson(returnJson)));

        List<Creator> creators = creatorService.getCreatorsByIds(List.of(1L, 2L));
        assertEquals(2, creators.size());
        assertEquals(1, creators.get(0).getId());
        assertEquals("one", creators.get(0).getName());
        assertEquals(2, creators.get(1).getId());
        assertEquals("two", creators.get(1).getName());
    }

    /**
     * Test for getting creator by their ID
     */
    @Test
    public void getCreatorById() {
        stubFor(get(urlEqualTo("/internal/profile/1/username"))
                .withHost(equalTo("auth-service"))
                .willReturn(ok().withBody("one")));

        Creator creator = creatorService.getCreator(1L);
        assertEquals(1, creator.getId());
        assertEquals("one", creator.getName());
    }

    // TODO: Server unavailable test
}
