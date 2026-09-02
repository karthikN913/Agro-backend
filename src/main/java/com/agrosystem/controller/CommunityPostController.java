package com.agrosystem.controller;

import com.agrosystem.model.CommunityPost;
import com.agrosystem.model.User;
import com.agrosystem.repository.CommunityPostRepository;
import com.agrosystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/community")
public class CommunityPostController {

    @Autowired
    private CommunityPostRepository communityPostRepository;

    @Autowired
    private UserRepository userRepository;

    /** GET /api/community — newest posts first */
    @GetMapping
    public List<CommunityPost> getAllPosts() {
        return communityPostRepository.findAllByOrderByCreatedAtDesc();
    }

    /** POST /api/community — create a new post */
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody CommunityPost post) {
        if (post.getAuthor() == null || post.getAuthor().getId() == null) {
            return ResponseEntity.badRequest().body("Author ID is required");
        }
        Optional<User> authorOpt = userRepository.findById(post.getAuthor().getId());
        if (authorOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Author not found");
        }
        post.setAuthor(authorOpt.get());
        CommunityPost saved = communityPostRepository.save(post);
        return ResponseEntity.ok(saved);
    }

    /** POST /api/community/{id}/like — increment like count by 1 */
    @PostMapping("/{id}/like")
    public ResponseEntity<CommunityPost> likePost(@PathVariable Long id) {
        return communityPostRepository.findById(id)
            .map(post -> {
                post.setLikes(post.getLikes() + 1);
                return ResponseEntity.ok(communityPostRepository.save(post));
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
