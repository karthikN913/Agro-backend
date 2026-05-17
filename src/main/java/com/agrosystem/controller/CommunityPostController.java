package com.agrosystem.controller;

import com.agrosystem.model.CommunityPost;
import com.agrosystem.repository.CommunityPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@CrossOrigin(origins = "*")
public class CommunityPostController {

    @Autowired
    private CommunityPostRepository communityPostRepository;

    /** GET /api/community — newest posts first */
    @GetMapping
    public List<CommunityPost> getAllPosts() {
        return communityPostRepository.findAllByOrderByCreatedAtDesc();
    }

    /** POST /api/community — create a new post */
    @PostMapping
    public ResponseEntity<CommunityPost> createPost(@RequestBody CommunityPost post) {
        if (post.getAuthor() == null || post.getAuthor().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
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
