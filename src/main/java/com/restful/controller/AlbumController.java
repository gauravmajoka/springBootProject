package com.restful.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import com.restful.model.Account;
import com.restful.model.Album;
import com.restful.model.Photo;

import org.apache.commons.lang3.RandomStringUtils;
import com.restful.payload.auth.album.AlbumDTO;
import com.restful.payload.auth.album.AlbumViewDTO;
import com.restful.payload.auth.album.PhotoDTO;
import com.restful.payload.auth.album.PhotoPayloadDTO;
import com.restful.payload.auth.album.PhotoViewDTO;
import com.restful.service.AccountService;
import com.restful.service.AlbumService;
import com.restful.service.PhotoService;
import com.restful.util.appUtils.AppUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;



@RestController
@RequestMapping("/album")
@Tag(name = "ALbum Controller", description = "Controller for Album and photos management")
@Slf4j
public class AlbumController {

     static final String PHOTOS_FOLDER_NAME = "photos";
    static final String THUMBNAIL_FOLDER_NAME = "thumbnails";
    static final int THUMBNAIL_WIDTH = 300;

     @Autowired
    private AccountService accountService;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private PhotoService photoService;


     @PostMapping(value = "/add", consumes = "application/json" ,produces = "application/json")
     @ResponseStatus(HttpStatus.CREATED)
     @ApiResponse(responseCode = "400", description = "error")
     @ApiResponse(responseCode = "200", description = "New Album Added")
     @Operation(summary = "Add new Album")
     @SecurityRequirement(name = "demo-api")
     public ResponseEntity<AlbumViewDTO> addAlbum(@Valid @RequestBody AlbumDTO albumDTO ,Authentication authenticate){
        try {
            Album album = new Album();
            album.setName(albumDTO.getName());
            album.setDescription(albumDTO.getDescription());
            String email = authenticate.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
            album.setAccount(account);
            album = albumService.save(album);
            AlbumViewDTO albumViewDTO = new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(), null);
            return ResponseEntity.ok(albumViewDTO);
            

        } catch (Exception e) {
           // log.debug(AlbumError.ADD_ALBUM_ERROR.toString() + ": "+ e.getMessage());
            return new ResponseEntity<AlbumViewDTO>(HttpStatus.BAD_REQUEST);
        }

     }

     @GetMapping(value = "/albums", produces = "application/json")
     @ApiResponse(responseCode = "400", description = "error")
     @ApiResponse(responseCode = "200", description = "List Of Album")
     @Operation(summary = "View Album")
     @SecurityRequirement(name = "demo-api")
     public List<AlbumViewDTO> albums(Authentication authentication) {
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();
        List<AlbumViewDTO> albums = new ArrayList<>();
        for(Album album : albumService.findByAccount_id(account.getId())){
             List<PhotoDTO> photos = new ArrayList<>();
            for(Photo photo: photoService.findByAlbumId(album.getId())){
                String link = "/albums/"+album.getId()+"/photos/"+photo.getId()+"/download-photo";
                photos.add(new PhotoDTO(photo.getId(), photo.getName(), photo.getDescription(), 
                photo.getFileName(), link));

            }
            albums.add(new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(), photos));

        }

        return albums;
     }

      @GetMapping(value = "/albums/{album_id}", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "List of albums")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token Error")
    @Operation(summary = "List album by album ID")
    @SecurityRequirement(name = "demo-api")
    public ResponseEntity<AlbumViewDTO> albums_by_id(@PathVariable long album_id, Authentication authentication) {
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();
        Optional<Album> optionalAlbum = albumService.findById(album_id);
        Album album;
        if (optionalAlbum.isPresent()) {
            album = optionalAlbum.get();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        if (account.getId() != album.getAccount().getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<PhotoDTO> photos = new ArrayList<>();
        for (Photo photo : photoService.findByAlbumId(album.getId())) {
            String link = "/albums/"+album.getId()+"/photos/"+photo.getId()+"/download-photo";
            photos.add(new PhotoDTO(photo.getId(), photo.getName(), photo.getDescription(), photo.getFileName(), link));
        }

        AlbumViewDTO albumViewDTO = new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(), photos);

        return ResponseEntity.ok(albumViewDTO);
    }

    @PutMapping(value = "/albums/{album_id}/update", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please add valid name a description")
    @ApiResponse(responseCode = "204", description = "Album update")
    @Operation(summary = "Update an Album")
    @SecurityRequirement(name = "demo-api")
    public ResponseEntity<AlbumViewDTO> update_Album(@Valid @RequestBody AlbumDTO albumPayloadDTO,
            @PathVariable long album_id, Authentication authentication) {
        try {

            String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
    
            Optional<Album> optionaAlbum = albumService.findById(album_id);
            Album album;
            if (optionaAlbum.isPresent()) {
                album = optionaAlbum.get();
                if (account.getId() != album.getAccount().getId()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }


            album.setName(albumPayloadDTO.getName());
            album.setDescription(albumPayloadDTO.getDescription());
            album = albumService.save(album);
            List<PhotoDTO> photos = new ArrayList<>();
            for(Photo photo: photoService.findByAlbumId(album.getId())){
                String link = "/albums/"+album.getId()+"/photos/"+photo.getId()+"/download-photo";
                photos.add(new PhotoDTO(photo.getId(), photo.getName(), photo.getDescription(), 
                photo.getFileName(), link));

            }
            AlbumViewDTO albumViewDTO = new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(), photos);
            return ResponseEntity.ok(albumViewDTO);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

     @PutMapping(value = "albums/{album_id}/photos/{photo_id}/update", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please add valid name a description")
    @ApiResponse(responseCode = "204", description = "Album update")
    @Operation(summary = "Update a photo")
    @SecurityRequirement(name = "demo-api")
    public ResponseEntity<PhotoViewDTO> update_photo(@Valid @RequestBody PhotoPayloadDTO photoPayloadDTO,
            @PathVariable long album_id, @PathVariable long photo_id,Authentication authentication) {
        try {

            String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
    
            Optional<Album> optionaAlbum = albumService.findById(album_id);
            Album album;
            if (optionaAlbum.isPresent()) {
                album = optionaAlbum.get();
                if (account.getId() != album.getAccount().getId()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            Optional<Photo> optionalPhoto = photoService.findById(photo_id);
            if(optionalPhoto.isPresent()){
                Photo photo = optionalPhoto.get();
                if (photo.getAlbum().getId() != album_id) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
                photo.setName(photoPayloadDTO.getName());
                photo.setDescription(photoPayloadDTO.getDescription());
                photoService.save(photo);
                PhotoViewDTO photoViewDTO = new PhotoViewDTO(photo.getId(), photoPayloadDTO.getName(), photoPayloadDTO.getDescription());
                return ResponseEntity.ok(photoViewDTO);
            }else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

        } catch (Exception e) {
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

     @DeleteMapping(value = "albums/{album_id}/photos/{photo_id}/delete")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "202", description = "Photo delete")
    @Operation(summary = "delete a photo")
    @SecurityRequirement(name = "demo-api")
    public ResponseEntity<String> delete_photo(@PathVariable long album_id, 
    @PathVariable long photo_id,Authentication authentication) {
        try {

            String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
    
            Optional<Album> optionaAlbum = albumService.findById(album_id);
            Album album;
            if (optionaAlbum.isPresent()) {
                album = optionaAlbum.get();
                if (account.getId() != album.getAccount().getId()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            Optional<Photo> optionalPhoto = photoService.findById(photo_id);
            if(optionalPhoto.isPresent()){
                Photo photo = optionalPhoto.get();
                if (photo.getAlbum().getId() != album_id) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
                
                AppUtils.delete_photo_from_path(photo.getFileName(), PHOTOS_FOLDER_NAME, album_id);
                AppUtils.delete_photo_from_path(photo.getFileName(), THUMBNAIL_FOLDER_NAME, album_id);
                photoService.delete(photo);

                return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
            }else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

        } catch (Exception e) {
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping(value = "albums/{album_id}/delete")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "202", description = "Album deleted")
    @Operation(summary = "delete a album")
    @SecurityRequirement(name = "demo-api")
    public ResponseEntity<String> delete_album(@PathVariable long album_id,Authentication authentication) {
        try {

            String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
    
            Optional<Album> optionaAlbum = albumService.findById(album_id);
            Album album;
            if (optionaAlbum.isPresent()) {
                album = optionaAlbum.get();
                if (account.getId() != album.getAccount().getId()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
           
            for (Photo photo : photoService.findByAlbumId(album.getId())) {
                AppUtils.delete_photo_from_path(photo.getFileName(), PHOTOS_FOLDER_NAME, album_id);
                AppUtils.delete_photo_from_path(photo.getFileName(), THUMBNAIL_FOLDER_NAME, album_id);
                photoService.delete(photo);
            }
            albumService.deleteAlbum(album);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
           

        } catch (Exception e) {
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }



    @PostMapping(value = "albums/{album_id}/upload-photos", consumes = {"multipart/form-data"})
    @Operation(summary = "Upload photo into album")
    @ApiResponse(responseCode = "400", description = "Please check the payload or token")
    @SecurityRequirement(name = "demo-api")
    public ResponseEntity<List<HashMap<String, List<String>>>> photos(@RequestPart(required = true) MultipartFile[] files, 
    @PathVariable long album_id, Authentication authentication){
         String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();
        Optional<Album> optionaAlbum = albumService.findById(album_id);
        Album album;
        if(optionaAlbum.isPresent()){
            album = optionaAlbum.get();
            if(account.getId() != album.getAccount().getId()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

         List<String> fileNamesWithSuccess = new ArrayList<>();
         List<String> fileNamesWithError = new ArrayList<>();

          Arrays.asList(files).stream().forEach(file -> { 
            String contentType = file.getContentType();
            if(contentType.equals("image/png")
            || contentType.equals("image/jpg")
            || contentType.equals("image/jpeg")){
                fileNamesWithSuccess.add(file.getOriginalFilename());

                 int length = 10;
                boolean useLetters = true;
                boolean useNumbers = true;

                try {
                     String fileName = file.getOriginalFilename();
                    String generatedString = RandomStringUtils.random(length, useLetters,useNumbers);
                    String final_photo_name = generatedString+fileName;
                    String absolute_fileLocation = AppUtils.get_photo_upload_path(final_photo_name, PHOTOS_FOLDER_NAME,  album_id);
                    Path path = Paths.get(absolute_fileLocation);
                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                    Photo photo = new Photo();
                    photo.setName(fileName);
                    photo.setFileName(final_photo_name);
                    photo.setOrignalFileName(fileName);
                    photo.setAlbum(album);
                    photoService.save(photo);
                    
                    BufferedImage thumbImg = AppUtils.getThumbnail(file, THUMBNAIL_WIDTH);
                    File thumbnail_location = new File(AppUtils.get_photo_upload_path(final_photo_name, THUMBNAIL_FOLDER_NAME, album_id));
                    ImageIO.write(thumbImg, file.getContentType().split("/")[1], thumbnail_location);

                } catch (Exception e) {
                    fileNamesWithError.add(file.getOriginalFilename());
                }

                 }else{
                fileNamesWithError.add(file.getOriginalFilename()); }
            }); 
            HashMap<String, List<String>> result = new HashMap<>();
        result.put("SUCCESS", fileNamesWithSuccess);
        result.put("ERRORS", fileNamesWithError);
        
        List<HashMap<String, List<String>>> response = new ArrayList<>();
        response.add(result);
        
        return ResponseEntity.ok(response);



    }


    
}

