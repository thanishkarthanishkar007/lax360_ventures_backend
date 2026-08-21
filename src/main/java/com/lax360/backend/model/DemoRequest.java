package com.lax360.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "demo_requests")
public class DemoRequest {

    @Id
    private String id;

    private String fullName;
    private String email;
    private String mobileNumber;
    private String company;
    private String product;
    private String customRequirement;
    private Instant createdAt;

    public DemoRequest() {
    }

    public DemoRequest(String fullName, String email, String mobileNumber, String company, String product, String customRequirement) {
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.company = company;
        this.product = product;
        this.customRequirement = customRequirement;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getCustomRequirement() {
        return customRequirement;
    }

    public void setCustomRequirement(String customRequirement) {
        this.customRequirement = customRequirement;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
