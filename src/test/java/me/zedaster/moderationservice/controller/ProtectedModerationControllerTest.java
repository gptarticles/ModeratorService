package me.zedaster.moderationservice.controller;

import me.zedaster.moderationservice.TestUtils;
import me.zedaster.moderationservice.dto.*;
import me.zedaster.moderationservice.service.ArticleModerationService;
import me.zedaster.moderationservice.service.NoSuchArticleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link ProtectedModerationController}
 */
@WebMvcTest(ProtectedModerationController.class)
public class ProtectedModerationControllerTest {
    private static final String ASK_EDIT_JSON = """
            {
              "comment": "Test comment"
            }""";

    private static final long NOT_FOUND_ARTICLE_ID = 404L;

    private static final String NOT_FOUND_ARTICLE_MESSAGE = "Article with ID 404 was not found!";

    /**
     * Mock MVC object for testing.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Mock moderation service
     */
    @MockitoBean
    private ArticleModerationService articleModerationService;

    @Test
    public void getUserArticles() throws Exception {
        Instant createdAt1 = TestUtils.createInstantOf(2021, 1, 1, 12, 30, 0);
        Instant createdAt2 = TestUtils.createInstantOf(2022, 1, 1, 12, 30, 0);

        List<ArticleSummary> articleSummaries = List.of(
                new ArticleSummary(1L, "a".repeat(15), createdAt1, ModerationStatus.MODERATING, null),
                new ArticleSummary(2L, "b".repeat(15), createdAt2, ModerationStatus.MODERATING, null)
        );
        when(articleModerationService.getUserArticleSummaries(123, 12)).thenReturn(articleSummaries);

        mockMvc.perform(get("/protected/moderation/articles/user?tokenPayload.sub=123&page=12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("a".repeat(15)))
                .andExpect(jsonPath("$[0].createdAt").value("2021-01-01T12:30:00Z"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("b".repeat(15)))
                .andExpect(jsonPath("$[1].createdAt").value("2022-01-01T12:30:00Z"));
    }

    @Test
    public void getParticularArticleByUser() throws Exception {
        Instant createdAt = TestUtils.createInstantOf(2023, 1, 1, 12, 30, 0);
        Article fakeArticle = new Article(1L, "a".repeat(15), "b".repeat(100), createdAt,
                ModerationStatus.MODERATING, null, new Creator(456L, "Alice"));
        when(articleModerationService.userOwnArticle(456L, 123L)).thenReturn(true);
        when(articleModerationService.getArticle(123)).thenReturn(fakeArticle);

        mockMvc.perform(get("/protected/moderation/articles/123?tokenPayload.sub=456&tokenPayload.role=USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(6)))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("a".repeat(15)))
                .andExpect(jsonPath("$.content").value("b".repeat(100)))
                .andExpect(jsonPath("$.createdAt").value("2023-01-01T12:30:00Z"))
                .andExpect(jsonPath("$.status").value("MODERATING"))
                .andExpect(jsonPath("$.creator.*", hasSize(2)))
                .andExpect(jsonPath("$.creator.id").value(456L))
                .andExpect(jsonPath("$.creator.name").value("Alice"));

        verify(articleModerationService, times(1)).userOwnArticle(456L, 123L);
    }

    @Test
    public void getParticularArticleWithCommentByUser() throws Exception {
        Instant createdAt = TestUtils.createInstantOf(2023, 1, 1, 12, 30, 0);
        Article fakeArticle = new Article(1L, "a".repeat(15), "b".repeat(100), createdAt,
                ModerationStatus.EDIT_REQUESTED, "Test comment", new Creator(456L, "Alice"));
        when(articleModerationService.userOwnArticle(456L, 123L)).thenReturn(true);
        when(articleModerationService.getArticle(123)).thenReturn(fakeArticle);

        mockMvc.perform(get("/protected/moderation/articles/123?tokenPayload.sub=456&tokenPayload.role=USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(7)))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("a".repeat(15)))
                .andExpect(jsonPath("$.content").value("b".repeat(100)))
                .andExpect(jsonPath("$.createdAt").value("2023-01-01T12:30:00Z"))
                .andExpect(jsonPath("$.status").value("EDIT_REQUESTED"))
                .andExpect(jsonPath("$.moderatorComment").value("Test comment"))
                .andExpect(jsonPath("$.creator.*", hasSize(2)))
                .andExpect(jsonPath("$.creator.id").value(456L))
                .andExpect(jsonPath("$.creator.name").value("Alice"));
    }

    @Test
    public void getParticularArticleByModerator() throws Exception {
        Instant createdAt = TestUtils.createInstantOf(2023, 1, 1, 12, 30, 0);
        Article fakeArticle = new Article(1L, "a".repeat(15), "b".repeat(100), createdAt,
                ModerationStatus.MODERATING, null, new Creator(456L, "Alice"));
        when(articleModerationService.getArticle(1)).thenReturn(fakeArticle);

        mockMvc.perform(get("/protected/moderation/articles/1?tokenPayload.role=MODERATOR&tokenPayload.sub=777"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(6)))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("a".repeat(15)))
                .andExpect(jsonPath("$.content").value("b".repeat(100)))
                .andExpect(jsonPath("$.createdAt").value("2023-01-01T12:30:00Z"))
                .andExpect(jsonPath("$.status").value("MODERATING"))
                .andExpect(jsonPath("$.creator.*", hasSize(2)))
                .andExpect(jsonPath("$.creator.id").value(456L))
                .andExpect(jsonPath("$.creator.name").value("Alice"));

        verify(articleModerationService, never()).userOwnArticle(anyLong(), anyLong());
    }

    @Test
    public void getParticularForeignArticleByUser() throws Exception {
        when(articleModerationService.userOwnArticle(456L, 123L)).thenReturn(false);

        testNoAccess(get("/protected/moderation/articles/123?tokenPayload.sub=456&tokenPayload.role=USER"));

        verify(articleModerationService, times(1)).userOwnArticle(456L, 123L);
        verify(articleModerationService, never()).getArticle(anyLong());
    }

    @Test
    public void getNonExistentParticularArticle() throws Exception {
        doThrow(new NoSuchArticleException(NOT_FOUND_ARTICLE_ID))
                .when(articleModerationService).getArticle(NOT_FOUND_ARTICLE_ID);

        testNotFound(get("/protected/moderation/articles/%d?tokenPayload.role=MODERATOR&tokenPayload.sub=777"
                .formatted(NOT_FOUND_ARTICLE_ID)));
    }

    @Test
    public void createArticle() throws Exception {
        CreateArticleDto createDto = new CreateArticleDto("a".repeat(15), "b".repeat(100));
        doNothing().when(articleModerationService).saveArticle(123, createDto);

        String contentJson = """
                {
                  "title": "%s",
                  "content": "%s"
                }""".formatted("a".repeat(15), "b".repeat(100));

        mockMvc.perform(post("/protected/moderation/articles?tokenPayload.sub=123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content((contentJson)))
                .andExpect(status().is(200));

        verify(articleModerationService, times(1)).saveArticle(123, createDto);
    }

    @Test
    public void getAllArticles() throws Exception {
        Instant createdAt1 = TestUtils.createInstantOf(2021, 1, 1, 12, 30, 0);
        Instant createdAt2 = TestUtils.createInstantOf(2022, 1, 1, 12, 30, 0);

        List<NamedArticleSummary> articleSummaries = List.of(
                new NamedArticleSummary(ArticleSummary.builder()
                        .id(1L)
                        .title("a".repeat(15))
                        .createdAt(createdAt1)
                        .status(ModerationStatus.MODERATING)
                        .build(), new Creator(1L, "Alice")),
                new NamedArticleSummary(ArticleSummary.builder()
                        .id(2L)
                        .title("b".repeat(15))
                        .createdAt(createdAt2)
                        .status(ModerationStatus.MODERATING)
                        .build(), new Creator(2L, "Bob")
                ));
        when(articleModerationService.getArticleSummaries(12)).thenReturn(articleSummaries);

        mockMvc.perform(get("/protected/moderation/articles?tokenPayload.role=MODERATOR&tokenPayload.sub=123&page=12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("a".repeat(15)))
                .andExpect(jsonPath("$[0].createdAt").value("2021-01-01T12:30:00Z"))
                .andExpect(jsonPath("$[0].creator.*", hasSize(2)))
                .andExpect(jsonPath("$[0].creator.id").value(1L))
                .andExpect(jsonPath("$[0].creator.name").value("Alice"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("b".repeat(15)))
                .andExpect(jsonPath("$[1].createdAt").value("2022-01-01T12:30:00Z"))
                .andExpect(jsonPath("$[1].creator.*", hasSize(2)))
                .andExpect(jsonPath("$[1].creator.id").value(2L))
                .andExpect(jsonPath("$[1].creator.name").value("Bob"));
    }

    @Test
    public void getAllArticlesWithWrongRole() throws Exception {
        testNoAccess(get("/protected/moderation/articles?tokenPayload.role=USER&tokenPayload.sub=123&page=12"));
    }

    @Test
    public void acceptArticle() throws Exception {
        doNothing().when(articleModerationService).publishArticle(1L);
        mockMvc.perform(patch("/protected/moderation/articles/1/accept?tokenPayload.role=MODERATOR"))
                .andExpect(status().isOk());
        verify(articleModerationService, times(1)).publishArticle(1L);
    }

    @Test
    public void acceptArticleWithWrongRole() throws Exception {
        testNoAccess(patch("/protected/moderation/articles/321/accept?tokenPayload.role=USER"));
    }

    @Test
    public void acceptNonExistentArticle() throws Exception {
        doThrow(new NoSuchArticleException(NOT_FOUND_ARTICLE_ID))
                .when(articleModerationService).publishArticle(NOT_FOUND_ARTICLE_ID);
        testNotFound(patch("/protected/moderation/articles/%d/accept?tokenPayload.role=MODERATOR"
                .formatted(NOT_FOUND_ARTICLE_ID)));
    }

    @Test
    public void askEdit() throws Exception {
        doNothing().when(articleModerationService).askEdit(321L, "Test comment");

        mockMvc.perform(patch("/protected/moderation/articles/321/askEdit?tokenPayload.role=MODERATOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content((ASK_EDIT_JSON)))
                .andExpect(status().isOk());

        verify(articleModerationService, times(1)).askEdit(321L, "Test comment");
    }

    @Test
    public void askEditWrongRole() throws Exception {
        testNoAccess(patch("/protected/moderation/articles/321/askEdit?tokenPayload.role=USER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ASK_EDIT_JSON));
    }

    @Test
    public void askEditNonExistentArticle() throws Exception {
        doThrow(new NoSuchArticleException(NOT_FOUND_ARTICLE_ID))
                .when(articleModerationService).askEdit(eq(NOT_FOUND_ARTICLE_ID), anyString());
        testNotFound(patch("/protected/moderation/articles/%d/askEdit?tokenPayload.role=MODERATOR"
                .formatted(NOT_FOUND_ARTICLE_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ASK_EDIT_JSON));
        // Fix InvalidUseOfMatchersException above
    }

    @Test
    public void removeArticle() throws Exception {
        doNothing().when(articleModerationService).removeArticle(123);
        mockMvc.perform(delete("/protected/moderation/articles/123?tokenPayload.role=MODERATOR"))
                .andExpect(status().isOk());
        verify(articleModerationService, times(1)).removeArticle(123);
    }

    @Test
    public void removeArticleWrongRole() throws Exception {
        testNoAccess(delete("/protected/moderation/articles/321?tokenPayload.role=USER"));
    }

    @Test
    public void removeNonExistentArticle() throws Exception {
        doThrow(new NoSuchArticleException(NOT_FOUND_ARTICLE_ID))
                .when(articleModerationService).removeArticle(NOT_FOUND_ARTICLE_ID);
        testNotFound(delete("/protected/moderation/articles/%s?tokenPayload.role=MODERATOR"
                .formatted(NOT_FOUND_ARTICLE_ID)));
    }

    private void testNoAccess(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        mockMvc.perform(requestBuilder)
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$.message").value("You don't have access to this resource!"));
    }

    private void testNotFound(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        mockMvc.perform(requestBuilder)
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_ARTICLE_MESSAGE));
    }
}
