package org.example.repository;

import org.example.domain.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
     * 중복된 ORDER BY 제거 - Pageable에 정렬 정보가 포함되어 있으므로 단순화
     * @param pageable 페이지 요청 정보 (정렬 포함)
     * @return 페이징된 뉴스 목록
     */
    @Query("SELECT n FROM NewsArticle n")
    Page<NewsArticle> findAllNews(Pageable pageable);

    /**
     * 기존 메서드 유지 (필요시 사용)
     */
    Page<NewsArticle> findAllByOrderByPubDateDesc(Pageable pageable);
}