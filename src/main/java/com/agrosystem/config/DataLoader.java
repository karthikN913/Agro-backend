package com.agrosystem.config;

import com.agrosystem.model.CommunityPost;
import com.agrosystem.model.GovernmentScheme;
import com.agrosystem.model.User;
import com.agrosystem.repository.CommunityPostRepository;
import com.agrosystem.repository.GovernmentSchemeRepository;
import com.agrosystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DataLoader — seeds essential reference data on first boot.
 * Uses count-checks so it is fully idempotent; safe to redeploy.
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private GovernmentSchemeRepository schemeRepository;

    @Autowired
    private CommunityPostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        seedSchemes();
        seedCommunityPosts();
    }

    // ─── Government Schemes ────────────────────────────────────────────────────

    private void seedSchemes() {
        if (schemeRepository.count() > 0) return; // already seeded

        schemeRepository.save(scheme(
            "PM-KISAN (Pradhan Mantri Kisan Samman Nidhi)",
            "Direct income support of ₹6,000 per year (3 instalments of ₹2,000 each) deposited " +
            "directly into the bank accounts of landholding farmers across India.",
            "Small & marginal farmers owning up to 2 hectares of agricultural land.",
            "https://pmkisan.gov.in/"
        ));

        schemeRepository.save(scheme(
            "PM Fasal Bima Yojana — Crop Insurance",
            "Comprehensive crop insurance scheme covering losses from natural calamities, pests, " +
            "and diseases. Premium rates are as low as 1.5% for Rabi and 2% for Kharif crops.",
            "All farmers growing notified crops in notified areas. Both loanee and non-loanee farmers.",
            "https://pmfby.gov.in/"
        ));

        schemeRepository.save(scheme(
            "Kisan Credit Card (KCC)",
            "Provides short-term credit for agricultural needs at concessional interest (7% p.a., " +
            "effective 4% with timely repayment incentive). Covers crop cultivation, post-harvest, " +
            "and allied activity expenses.",
            "Farmers, sharecroppers, tenant farmers, oral lessees, and SHGs engaged in agriculture.",
            "https://www.nabard.org/content.aspx?id=593"
        ));

        schemeRepository.save(scheme(
            "PM Krishi Sinchai Yojana — Irrigation Support",
            "Subsidies for drip and sprinkler irrigation installation, aimed at expanding irrigated " +
            "area and improving on-farm water-use efficiency under 'More Crop Per Drop'.",
            "Farmers in notified districts; priority given to small & marginal farmers.",
            "https://pmksy.gov.in/"
        ));

        schemeRepository.save(scheme(
            "Paramparagat Krishi Vikas Yojana — Organic Farming",
            "Financial assistance of ₹50,000 per hectare over 3 years for conversion to organic " +
            "farming, certification costs, and value-chain development.",
            "Farmers willing to adopt organic practices as part of a cluster of at least 20 farmers.",
            "https://pgsindia-ncof.gov.in/"
        ));

        schemeRepository.save(scheme(
            "Sub-Mission on Agricultural Mechanization (SMAM)",
            "Subsidies of up to 50% on purchase of farm machinery — tractors, harvesters, seed " +
            "drills, rotavators and more — to reduce labour costs and improve productivity.",
            "Small & marginal farmers; SC/ST farmers and women farmers receive higher subsidies.",
            "https://agrimachinery.nic.in/"
        ));

        schemeRepository.save(scheme(
            "Soil Health Card Scheme",
            "Free soil testing and personalised Soil Health Cards issued to every farmer every " +
            "2 years, with recommended dosage of nutrients to optimise yield and reduce input costs.",
            "All farmers across India — no income or land-size restriction.",
            "https://soilhealth.dac.gov.in/"
        ));

        schemeRepository.save(scheme(
            "National Agriculture Market (eNAM)",
            "Online trading platform connecting farmers directly to buyers across registered mandis " +
            "in India, eliminating middlemen and ensuring transparent, fair price discovery.",
            "All registered farmers with produce to sell at eNAM-connected mandis.",
            "https://www.enam.gov.in/"
        ));

        System.out.println("[DataLoader] ✅ Seeded 8 Government Schemes.");
    }

    private GovernmentScheme scheme(String title, String desc, String eligibility, String url) {
        GovernmentScheme s = new GovernmentScheme();
        s.setTitle(title);
        s.setDescription(desc);
        s.setEligibility(eligibility);
        s.setUrl(url);
        s.setActive(true);
        return s;
    }

    // ─── Community Posts ───────────────────────────────────────────────────────

    private void seedCommunityPosts() {
        if (postRepository.count() > 0) return; // already seeded

        // We need at least one system user to attach posts to.
        // Find or create a system/bot user.
        User system = userRepository.findByEmail("system@agrolinken.in")
            .orElseGet(() -> {
                User u = new User();
                u.setName("Agro Linken Team");
                u.setEmail("system@agrolinken.in");
                u.setRole(com.agrosystem.model.User.Role.FARMER);
                u.setLocation("India");
                return userRepository.save(u);
            });

        CommunityPost p1 = new CommunityPost();
        p1.setAuthor(system);
        p1.setTitle("Welcome to the Agro Linken Community! 🌾");
        p1.setContent("This is your space to share farming tips, ask questions, celebrate harvests, " +
            "and support each other. Whether you grow vegetables, grains, or spices — " +
            "every farmer's experience is valuable here. Start by introducing yourself below! 👇");
        p1.setLikes(12);
        postRepository.save(p1);

        CommunityPost p2 = new CommunityPost();
        p2.setAuthor(system);
        p2.setTitle("Tip: Best practices for drip irrigation in summer 💧");
        p2.setContent("With summer temperatures rising, here are a few quick tips:\n" +
            "1. Water early morning (5–7 AM) to minimise evaporation.\n" +
            "2. Use mulching around root zones to retain soil moisture.\n" +
            "3. Check drip emitters weekly for clogging — flush the lines monthly.\n" +
            "4. Consider fertigation (adding nutrients through drip lines) to save labour.\n" +
            "Share what works in your region! 🌱");
        p2.setLikes(34);
        postRepository.save(p2);

        System.out.println("[DataLoader] ✅ Seeded 2 Community Posts.");
    }
}
