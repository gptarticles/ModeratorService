package me.zedaster.moderationservice.repository;

import me.zedaster.moderationservice.entity.ArticleSummaryEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleSummaryRepository extends CrudRepository<ArticleSummaryEntity, Long> {
    List<ArticleSummaryEntity> findAllByCreatorId(long creatorId, Pageable pageable);

    List<ArticleSummaryEntity> findAll(PageRequest pageRequest);

    boolean existsByIdAndCreatorId(long articleId, long creatorId);
    // TODO

}
