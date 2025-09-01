package org.example.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "health_news")
@Getter
@Setter
@NoArgsConstructor
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long articleId;

    @Column(nullable = false)
    private String title;

    @Column(name = "original_link", nullable = false)
    private String originalLink;

    @Column(nullable = false)
    private String link;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "pub_date", nullable = false)
    private LocalDateTime pubDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public NewsArticle(String title, String originalLink, String link, String description, LocalDateTime pubDate) {
        this.title = title;
        this.originalLink = originalLink;
        this.link = link;
        this.description = description;
        this.pubDate = pubDate;
    }
}
