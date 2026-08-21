package com.lax360.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DemoRequestDto {

    @NotBlank(message = "Full name is required")
    @Size(max = 120, message = "Full name is too long")
    private String fullName;

    @NotBlank(message = "Work email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 180)
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[+]?[0-9 \\-()]{7,18}$", message = "Enter a valid mobile number")
    private String mobileNumber;

    @NotBlank(message = "Company name is required")
    @Size(max = 150)
    private String company;

    @NotBlank(message = "Please select a product")
    @Size(max = 100)
    private String product;

    @Size(max = 500)
    private String customRequirement;

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
}
