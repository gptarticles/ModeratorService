package me.zedaster.moderationservice.controller;

import lombok.RequiredArgsConstructor;
import me.zedaster.moderationservice.dto.*;
import me.zedaster.moderationservice.service.ArticleNotFoundException;
import me.zedaster.moderationservice.service.ModerationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for protected moderation endpoints
 */
@RestController
@RequestMapping("/protected/moderation")
@RequiredArgsConstructor
public class ProtectedModerationController {
    /**
     * Moderation service
     */
    private final ModerationService moderationService;

    /**
     * Get moderating articles of authorized user
     * @param userId User ID of authorized user
     * @param pageNumber Page number
     * @return List of summaries
     */
    @GetMapping("/articles/user")
    public List<ArticleSummary> getUserArticles(
            @RequestParam("tokenPayload[userId]") long userId,
            @RequestParam(value = "page", required = false) Integer pageNumber) {
        return moderationService.getUserArticleSummaries(userId, pageNumber);
    }

    /**
     * Get particular article by ID
     * @param role Role of authorized user
     * @param userId User ID of authorized user
     * @param id Article ID
     * @return Article object
     */
    @GetMapping("/articles/{id}")
    public Article getParticularArticle(@RequestParam("tokenPayload[role]") Role role,
                                        @RequestParam("tokenPayload[userId]") long userId,
                                        @PathVariable("id") long id)
            throws ArticleNotFoundException, NoAccessException {
        if (role == Role.USER && !moderationService.userOwnArticle(userId, id)) {
            throw new NoAccessException();
        }

        return moderationService.getArticle(id);
    }

    /**
     * Create article
     * @param userId User ID of authorized user
     * @param createArticleDto Article data
     */
    @PostMapping("/articles")
    public void createArticle(@RequestParam("tokenPayload[userId]") long userId, @RequestBody CreateArticleDto createArticleDto) {
        moderationService.createArticle(userId, createArticleDto);
    }

    /**
     * Get all moderating articles
     * @param role Role of authorized user
     * @param pageNumber Page number
     * @return List of summaries with creator data
     * @throws NoAccessException If user has no access to this method
     */
    @GetMapping("/articles")
    public List<NamedArticleSummary> getAllArticles(
            @RequestParam("tokenPayload[role]") Role role,
            @RequestParam(value = "page", required = false) Integer pageNumber) throws NoAccessException {
        assertRoleCanModerate(role);
        return moderationService.getArticleSummaries(pageNumber);
    }

    /**
     * Accept and publish article
     * @param role Role of authorized user
     * @param id Article ID
     * @throws NoAccessException If user has no access to this method
     * @throws ArticleNotFoundException If article was not found by specified ID
     */
    @PatchMapping("/articles/{id}/accept")
    public void acceptArticle(@RequestParam("tokenPayload[role]") Role role, @PathVariable("id") long id)
            throws NoAccessException, ArticleNotFoundException {
        assertRoleCanModerate(role);
        moderationService.publishArticle(id);
    }

    /**
     * Ask for edit article by moderator
     * @param role Role of authorized user
     * @param id Article ID
     * @param askEditDto Comment about the article
     * @throws NoAccessException If user has no access to this method
     * @throws ArticleNotFoundException If article was not found by specified ID
     */
    @PatchMapping("/articles/{id}/askEdit")
    public void askEditArticle(
            @RequestParam("tokenPayload[role]") Role role,
            @PathVariable("id") long id,
            @RequestBody AskEditDto askEditDto)
            throws NoAccessException, ArticleNotFoundException {
        assertRoleCanModerate(role);
        moderationService.askEdit(id, askEditDto.getComment());
    }

    /**
     * Decline and remove article
     * @param role Role of authorized user
     * @param id Article ID
     * @throws NoAccessException If user has no access to this method
     * @throws ArticleNotFoundException If article was not found by specified ID
     */
    @DeleteMapping("/articles/{id}")
    public void removeArticle(@RequestParam("tokenPayload[role]") Role role, @PathVariable("id") long id)
            throws NoAccessException, ArticleNotFoundException {
        assertRoleCanModerate(role);
        moderationService.removeArticle(id);
    }

    private void assertRoleCanModerate(Role role) throws NoAccessException {
        if (!role.canModerate()) {
            throw new NoAccessException();
        }
    }
}
