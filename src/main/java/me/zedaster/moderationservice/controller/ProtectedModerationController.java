package me.zedaster.moderationservice.controller;

import lombok.RequiredArgsConstructor;
import me.zedaster.moderationservice.dto.*;
import me.zedaster.moderationservice.service.NoSuchArticleException;
import me.zedaster.moderationservice.service.ArticleModerationService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.ConnectException;
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
    private final ArticleModerationService articleModerationService;

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
        return articleModerationService.getUserArticleSummaries(userId, pageNumber);
    }

    /**
     * Get particular article by ID
     * @param role Role of authorized user
     * @param userId User ID of authorized user
     * @param id Article ID
     * @return Article object
     */
    @GetMapping("/articles/{id}")
    @Transactional
    public Article getParticularArticle(@RequestParam("tokenPayload[role]") Role role,
                                        @RequestParam("tokenPayload[userId]") long userId,
                                        @PathVariable("id") long id) throws ConnectException {
        if (role == Role.USER && !articleModerationService.userOwnArticle(userId, id)) {
            throw new NoAccessException();
        }

        return articleModerationService.getArticle(id);
    }

    /**
     * Create article
     * @param userId User ID of authorized user
     * @param createArticleDto Article data
     */
    @PostMapping("/articles")
    public void createArticle(@RequestParam("tokenPayload[userId]") long userId, @RequestBody CreateArticleDto createArticleDto) throws ConnectException {
        articleModerationService.saveArticle(userId, createArticleDto);
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
            @RequestParam(value = "page", required = false) Integer pageNumber) throws ConnectException {
        assertRoleCanModerate(role);
        return articleModerationService.getArticleSummaries(pageNumber);
    }

    /**
     * Accept and publish article
     * @param role Role of authorized user
     * @param id Article ID
     * @throws NoAccessException If user has no access to this method
     * @throws NoSuchArticleException If article was not found by specified ID
     */
    @PatchMapping("/articles/{id}/accept")
    public void acceptArticle(@RequestParam("tokenPayload[role]") Role role, @PathVariable("id") long id) throws ConnectException {
        assertRoleCanModerate(role);
        articleModerationService.publishArticle(id);
    }

    /**
     * Ask for edit article by moderator
     * @param role Role of authorized user
     * @param id Article ID
     * @param askEditDto Comment about the article
     * @throws NoAccessException If user has no access to this method
     * @throws NoSuchArticleException If article was not found by specified ID
     */
    @PatchMapping("/articles/{id}/askEdit")
    public void askEditArticle(
            @RequestParam("tokenPayload[role]") Role role,
            @PathVariable("id") long id,
            @RequestBody AskEditDto askEditDto) {
        assertRoleCanModerate(role);
        articleModerationService.askEdit(id, askEditDto.getComment());
    }

    /**
     * Decline and remove article
     * @param role Role of authorized user
     * @param id Article ID
     * @throws NoAccessException If user has no access to this method
     * @throws NoSuchArticleException If article was not found by specified ID
     */
    @DeleteMapping("/articles/{id}")
    public void removeArticle(@RequestParam("tokenPayload[role]") Role role, @PathVariable("id") long id) throws ConnectException {
        assertRoleCanModerate(role);
        articleModerationService.removeArticle(id);
    }

    private void assertRoleCanModerate(Role role)  {
        if (!role.canModerate()) {
            throw new NoAccessException();
        }
    }
}
