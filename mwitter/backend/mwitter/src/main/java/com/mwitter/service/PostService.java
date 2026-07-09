package com.mwitter.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mwitter.dto.CreatePostRequest;
import com.mwitter.dto.PostResponse;
import com.mwitter.model.Post;
import com.mwitter.model.User;
import com.mwitter.repository.PostRepository;
import com.mwitter.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor//final olan repository’ler için constructor’ı Lombok otomatik oluşturur.
public class PostService {

    private final PostRepository postRepository; //bu tweet i kaydetmek için postRepository i çağırıyoruz
    private final UserRepository userRepository; //bu tweet i kim atıyor onu bilmek için userRepository i çağırıyoruz

    public PostResponse createPost(CreatePostRequest request) {//frontendden gelen tweet oluşturma isteğini alır
        Optional<User> user = userRepository.findById(request.getUserId());//gösterilen userId ye sahip kullanıcıyı bulur
        if (user.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        Post post = new Post();
        post.setContent(request.getContent());//tweetin içeriğini post nesnesine atıyoruz
        post.setCreatedAt(LocalDateTime.now());//tweetin atıldığı zamanı post nesnesine atıyoruz
        post.setUser(user.get());//tweeti atan kullanıcıyı post nesnesine atıyoruz

        Post savedPost = postRepository.save(post);//Post MongoDB’ye kaydediliyor. Kaydedildikten sonra MongoDB buna id verir. O yüzden sonucu savedPost içine alıyorsun.

        PostResponse response = new PostResponse();//kullanıcıya sadece gerekli alanları göndermek için PostResponse DTO sınıfını kullanıyoruz

        response.setId(savedPost.getId());//post>postresponse dönüşümü
        response.setContent(savedPost.getContent());
        response.setCreatedAt(savedPost.getCreatedAt());
        response.setUsername(savedPost.getUser().getUsername());
        return response;
    }//büyük post nesnesinden sadece gerekli bilgileri cevaba koyuyorsun.

    public List<PostResponse> getAllPosts() {//birden fazla tweeti listelemek için getAllPosts metodunu oluşturuyoruz
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();//mongoDB post koleksiyonundaki tüm tweetleri atıldığı zaman göre sıralayıp buluyoruz
        List<PostResponse> responses = new ArrayList<>();//tweetleri listelemek için boş bir liste oluşturuyoruz
        for (Post post : posts) {//tüm postları tek tek dolaşıyoruz
            PostResponse response = new PostResponse();//her post için yeni bir PostResponse nesnesi oluşturuyoruz
            response.setId(post.getId());//yeni response dönüşümü
            response.setContent(post.getContent());
            response.setCreatedAt(post.getCreatedAt());
            response.setUsername(post.getUser().getUsername());
            responses.add(response);//hazırlanan response nesnesini listeye ekliyoruz
        }
        return responses;

    }

    public List<PostResponse> getPostsByUserId(String userId) {//belirli kullanıcının attığı tweetleri listelemek için getPostsByUserId metodunu oluşturuyoruz

        List<Post> posts = postRepository.findByUser_IdOrderByCreatedAtDesc(userId);

        List<PostResponse> responses = new ArrayList<>();

        for (Post post : posts) {

            PostResponse response = new PostResponse();

            response.setId(post.getId());
            response.setContent(post.getContent());
            response.setCreatedAt(post.getCreatedAt());
            response.setUsername(post.getUser().getUsername());

            responses.add(response);
        }

        return responses;
    }
}
