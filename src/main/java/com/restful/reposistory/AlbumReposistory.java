package com.restful.reposistory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restful.model.Album;

@Repository
public interface AlbumReposistory extends JpaRepository<Album, Long>{

    List<Album> findByAccount_id(long id);
      

    
}
