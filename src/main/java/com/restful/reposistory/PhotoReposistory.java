package com.restful.reposistory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restful.model.Photo;

@Repository
public interface PhotoReposistory extends JpaRepository<Photo, Long> {

  List<Photo> findByAlbum_id(long id);
    
}
