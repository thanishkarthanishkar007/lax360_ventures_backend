package com.lax360.backend.config;

import com.lax360.backend.model.*;
import com.lax360.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepo;
    private final IndustryRepository industryRepo;
    private final TeamMemberRepository teamRepo;
    private final CustomerRepository customerRepo;
    private final WebsiteContentRepository contentRepo;

    public DataInitializer(
            ProductRepository productRepo,
            IndustryRepository industryRepo,
            TeamMemberRepository teamRepo,
            CustomerRepository customerRepo,
            WebsiteContentRepository contentRepo) {
        this.productRepo = productRepo;
        this.industryRepo = industryRepo;
        this.teamRepo = teamRepo;
        this.customerRepo = customerRepo;
        this.contentRepo = contentRepo;
    }

    @Override
    public void run(String... args) {
        if (productRepo.count() == 0) {
            productRepo.saveAll(List.of(
                new Product("Restaurants – 3D Animated Web", "3D Animated Web", "Haute cuisine indienne & royal dining with interactive 3D elements and reservations.", List.of("Interactive 3D table reservations", "Dynamic culinary menu showcase", "Chef storytelling & ambient audio")),
                new Product("Jewellery – Animated Web", "Animated Web", "Haute joaillerie and luxury gemstone showcase with real-time reflections and cinematic transitions.", List.of("High-precision gem showcases", "Bespoke consultation booking", "Cinematic jewelry catalog")),
                new Product("Gym – Cursor Interactive Web", "Cursor Interactive Web", "Premium athletic club experience with cursor physics, membership tiers, and trainer schedules.", List.of("Interactive cursor reactive canvas", "Class schedule & live bookings", "Elite trainer profile cards")),
                new Product("Textiles – Scrolling Web", "Scrolling Web", "Haute weaves, silks, and bespoke couture fabric gallery with smooth scroll animations.", List.of("Bespoke fabric visualizer", "Artisan weaver stories", "Silk sample order pipeline"))
            ));
        }

        if (industryRepo.count() == 0) {
            industryRepo.saveAll(List.of(
                new Industry("Healthcare", "Patient ops, provider scheduling, and care coordination at scale.", "HeartPulse"),
                new Industry("Education", "Admissions, LMS integrations, and student cohort tracking.", "GraduationCap"),
                new Industry("Finance", "Ledgers, reconciliation, and reporting your auditors will thank you for.", "Wallet"),
                new Industry("Banking", "Secure, compliant workflows for accounts, lending, and transactions.", "Landmark"),
                new Industry("Retail", "Inventory, POS, and omnichannel customer journeys in one view.", "ShoppingBag"),
                new Industry("Manufacturing", "Production tracking, supply chain visibility, and quality control.", "Factory"),
                new Industry("Logistics", "Fleet visibility, route automation, and real-time delivery tracking.", "Truck"),
                new Industry("Real Estate", "Deal pipelines, listings, and tenant relationship management.", "Building2"),
                new Industry("Hospitality", "Bookings, guest experience, and staff operations, unified.", "Hotel"),
                new Industry("Construction", "Project timelines, site resources, and contractor coordination.", "HardHat"),
                new Industry("Insurance", "Claims processing, underwriting workflows, and policy management.", "ShieldCheck"),
                new Industry("Government", "Citizen services and public-sector workflows built for compliance.", "Scale"),
                new Industry("Startups", "Move fast with tooling that scales from first hire to Series B.", "Rocket"),
                new Industry("E-commerce", "Orders, fulfillment, and customer journeys, end to end.", "ShoppingCart")
            ));
        }

        if (teamRepo.count() == 0) {
            teamRepo.saveAll(List.of(
                new TeamMember("Arjun Kapoor", "Founder & CEO", "AK"),
                new TeamMember("Sana Rahman", "Head of Product", "SR"),
                new TeamMember("Vikram Nair", "Engineering Lead", "VN"),
                new TeamMember("Meera Pillai", "Head of Design", "MP"),
                new TeamMember("Devesh Kumar", "VP Sales", "DK"),
                new TeamMember("Riya Thomas", "Customer Success Lead", "RT"),
                new TeamMember("Aditya Joshi", "Head of Marketing", "AJ"),
                new TeamMember("Neha Singh", "Finance Lead", "NS")
            ));
        }

        if (customerRepo.count() == 0) {
            customerRepo.saveAll(List.of(
                new Customer("Apollo Clinic", "Healthcare", "CRM cut our patient follow-up no-shows by half within the first quarter.", "Head of Operations"),
                new Customer("XYZ Hospital", "Healthcare", "Hospital Management automated our insurance billing reconciliation — what took days now takes hours.", "Finance Director"),
                new Customer("ABC College", "Education", "ERP automated our admissions workflow end-to-end, from application to enrollment.", "Admissions Lead"),
                new Customer("DEF School", "Education", "Clinic Management helped us track patient engagement across every communication channel.", "Communications Manager"),
                new Customer("GHI Clinic", "Healthcare", "Onboarding took less than a week, and our front-desk team picked it up immediately.", "Practice Manager")
            ));
        }

        if (contentRepo.findByKey("admin_passcode").isEmpty()) {
            contentRepo.save(new WebsiteContent("admin_passcode", "Admin Passcode", "lax360@1234"));
        }

        if (contentRepo.count() == 0) {
            contentRepo.save(new WebsiteContent("hero_headline", "Hero Headline", "Transform Your Business with Smart Digital Solutions"));
            contentRepo.save(new WebsiteContent("hero_subhead", "Hero Subhead", "LAX360 Ventures builds SaaS products that help teams automate operations, understand customers, and grow revenue."));
        }
    }
}
