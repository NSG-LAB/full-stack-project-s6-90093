package com.example.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String benefitsJson;

    @Column(columnDefinition = "TEXT")
    private String estimatedCostJson;

    @Column(precision = 10, scale = 2)
    private BigDecimal expectedRoi = BigDecimal.ZERO;

    private Double roiPercentage = 0d;

    private String difficulty = "moderate";

    private String timeframe;

    @Column(columnDefinition = "TEXT")
    private String imagesJson;

    @Column(columnDefinition = "TEXT")
    private String tipsJson;

    @Column(columnDefinition = "TEXT")
    private String applicablePropertyTypesJson;

    @Column(columnDefinition = "TEXT")
    private String applicableCitiesJson;

    @Column(columnDefinition = "TEXT")
    private String applicableConditionsJson;

    @Column(columnDefinition = "TEXT")
    private String beforeAfterImagesJson;

    @Column(columnDefinition = "TEXT")
    private String relatedRecommendationIdsJson;

    private Integer priority = 0;

    @Column(nullable = false)
    private Boolean isActive = true;

    private Long createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBenefitsJson() {
        return benefitsJson;
    }

    public void setBenefitsJson(String benefitsJson) {
        this.benefitsJson = benefitsJson;
    }

    public String getEstimatedCostJson() {
        return estimatedCostJson;
    }

    public void setEstimatedCostJson(String estimatedCostJson) {
        this.estimatedCostJson = estimatedCostJson;
    }

    public BigDecimal getExpectedRoi() {
        return expectedRoi;
    }

    public void setExpectedRoi(BigDecimal expectedRoi) {
        this.expectedRoi = expectedRoi;
    }

    public Double getRoiPercentage() {
        return roiPercentage;
    }

    public void setRoiPercentage(Double roiPercentage) {
        this.roiPercentage = roiPercentage;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public String getImagesJson() {
        return imagesJson;
    }

    public void setImagesJson(String imagesJson) {
        this.imagesJson = imagesJson;
    }

    public String getTipsJson() {
        return tipsJson;
    }

    public void setTipsJson(String tipsJson) {
        this.tipsJson = tipsJson;
    }

    public String getApplicablePropertyTypesJson() {
        return applicablePropertyTypesJson;
    }

    public void setApplicablePropertyTypesJson(String applicablePropertyTypesJson) {
        this.applicablePropertyTypesJson = applicablePropertyTypesJson;
    }

    public String getApplicableCitiesJson() {
        return applicableCitiesJson;
    }

    public void setApplicableCitiesJson(String applicableCitiesJson) {
        this.applicableCitiesJson = applicableCitiesJson;
    }

    public String getApplicableConditionsJson() {
        return applicableConditionsJson;
    }

    public void setApplicableConditionsJson(String applicableConditionsJson) {
        this.applicableConditionsJson = applicableConditionsJson;
    }

    public String getBeforeAfterImagesJson() {
        return beforeAfterImagesJson;
    }

    public void setBeforeAfterImagesJson(String beforeAfterImagesJson) {
        this.beforeAfterImagesJson = beforeAfterImagesJson;
    }

    public String getRelatedRecommendationIdsJson() {
        return relatedRecommendationIdsJson;
    }

    public void setRelatedRecommendationIdsJson(String relatedRecommendationIdsJson) {
        this.relatedRecommendationIdsJson = relatedRecommendationIdsJson;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
