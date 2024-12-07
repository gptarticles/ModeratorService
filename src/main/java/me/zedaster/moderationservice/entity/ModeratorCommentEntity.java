package me.zedaster.moderationservice.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "moderator_comments")
@Getter
@Setter
@EqualsAndHashCode(of = "articleId")
@NoArgsConstructor
public class ModeratorCommentEntity {
    @Id
    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(nullable = false)
    private String comment;

    @OneToOne
    @MapsId
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleSummaryEntity article;

    public ModeratorCommentEntity(Long articleId, String comment) {
        this.articleId = articleId;
        this.comment = comment;
    }
}
