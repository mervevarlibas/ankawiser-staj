package com.mwitter.repository;
import org.springframework.data.mongodb.repository.MongoRepository;//hazır metodları kullanabilmek için MongoRepository sınıfını import ediyoruz.
import java.util.List;
import com.mwitter.model.Post;
public interface PostRepository extends MongoRepository<Post, String> {
 List<Post> findAllByOrderByCreatedAtDesc();//tüm postları oluşturulma tarihine göre azalan sırada listelemek 
List<Post> findByUser_IdOrderByCreatedAtDesc(String userId);

}
