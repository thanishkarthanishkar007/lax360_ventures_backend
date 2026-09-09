package com.lax360.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "products")
public class Product {

    @Id
    private String id;
    private String name;
    private String tag;
    private String slug;
    private String liveUrl;
    private String imageUrl;
    private String index;
    private String description;
    private List<String> points;
    private Instant createdAt;

    public Product() {
    }

    public Product(String name, String tag, String slug, String liveUrl, String imageUrl, String index, String description, List<String> points) {
        this.name = name;
        this.tag = tag;
        this.slug = slug;
        this.liveUrl = liveUrl;
        this.imageUrl = imageUrl;
        this.index = index;
        this.description = description;
        this.points = points;
        this.createdAt = Instant.now();
    }

    public Product(String name, String tag, String description, List<String> points) {
        this.name = name;
        this.tag = tag;
        this.description = description;
        this.points = points;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getLiveUrl() {
        return liveUrl;
    }

    public void setLiveUrl(String liveUrl) {
        this.liveUrl = liveUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getPoints() {
        return points;
    }

    public void setPoints(List<String> points) {
        this.points = points;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
