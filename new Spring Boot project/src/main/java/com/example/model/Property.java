package com.example.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String propertyType;

    private Integer age;

    private Integer builUpArea;

    private Integer bedrooms;

    private Integer bathrooms;

    @Column(columnDefinition = "TEXT")
    private String locationJson;

    private String locationCity;

    private String condition;

    @Column(precision = 15, scale = 2)
    private BigDecimal currentValue;

    @Column(columnDefinition = "TEXT")
    private String featuresJson;

    @Column(columnDefinition = "TEXT")
    private String imagesJson;

    @Column(columnDefinition = "TEXT")
    private String improvementsJson;

    @Column(precision = 15, scale = 2)
    private BigDecimal estimatedNewValue;

    @Column(precision = 15, scale = 2)
    private BigDecimal potentialValueIncrease;

    private String status = "pending";

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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getBuilUpArea() {
        return builUpArea;
    }

    public void setBuilUpArea(Integer builUpArea) {
        this.builUpArea = builUpArea;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public String getLocationJson() {
        return locationJson;
    }

    public void setLocationJson(String locationJson) {
        this.locationJson = locationJson;
    }

    public String getLocationCity() {
        return locationCity;
    }

    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public String getFeaturesJson() {
        return featuresJson;
    }

    public void setFeaturesJson(String featuresJson) {
        this.featuresJson = featuresJson;
    }

    public String getImagesJson() {
        return imagesJson;
    }

    public void setImagesJson(String imagesJson) {
        this.imagesJson = imagesJson;
    }

    public String getImprovementsJson() {
        return improvementsJson;
    }

    public void setImprovementsJson(String improvementsJson) {
        this.improvementsJson = improvementsJson;
    }

    public BigDecimal getEstimatedNewValue() {
        return estimatedNewValue;
    }

    public void setEstimatedNewValue(BigDecimal estimatedNewValue) {
        this.estimatedNewValue = estimatedNewValue;
    }

    public BigDecimal getPotentialValueIncrease() {
        return potentialValueIncrease;
    }

    public void setPotentialValueIncrease(BigDecimal potentialValueIncrease) {
        this.potentialValueIncrease = potentialValueIncrease;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
