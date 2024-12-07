package me.zedaster.moderationservice.service;

import me.zedaster.moderationservice.dto.Creator;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.util.List;

@Service
public class CreatorService {
    public List<Creator> getCreatorsByIds(List<Long> userIds) throws ConnectException {
        // TODO
        return null;
    }

    public Creator getCreator(Long creatorId) throws ConnectException {
        // TODO
        return null;
    }
}
