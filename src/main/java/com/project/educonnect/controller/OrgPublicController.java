package com.project.educonnect.controller;

import com.project.educonnect.model.OpportunityPost;
import com.project.educonnect.model.OrganizationProfile;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.OrganizationProfileRepository;
import com.project.educonnect.repository.PostRepository;
import com.project.educonnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/org")
public class OrgPublicController {

    @Autowired
    private OrganizationProfileRepository orgProfileRepo;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile/{orgId}")
    public Map<String, Object> getOrgProfile(@PathVariable String orgId) {

        Map<String, Object> result = new LinkedHashMap<>();

        OrganizationProfile profile = orgProfileRepo.findByUserId(orgId);
        if (profile == null) {
            result.put("error", "Organization not found");
            return result;
        }

        User user = userRepository.findByUserId(orgId).orElse(null);

        result.put("userId",           profile.getUserId());
        result.put("organizationName", profile.getOrganizationName());
        result.put("industry",         profile.getIndustry());
        result.put("bio",              profile.getBio());
        result.put("location",         profile.getLocation());
        result.put("website",          profile.getWebsite());
        result.put("totalEmployees",   profile.getTotalEmployees());
        result.put("email",            user != null ? user.getEmail() : null);

        return result;
    }

    @GetMapping("/posts/{orgId}")
    public List<Map<String, Object>> getOrgPosts(@PathVariable String orgId) {

        List<OpportunityPost> posts = postRepository.findByOrganizationId(orgId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (OpportunityPost p : posts) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",              p.getId());
            map.put("title",           p.getTitle());
            map.put("type",            p.getType());
            map.put("status",          p.getStatus());
            map.put("location",        p.getLocation());
            map.put("deadline",        p.getDeadline());
            map.put("mode",            p.getMode());
            map.put("skillsRequired",  p.getSkillsRequired());
            map.put("totalApplicants", p.getTotalApplicants());
            map.put("description",     p.getDescription());
            map.put("salaryRange",     p.getSalaryRange());
            map.put("stipend",         p.getStipend());
            map.put("prizePool",       p.getPrizePool());
            result.add(map);
        }
        return result;
    }
}