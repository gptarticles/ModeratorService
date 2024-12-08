package me.zedaster.moderationservice.service;

import me.zedaster.moderationservice.configuration.microservice.AuthServiceConfiguration;
import me.zedaster.moderationservice.dto.Creator;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class CreatorService {

    private final RestClient restClient;

    public CreatorService(AuthServiceConfiguration configuration) {
        this.restClient = RestClient.create(configuration.getUri());
    }

    /**
     * Get creators by their ids.
     * @param userIds List of creator ids
     * @throws ExternalConnectException If the creator service is not available
     * @return List of creators
     */
    public List<Creator> getCreatorsByIds(List<Long> userIds) {
        try {
            URI uri = UriComponentsBuilder
                    .fromPath("/internal/profile/usernames")
                    .queryParam("ids", userIds)
                    .build()
                    .toUri();
            List<String> names = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<List<String>>() {})
                    .getBody();
            return IntStream.range(0, userIds.size())
                    .mapToObj(i -> new Creator(userIds.get(i), names.get(i)))
                    .toList();
        } catch (ResourceAccessException e) {
            throw new ExternalConnectException("Creator service is not available", e);
        }
    }

    /**
     * Get creator by their id.
     * @param creatorId Creator id
     * @throws ExternalConnectException If the creator service is not available
     * @return Creator
     */
    public Creator getCreator(Long creatorId) {
        try {
            URI uri = UriComponentsBuilder
                    .fromPath("/internal/profile/{id}/username")
                    .buildAndExpand(creatorId)
                    .toUri();
            String name = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(String.class)
                    .getBody();
            return new Creator(creatorId, name);
        } catch (ResourceAccessException e) {
            throw new ExternalConnectException("Creator service is not available", e);
        }
    }
}
