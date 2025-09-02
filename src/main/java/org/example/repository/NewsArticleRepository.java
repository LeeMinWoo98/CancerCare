package org.example.repository;

import org.example.domain.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    /**
     * 제목으로 기사가 이미 존재하는지 확인합니다. (중복 방지용)
     */
    boolean existsByTitle(String title);

    /**
     * 최신 발행일 순으로 상위 10개의 기사를 조회합니다. (메인 페이지 미리보기용)
     */
    List<NewsArticle> findTop10ByOrderByPubDateDesc();

    /**
     * 최신 발행일 순으로 모든 기사를 페이지네이션하여 조회합니다.
     * @param pageable 페이지 요청 정보
     * @return 페이징된 뉴스 목록
     */
    Page<NewsArticle> findAllByOrderByPubDateDesc(Pageable pageable);
}

