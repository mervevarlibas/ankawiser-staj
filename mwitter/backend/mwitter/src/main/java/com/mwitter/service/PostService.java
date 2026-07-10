package com.mwitter.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map; 
import java.util.Set;

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

    public PostResponse createPost(CreatePostRequest request, String userId) {//frontendden gelen tweet oluşturma isteğini alır
        User user= getUserById(userId);//posta kullanıcı nesnesi değil user id koyuyoruz
        Post post = new Post();
        post.setContent(request.getContent());//tweetin içeriğini post nesnesine atıyoruz
        post.setCreatedAt(LocalDateTime.now());//tweetin atıldığı zamanı post nesnesine atıyoruz
        post.setUserId(user.getId());//tweeti atan kullanıcıyı post nesnesine atıyoruz

        Post savedPost = postRepository.save(post);//Post MongoDB’ye kaydediliyor. Kaydedildikten sonra MongoDB buna id verir. O yüzden sonucu savedPost içine alıyorsun.

        return convertToResponse(savedPost, user.getUsername());
    }

    public List<PostResponse> getAllPosts() {//birden fazla tweeti listelemek için getAllPosts metodunu oluşturuyoruz
       List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();//MongoDB deki tüm postları createdAt e göre azalan sırada getiriyor
        Set<String> userIds = new HashSet<>();//birden fazla postun sahibini bulmak için userId leri bir set içine alıyoruz. Set, aynı değeri birden fazla kez eklemeye izin vermez.

    for (Post post : posts) {//her postun userId sini alıyoruz ve set içine ekliyoruz
        userIds.add(post.getUserId());
    }

    List<User> users = userRepository.findAllById(userIds);//MongoDB deki tüm userId leri kullanarak kullanıcıları getiriyoruz

    Map<String, User> usersById = new HashMap<>();//kullanıcıları id ile kolay bulmak için map oluşturuyoruz.her postun sahibini bulmak için kullanıcıları bir map içine alıyoruz. Map, key-value çiftlerini saklar. Burada key userId, value ise User nesnesi olacak.

    for (User user : users) {//her kullanıcıyı map içine ekliyoruz. userId yi key> User nesnesini value olarak ekliyoruz.
        usersById.put(user.getId(), user);//ilk anahtar sonra değer
    }

    List<PostResponse> responses = new ArrayList<>();

    for (Post post : posts) {

        User postUser = usersById.get(post.getUserId());// postun sahibini map den alıyoruz. userId yi key olarak kullanıyoruz.

        if (postUser == null) {//postta userid var fakat o kullanıcı silinmişse null sonucunu anlamlı hata olarak veriyoruz
            throw new RuntimeException("Post owner not found.");
        }

        responses.add(//post ve kullanıcı adını postresponse nesnesine çevirip listeye ekliyoruz
                convertToResponse(post, postUser.getUsername())
        );
    }


        return responses;

    }

    public List<PostResponse> getPostsByUserId(String userId) {//belirli kullanıcının attığı tweetleri listelemek için getPostsByUserId metodunu oluşturuyoruz
        User user = getUserById(userId);
        List<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<PostResponse> responses = new ArrayList<>();

        for (Post post : posts) {

            responses.add(convertToResponse(post, user.getUsername()));
        }

        return responses;
    }

    private PostResponse convertToResponse(Post post, String username) {//metodu sadece postservice kullanacağı için private.bu metodun amacı, Post nesnesini PostResponse nesnesine dönüştürmektir. PostResponse, frontend'e gönderilecek olan veri yapısını temsil eder.

        PostResponse response = new PostResponse();

        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setCreatedAt(post.getCreatedAt());
        response.setUsername(username);
        response.setUserId(post.getUserId());//post içindeki sahibini gösteren userid 

        return response;
    }
private User getUserById(String userId) {

    return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found."));
}
}
