package org.example.service;

import org.example.domain.NewsArticle;
import org.example.repository.NewsArticleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock
    private NewsArticleRepository newsArticleRepository;

    @InjectMocks
    private NewsService newsService;

    @Test
    void getLatestNews_returnsTop10() {
        List<NewsArticle> list = List.of(new NewsArticle("t","o","l","d", LocalDateTime.now()));
        when(newsArticleRepository.findTop10ByOrderByPubDateDesc()).thenReturn(list);
        List<NewsArticle> result = newsService.getLatestNews();
        assertEquals(1, result.size());
        verify(newsArticleRepository, times(1)).findTop10ByOrderByPubDateDesc();
    }

    @Test
    void getAllNews_returnsPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<NewsArticle> page = new PageImpl<>(List.of());
        when(newsArticleRepository.findAllByOrderByPubDateDesc(pageable)).thenReturn(page);
        Page<NewsArticle> result = newsService.getAllNews(pageable);
        assertNotNull(result);
        verify(newsArticleRepository, times(1)).findAllByOrderByPubDateDesc(pageable);
    }
}
