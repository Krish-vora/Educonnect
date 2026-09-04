package com.project.educonnect.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.educonnect.dto.ApplicantDTO;
import com.project.educonnect.dto.DashboardStatsDto;
import com.project.educonnect.model.Application;
import com.project.educonnect.model.OpportunityPost;
import com.project.educonnect.model.ProfessionalProfile;
import com.project.educonnect.model.StudentProfile;
import com.project.educonnect.repository.ApplicationRepository;
import com.project.educonnect.repository.PostRepository;
import com.project.educonnect.repository.ProfessionalProfileRepository;
import com.project.educonnect.repository.StudentProfileRepository;
import com.project.educonnect.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    @Autowired
    private ApplicationRepository repository;
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AppNotificationService appNotificationService;

    @Autowired
    private ProfessionalProfileRepository professionalProfileRepository;

    public boolean apply(String studentId, String postId) {

        boolean alreadyApplied = repository.findByStudentIdAndPostId(studentId, postId)
                .isPresent();

        if (alreadyApplied) {
            return false;
        }

        Application app = new Application();

        app.setStudentId(studentId);
        app.setPostId(postId);
        app.setStatus("APPLIED");
        app.setAppliedAt(LocalDate.now());

        repository.save(app);
        // Notify the organization
        appNotificationService.createApplicationNotification(studentId, postId, "APPLIED");
        Optional<OpportunityPost> optionalPost = postRepository.findById(postId);

        if (optionalPost.isPresent()) {

            OpportunityPost post = optionalPost.get();

            post.setTotalApplicants(
                    post.getTotalApplicants() + 1);

            postRepository.save(post);
        }

        return true;
    }

    public List<String> getAppliedPostIds(String studentId) {

        return repository.findByStudentId(studentId)
                .stream()
                .map(Application::getPostId)
                .collect(Collectors.toList());
    }

    public List<Application> getStudentApplications(String studentId) {

        return repository.findByStudentId(studentId);

    }

    public List<ApplicantDTO> getApplicantsForOrg(String orgUserId) {

        List<OpportunityPost> orgPosts = postRepository.findByOrganizationId(orgUserId);

        if (orgPosts.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> postIds = orgPosts.stream()
                .map(OpportunityPost::getId)
                .collect(Collectors.toList());

        List<Application> applications = repository.findByPostIdIn(postIds);

        if (applications.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, OpportunityPost> postMap = orgPosts.stream()
                .collect(Collectors.toMap(OpportunityPost::getId, p -> p));

        List<ApplicantDTO> result = new ArrayList<>();

        for (Application app : applications) {

            ApplicantDTO dto = new ApplicantDTO();

            dto.setApplicationId(app.getId());
            dto.setStatus(app.getStatus());
            dto.setAppliedAt(app.getAppliedAt() != null ? app.getAppliedAt().toString() : "N/A");

   
            OpportunityPost post = postMap.get(app.getPostId());
            if (post != null) {
                dto.setPostId(post.getId());
                dto.setPostTitle(post.getTitle());
                dto.setPostType(post.getType());
            }

          
            String userId = app.getStudentId();
            dto.setStudentId(userId);

            userRepository.findByUserId(userId).ifPresent(user -> {
                dto.setStudentName(user.getFullName());
                dto.setStudentEmail(user.getEmail());
            });

           
            String skills = null;
            String education = null;
            String location = null;
            String bio = null;
            String university = null;
            String company = null;
            String experience = null;
            String applicantType = "Unknown"; // Default

            StudentProfile student = studentProfileRepository.findByUserId(userId);
            ProfessionalProfile professional = professionalProfileRepository.findByUserId(userId);

            if (student != null) {
                applicantType = "Student";
                skills = student.getStudentSkills();
                education = student.getStudentEducation();
                university = student.getStudentUniversity();
                location = student.getLocation();
                bio = student.getBio();
            } else if (professional != null) {
                applicantType = "Professional";
                skills = professional.getSkills();
                education = professional.getEducation();
                location = professional.getLocation();
                bio = professional.getBio();
                company = professional.getCompany();
                experience = professional.getExperience();
            }

            // =========================
            // DTO ASSIGNMENT (FINAL)
            // =========================
            dto.setApplicantType(applicantType);
            dto.setSkills(skills != null && !skills.isEmpty() ? skills : "N/A");
            dto.setEducation(education != null && !education.isEmpty() ? education : "N/A");
            dto.setLocation(location != null && !location.isEmpty() ? location : "N/A");
            dto.setBio(bio != null && !bio.isEmpty() ? bio : "N/A");

            // Specifics mapping correctly now
            dto.setUniversity(university != null && !university.isEmpty() ? university : "N/A");
            dto.setCompany(company != null && !company.isEmpty() ? company : "N/A");
            dto.setExperience(experience != null && !experience.isEmpty() ? experience : "N/A");

            result.add(dto);
        }

        return result;
    }

    public List<Application> getApplicationsByPostId(
            String postId) {

        return applicationRepository.findByPostId(postId);
    }

    public Application save(Application application) {

        return repository.save(application);
    }

    public Application updateStatus(
            String applicationId,
            String status) {

        Application app = applicationRepository
                .findById(applicationId)
                .orElse(null);

        if (app == null) {
            return null;
        }

        app.setStatus(status);

        return applicationRepository.save(app);
    }

    public void cancelApplication(String applicationId) {

        Application application = applicationRepository
                .findById(applicationId)
                .orElse(null);

        if (application == null) {
            return;
        }


        String postId = application.getPostId();


        OpportunityPost post = postRepository
                .findById(postId)
                .orElse(null);


        if (post != null) {

            int currentCount = post.getTotalApplicants();

            if (currentCount > 0) {

                post.setTotalApplicants(
                        currentCount - 1);
            }

            postRepository.save(post);
        }

        
        appNotificationService.createApplicationNotification(
                application.getStudentId(), application.getPostId(), "CANCELLED");
        applicationRepository.deleteById(
                applicationId);
    }


 

    


}