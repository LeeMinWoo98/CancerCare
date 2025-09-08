package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.NewsArticle;
import org.example.repository.NewsArticleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class NewsService implements ApplicationRunner {

    private final NewsArticleRepository newsArticleRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${naver.client.id}")
    private String naverClientId;

    @Value("${naver.client.secret}")
    private String naverClientSecret;

    // 6대 암에 집중한 키워드
    private static final List<String> KEYWORDS = List.of(
            "위암", "대장암", "유방암", "폐암", "간암", "자궁경부암"
    );

    public NewsService(NewsArticleRepository newsArticleRepository) {
        this.newsArticleRepository = newsArticleRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (newsArticleRepository.count() == 0) {
            System.out.println("초기 뉴스 데이터가 비어있습니다. 애플리케이션 시작 시 뉴스를 가져옵니다...");
            fetchAndSaveNews();
        }
    }

    // 매일 오후 9시 40분에 실행되도록 스케줄링 수정
    @Scheduled(cron = "0 30 09 * * ?")
    public void scheduleNewsFetch() {
        System.out.println("예정된 뉴스 가져오기 작업이 실행 중입니다...");
        fetchAndSaveNews();
    }

    public void fetchAndSaveNews() {
        System.out.println("6대 암 관련 최신 뉴스를 가져오는 중입니다...");
        List<NewsArticle> newArticles = new ArrayList<>();

        for (String keyword : KEYWORDS) {
            try {
                // URL 인코딩 제거하고 직접 한글 사용
                String url = "https://openapi.naver.com/v1/search/news.json?query=" + keyword +
                        "&display=100" +
                        "&start=1" +
                        "&sort=date"; // 최신순으로 변경

                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Naver-Client-Id", naverClientId);
                headers.set("X-Naver-Client-Secret", naverClientSecret);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode items = root.path("items");

                for (JsonNode item : items) {
                    String pubDateStr = item.path("pubDate").asText();
                    DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
                    LocalDateTime pubDate = ZonedDateTime.parse(pubDateStr, formatter).toLocalDateTime();

                    if (pubDate.getYear() != 2025) {
                        continue;
                    }

                    String title = item.path("title").asText().replaceAll("<[^>]*>", "");
                    String originalLink = item.path("originallink").asText();

                    // 6대 암 관련 키워드가 실제로 포함되어 있는지 필터링
                    if (containsSixMajorCancers(title) && !newsArticleRepository.existsByTitle(title) && !originalLink.isEmpty()) {
                        String link = item.path("link").asText();
                        String description = item.path("description").asText().replaceAll("<[^>]*>", "");
                        newArticles.add(new NewsArticle(title, originalLink, link, description, pubDate));
                    }
                }
            } catch (Exception e) {
                System.err.println("키워드 '" + keyword + "'에 대한 뉴스를 가져오는 중 오류 발생: " + e.getMessage());
            }
        }

        if (!newArticles.isEmpty()) {
            newsArticleRepository.saveAll(newArticles);
            System.out.println(newArticles.size() + "개의 새로운 " + String.join(", ", KEYWORDS) + " 관련 기사가 저장되었습니다.");
        } else {
            System.out.println("새로운 6대 암 관련 기사를 찾지 못했습니다.");
        }
    }

    // 6대 암 관련 키워드 필터링 메서드
    private boolean containsSixMajorCancers(String title) {
        String[] sixMajorCancers = {"위암", "대장암", "유방암", "폐암", "간암", "자궁경부암"};
        String lowerTitle = title.toLowerCase();

        for (String cancer : sixMajorCancers) {
            if (lowerTitle.contains(cancer)) {
                return true;
            }
        }
        return false;
    }

    public List<NewsArticle> getLatestNews() {
        return newsArticleRepository.findTop10ByOrderByPubDateDesc();
    }

    public Page<NewsArticle> getAllNews(Pageable pageable) {
        return newsArticleRepository.findAllByOrderByPubDateDesc(pageable);
    }
}