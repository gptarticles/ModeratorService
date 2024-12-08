package me.zedaster.moderationservice.service;

import me.zedaster.moderationservice.dto.Creator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreatorService {
    /**
     *
     * @param userIds
     * @throws ExternalConnectException
     * @return
     */
    public List<Creator> getCreatorsByIds(List<Long> userIds) {
        // TODO
        return null;
    }

    /**
     *
     * @param creatorId
     * @throws ExternalConnectException
     * @return
     */
    public Creator getCreator(Long creatorId) {
        // TODO
        return null;
    }
}
