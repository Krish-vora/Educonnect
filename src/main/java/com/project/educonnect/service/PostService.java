package com.project.educonnect.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.project.educonnect.model.OpportunityPost;
import com.project.educonnect.model.OrganizationProfile;
import com.project.educonnect.model.ProfessionalProfile;
import com.project.educonnect.model.StudentProfile;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.ApplicationRepository;
import com.project.educonnect.repository.PostRepository;
import com.project.educonnect.repository.UserRepository;

@Service
public class PostService {
    @Autowired
    private PostRepository repository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private OrganizationService organizationService;

    private static final double SKILL_WEIGHT = 50.0;
    private static final double LOCATION_WEIGHT = 20.0;
    private static final double INTEREST_WEIGHT = 20.0;
    private static final double TYPE_WEIGHT = 10.0;

    public OpportunityPost createPost(OpportunityPost post, String email) {

        Optional<User> user = userRepository.findByEmail(email);
        post.setOrganizationId(user.get().getUserId());
        post.setCreatedAt(LocalDate.now());
        post.setStatus("ACTIVE");

        OpportunityPost saved = repository.save(post);


        OrganizationProfile org = organizationService.getOrgById(user.get().getUserId());
        String orgName = (org != null) ? org.getOrganizationName() : "Unknown";
        notificationService.createNotification(saved, orgName);

        return saved;
    }

    public List<OpportunityPost> getAllPosts() {

        List<OpportunityPost> posts = repository.findAll();

        posts.forEach(post -> {

            long totalApplicants = applicationRepository.countByPostId(post.getId());

            post.setTotalApplicants((int) totalApplicants);

        });

        return posts;
    }

    public OpportunityPost getPost(String pid) {
        return repository.findById(pid).orElse(null);
    }

    public List<OpportunityPost> getPostsByOrg(String orgId) {
        return repository.findByOrganizationId(orgId);
    }

    public void deletePost(String postId) {
        repository.deleteById(postId);
    }

    public List<OpportunityPost> getPostsByOrgAndType(String orgId, String type) {
        return repository.findByOrganizationIdAndType(orgId, type);
    }

    public List<OpportunityPost> getPosts(String orgId, String type) {

        if (type == null || type.equalsIgnoreCase("all")) {
            return repository.findByOrganizationId(orgId);
        }

        return repository.findByOrganizationIdAndType(orgId, type);
    }

    public OpportunityPost getPostById(String id) {

        return repository.findById(id).orElse(null);

    }

    public Page<OpportunityPost> getPaginatedPosts(
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC,
                        "createdAt"));

        return repository.findAll(pageable);
    }

    public List<OpportunityPost> getPosts(
            String organizationId) {
        return repository.findByOrganizationId(
                organizationId);
    }

    public OpportunityPost save(
            OpportunityPost post) {

        return repository.save(post);
    }

    public List<OpportunityPost> getRecentPosts(String organizationId) {

        return repository
                .findByOrganizationIdOrderByCreatedAtDesc(
                        organizationId);
    }

    public void updatePost(
            OpportunityPost updatedPost) {

        OpportunityPost existingPost = repository
                .findById(updatedPost.getId())
                .orElse(null);

        if (existingPost != null) {

            existingPost.setTitle(
                    updatedPost.getTitle());

            existingPost.setLocation(
                    updatedPost.getLocation());

            existingPost.setType(
                    updatedPost.getType());

            existingPost.setStatus(
                    updatedPost.getStatus());

            existingPost.setDeadline(
                    updatedPost.getDeadline());

            existingPost.setSalaryRange(
                    updatedPost.getSalaryRange());

            existingPost.setSkillsRequired(
                    updatedPost.getSkillsRequired());

            existingPost.setDescription(
                    updatedPost.getDescription());

            existingPost.setMode(
                    updatedPost.getMode());

            repository.save(existingPost);
        }
    }

    public List<OpportunityPost> getRankedPostsForStudent(StudentProfile student) {

        List<OpportunityPost> activePosts = repository.findAll();

        activePosts.forEach(post -> {
            long count = applicationRepository.countByPostId(post.getId());
            post.setTotalApplicants((int) count);
            post.setMatchScore(computeScore(post, student));
        });

        activePosts.sort(Comparator.comparingDouble(
                (OpportunityPost p) -> computeScore(p, student)).reversed());

        return activePosts;
    }

    public double computeScore(OpportunityPost post, StudentProfile student) {

        double skillScore = scoreSkills(post, student);
        double locationScore = scoreLocation(post, student);
        double interestScore = scoreInterests(post, student);
        double typeScore = scoreType(post);

        double total = (skillScore * SKILL_WEIGHT / 100.0)
                + (locationScore * LOCATION_WEIGHT / 100.0)
                + (interestScore * INTEREST_WEIGHT / 100.0)
                + (typeScore * TYPE_WEIGHT / 100.0);

        return Math.round(total * 10.0) / 10.0;

    }

    private double scoreSkills(OpportunityPost post, StudentProfile student) {

        List<String> required = toList(post.getSkillsRequired());
        List<String> has = splitString(student.getStudentSkills());

        if (required.isEmpty())
            return 100.0;
        if (has.isEmpty())
            return 0.0;

        long intersection = has.stream().filter(required::contains).count();
        long union = required.size() + has.size() - intersection;

        return union == 0 ? 0.0 : Math.round((intersection * 100.0) / union * 10.0) / 10.0;
    }

    private double scoreLocation(OpportunityPost post, StudentProfile student) {

        String mode = post.getMode();
        if (mode != null && (mode.equalsIgnoreCase("REMOTE")
                || mode.equalsIgnoreCase("ONLINE"))) {
            return 100.0;
        }

        String postLoc = post.getLocation();
        String studentLoc = student.getLocation();

        if (postLoc == null || postLoc.isBlank()
                || studentLoc == null || studentLoc.isBlank()) {
            return 50.0; // Unknown → neutral
        }

        String pl = postLoc.trim().toLowerCase();
        String sl = studentLoc.trim().toLowerCase();

        if (pl.equals(sl))
            return 100.0;
        if (pl.contains(sl) || sl.contains(pl))
            return 70.0;

        return 0.0;
    }

    private double scoreInterests(OpportunityPost post, StudentProfile student) {

        List<String> interests = splitString(student.getInterests());

        if (interests.isEmpty())
            return 50.0; 

        String category = post.getCategory() != null
                ? post.getCategory().trim().toLowerCase()
                : "";
        if (!category.isEmpty() && interests.contains(category))
            return 100.0;

        // 2. Tag overlap
        List<String> tags = toList(post.getTags());
        if (!tags.isEmpty()) {
            long tagHits = tags.stream().filter(interests::contains).count();
            if (tagHits > 0)
                return Math.min(100.0, tagHits * 35.0);
        }

        String text = (nvl(post.getTitle()) + " " + nvl(post.getDescription())).toLowerCase();
        long keywordHits = interests.stream().filter(text::contains).count();
        if (keywordHits > 0)
            return Math.min(80.0, keywordHits * 25.0);

        return 0.0;
    }

    private double scoreType(OpportunityPost post) {
        if (post.getType() == null)
            return 40.0;
        return switch (post.getType().toUpperCase()) {
            case "INTERNSHIP" -> 100.0;
            case "HACKATHON" -> 90.0;
            case "CONTEST" -> 80.0;
            case "EVENT" -> 70.0;
            case "JOB" -> 50.0;
            default -> 40.0;
        };
    }

    private List<String> toList(List<String> raw) {
        if (raw == null)
            return new ArrayList<>();
        return raw.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.trim().toLowerCase())
                .collect(Collectors.toList());
    }

   
    private List<String> splitString(String raw) {
        if (raw == null || raw.isBlank())
            return new ArrayList<>();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }

    public List<OpportunityPost> getRankedPostsForProfessional(ProfessionalProfile professional) {
        List<OpportunityPost> activePosts = repository.findAll();

        activePosts.forEach(post -> {
            long count = applicationRepository.countByPostId(post.getId());
            post.setTotalApplicants((int) count);
            post.setMatchScore(computeScoreForProfessional(post, professional));
        });

        activePosts.sort(Comparator.comparingDouble(
                (OpportunityPost p) -> computeScoreForProfessional(p, professional)).reversed());

        return activePosts;
    }

    public double computeScoreForProfessional(OpportunityPost post, ProfessionalProfile professional) {
        double skillScore = scoreSkillsForProfessional(post, professional);
        double locationScore = scoreLocationForProfessional(post, professional);
        double interestScore = 50.0;
        double typeScore = scoreTypeForProfessional(post);

        double total = (skillScore * SKILL_WEIGHT / 100.0)
                + (locationScore * LOCATION_WEIGHT / 100.0)
                + (interestScore * INTEREST_WEIGHT / 100.0)
                + (typeScore * TYPE_WEIGHT / 100.0);

        return Math.round(total * 10.0) / 10.0;
    }

    private double scoreSkillsForProfessional(OpportunityPost post, ProfessionalProfile professional) {
        List<String> required = toList(post.getSkillsRequired());
        List<String> has = splitString(professional.getSkills());

        if (required.isEmpty())
            return 100.0;
        if (has.isEmpty())
            return 0.0;

        long intersection = has.stream().filter(required::contains).count();
        long union = required.size() + has.size() - intersection;

        return union == 0 ? 0.0 : Math.round((intersection * 100.0) / union * 10.0) / 10.0;
    }

    private double scoreLocationForProfessional(OpportunityPost post, ProfessionalProfile professional) {
        String mode = post.getMode();
        if (mode != null && (mode.equalsIgnoreCase("REMOTE")
                || mode.equalsIgnoreCase("ONLINE"))) {
            return 100.0;
        }

        String postLoc = post.getLocation();
        String profLoc = professional.getLocation();

        if (postLoc == null || postLoc.isBlank()
                || profLoc == null || profLoc.isBlank()) {
            return 50.0;
        }

        String pl = postLoc.trim().toLowerCase();
        String sl = profLoc.trim().toLowerCase();

        if (pl.equals(sl))
            return 100.0;
        if (pl.contains(sl) || sl.contains(pl))
            return 70.0;

        return 0.0;
    }

    private double scoreTypeForProfessional(OpportunityPost post) {
        if (post.getType() == null)
            return 40.0;
        return switch (post.getType().toUpperCase()) {
            case "JOB" -> 100.0;
            case "EVENT" -> 80.0;
            case "HACKATHON" -> 70.0;
            case "CONTEST" -> 60.0;
            case "INTERNSHIP" -> 50.0;
            default -> 40.0;
        };
    }
}
