package com.restful.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.restful.model.Album;
import com.restful.reposistory.AlbumReposistory;

@Service
public class AlbumService {

    @Autowired
    private AlbumReposistory albumReposistory;

    public Album save(Album album){
        return albumReposistory.save(album);
    }

    public List<Album> findByAccount_id(long id){
        return albumReposistory.findByAccount_id(id);
    }

     public Optional<Album> findById(long id){
        return albumReposistory.findById(id);
    }

     public void deleteAlbum(Album album){
        albumReposistory.delete(album);
    }
    
}
