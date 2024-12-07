package me.zedaster.moderationservice.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.zedaster.moderationservice.dto.ModerationStatus;

import java.time.Instant;

@Entity
@Table(name = "article_summaries", indexes = {
        @Index(name = "article_summaries_creator_id_index", columnList = "creatorId")
})
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class ArticleSummaryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    @Convert(converter = ModeratingStatusConverter.class)
    private ModerationStatus status;

    @Column(nullable = false)
    private Long creatorId;

    @OneToOne(mappedBy = "article", fetch = FetchType.EAGER, orphanRemoval = true)
    private ModeratorCommentEntity moderatorComment;

    public ArticleSummaryEntity(String title, Instant createdAt, Long creatorId) {
        this.title = title;
        this.createdAt = createdAt;
        this.status = ModerationStatus.MODERATING;
        this.creatorId = creatorId;
    }
}
