package com.restful.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.restful.model.Photo;
import com.restful.reposistory.PhotoReposistory;

@Service
public class PhotoService {

    @Autowired
    private PhotoReposistory photoReposistory;

     public Photo save(Photo photo){
        return photoReposistory.save(photo);
    }

    public Optional<Photo> findById(long id){
        return photoReposistory.findById(id);
    }
    
    public void delete(Photo photo){
        photoReposistory.delete(photo);
    }

    public List<Photo> findByAlbumId(long id){
        return photoReposistory.findByAlbum_id(id);
    }
    
}
