package org.example.controller;

import org.example.domain.NewsArticle;
import org.example.service.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * 건강 뉴스 페이지("/news/healthNews") 요청을 처리하며, 페이지네이션을 지원합니다.
     * @param pageable Spring이 URL 파라미터(예: ?page=0&size=10)를 기반으로 자동 생성하는 페이지 요청 객체
     * @param model 뷰에 전달할 모델
     * @return "news/healthNews" 뷰 이름
     */
    @GetMapping("/healthNews")
    public String healthNewsPage(
            @PageableDefault(size = 10, sort = "pubDate", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        // 서비스로부터 페이징된 뉴스 데이터를 가져옵니다.
        Page<NewsArticle> newsPage = newsService.getAllNews(pageable);

        // Page 객체 자체를 모델에 추가합니다.
        model.addAttribute("newsPage", newsPage);

        // 계산 로직 추가
        int totalPages = newsPage.getTotalPages();
        int currentPage = newsPage.getNumber();

        // 현재 페이지 중심으로 앞뒤 2개씩 총 5개의 페이지 번호 표시
        int startPage = Math.max(0, currentPage - 2);
        int endPage = Math.min(totalPages - 1, currentPage + 2);

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);


        // templates/news/healthNews.html 을 렌더링합니다.
        return "news/healthNews";
    }
}

