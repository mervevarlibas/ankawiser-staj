package com.mwitter.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.mwitter.dto.CreatePostRequest;
import com.mwitter.dto.PostResponse;
import com.mwitter.model.Post;
import com.mwitter.model.Repost;
import com.mwitter.model.User;
import com.mwitter.repository.PostRepository;
import com.mwitter.repository.CommentRepository;
import com.mwitter.repository.RepostRepository;
import com.mwitter.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final int TIMELINE_LIMIT = 50;//listenin yalnızca ilk 50 elemanını alıyor.sayfa yenilendiğinde backendden tekrar oluşturulur

    private final PostRepository postRepository;//gönderileri kaydetmek,bulmak için
    private final UserRepository userRepository;//gönderiyi atan kişinin bilgilerini almak için
    private final RepostRepository repostRepository;//retweet işlemleri için
    private final CommentRepository commentRepository;//yorumları yönetmek için

    public PostResponse createPost(CreatePostRequest request, String userId) {
        User user = getUserById(userId);//userid ile gelen kullanıcıyı bulur
        Post post = new Post();
        post.setContent(request.getContent());//gelen metinleri alır
        post.setCreatedAt(LocalDateTime.now());
        post.setUserId(user.getId());

        Post savedPost = postRepository.save(post);//yeni bir post oluşturup vtabanına kaydeder
        return convertToResponse(savedPost, user.getUsername(), user.getId());//frontend ekrana çizbilsin diye güvenli format
    }

    public List<PostResponse> getAllPosts() {//Veritabanından tarihe göre sıralı tüm postları çeker.
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
        Map<String, User> usersById = getUsersByIdForPosts(posts);
        List<PostResponse> responses = new ArrayList<>();

        for (Post post : posts) {
            User postUser = usersById.get(post.getUserId());
            if (postUser == null) {
                throw new RuntimeException("Post owner not found.");
            }
            responses.add(convertToResponse(post, postUser.getUsername(), null));
        }

        return responses;
    }

    public List<PostResponse> getPostsByUserId(String userId, String currentUserId) {//birinin profiline girdiğimizde çalışır
        User user = getUserById(userId);
        List<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<PostResponse> responses = new ArrayList<>();

        for (Post post : posts) {
            responses.add(convertToResponse(post, user.getUsername(), currentUserId));
        }

        addRepostResponses(responses, repostRepository.findByUserId(userId), currentUserId);
        sortByDisplayDate(responses);
        return responses;
    }

    public void likePost(String postId, String userId) {
        Post post = getPostById(postId);
        getUserById(userId);

        if (post.getLikedUserIds().contains(userId)) {//listede var mı diye bakar yoksa ekler
            throw new RuntimeException("You already liked this post.");
        }

        post.getLikedUserIds().add(userId);
        postRepository.save(post);
    }

    public void unlikePost(String postId, String userId) {
        Post post = getPostById(postId);
        getUserById(userId);

        if (!post.getLikedUserIds().contains(userId)) {
            throw new RuntimeException("You have not liked this post.");
        }

        post.getLikedUserIds().remove(userId);
        postRepository.save(post);
    }

    public void repostPost(String postId, String userId) {
        getPostById(postId);
        getUserById(userId);

        if (repostRepository.findByUserIdAndPostId(userId, postId).isPresent()) {
            throw new RuntimeException("You already reposted this post.");
        }

        Repost repost = new Repost();
        repost.setUserId(userId);
        repost.setPostId(postId);
        repost.setCreatedAt(LocalDateTime.now());
        repostRepository.save(repost);
    }

    public void undoRepost(String postId, String userId) {
        Repost repost = repostRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new RuntimeException("Repost not found."));
        repostRepository.delete(repost);
    }

    public Post getPostById(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found."));
    }

    public void deletePost(String postId, String userId) {
        Post post = getPostById(postId);//postu bulur

        if (!post.getUserId().equals(userId)) {//postu silmek isteyen lişi ile postun sahibi aynı mı
            throw new RuntimeException("You can only delete your own post.");
        }

        commentRepository.deleteByPostId(postId);
        repostRepository.deleteByPostId(postId);
        postRepository.delete(post);
    }

    public List<PostResponse> getTimeline(String userId) {
        User user = getUserById(userId);
        List<String> timelineUserIds = new ArrayList<>(user.getFollowing());//user servisine gidip takip ettiklerimizin idsini listeye ekler
        timelineUserIds.add(user.getId());

        List<Post> posts = postRepository.findByUserIdInOrderByCreatedAtDesc(timelineUserIds);
        Map<String, User> usersById = getUsersByIdForPosts(posts);
        List<PostResponse> responses = new ArrayList<>();

        for (Post post : posts) {
            User postUser = usersById.get(post.getUserId());
            if (postUser == null) {
                throw new RuntimeException("Post owner not found.");
            }
            responses.add(convertToResponse(post, postUser.getUsername(), userId));
        }

        addRepostResponses(responses, repostRepository.findByUserIdIn(timelineUserIds), userId);
        sortByDisplayDate(responses);
        return responses.stream()
                .limit(TIMELINE_LIMIT)
                .toList();
    }

    public PostResponse getPostByIdForResponse(String postId, String currentUserId) {
        Post post = getPostById(postId);
        User postUser = getUserById(post.getUserId());
        return convertToResponse(post, postUser.getUsername(), currentUserId);
    }

    private PostResponse convertToResponse(Post post, String username, String currentUserId) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setCreatedAt(post.getCreatedAt());
        response.setUserId(post.getUserId());
        response.setUsername(username);
        response.setLikeCount(post.getLikedUserIds().size());//beğeni sayısı
        response.setLikedByCurrentUser(
                currentUserId != null && post.getLikedUserIds().contains(currentUserId)//kalp ikonunun boş olup olmadığını frontende göndermek için
        );
        response.setRepostCount((int) repostRepository.countByPostId(post.getId()));
        response.setRepostedByCurrentUser(
                currentUserId != null
                && repostRepository.findByUserIdAndPostId(currentUserId, post.getId()).isPresent()
        );
        return response;
    }

    private void addRepostResponses(
            List<PostResponse> responses,
            List<Repost> reposts,
            String currentUserId) {

        if (reposts.isEmpty()) {
            return;
        }

        Set<String> postIds = new HashSet<>();
        Set<String> userIds = new HashSet<>();
        for (Repost repost : reposts) {
            postIds.add(repost.getPostId());
            userIds.add(repost.getUserId());
        }

        Map<String, Post> postsById = new HashMap<>();
        for (Post post : postRepository.findAllById(postIds)) {
            postsById.put(post.getId(), post);
            userIds.add(post.getUserId());
        }

        Map<String, User> usersById = new HashMap<>();
        for (User user : userRepository.findAllById(userIds)) {
            usersById.put(user.getId(), user);
        }

        for (Repost repost : reposts) {
            Post post = postsById.get(repost.getPostId());
            User repostingUser = usersById.get(repost.getUserId());
            if (post == null || repostingUser == null) {
                continue;
            }

            User postOwner = usersById.get(post.getUserId());
            if (postOwner == null) {
                continue;
            }

            PostResponse response = convertToResponse(post, postOwner.getUsername(), currentUserId);
            response.setRepost(true);
            response.setRepostedByUserId(repostingUser.getId());
            response.setRepostedByUsername(repostingUser.getUsername());
            response.setRepostedAt(repost.getCreatedAt());
            responses.add(response);
        }
    }

    private Map<String, User> getUsersByIdForPosts(List<Post> posts) {
        Set<String> userIds = new HashSet<>();
        for (Post post : posts) {
            userIds.add(post.getUserId());
        }

        Map<String, User> usersById = new HashMap<>();
        for (User user : userRepository.findAllById(userIds)) {//mongodbye sadece bir kere gidip postları yazanları tek seferde alıyoruz
            usersById.put(user.getId(), user);
        }
        return usersById;
    }

    private User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
    }

    private void sortByDisplayDate(List<PostResponse> responses) {
        responses.sort(
                Comparator.comparing(//eğer bu veri bir repost ise sıralamayı o tarihe göre yap,postsa oluşturma tarihine göre
                        (PostResponse response) -> response.isRepost()
                                ? response.getRepostedAt()
                                : response.getCreatedAt(),
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed()//en yeni en üstte olacak şekilde ters çevir
        );
    }
}
