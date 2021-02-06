package com.buinak.imageapi.controller;

import com.buinak.imageapi.entity.Image;
import com.buinak.imageapi.entity.ImageData;
import com.buinak.imageapi.repository.ImageDataRepository;
import com.buinak.imageapi.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller("/image")
public class ImageController {

    private final ImageRepository imageRepository;
    private final ImageDataRepository imageDataRepository;

    @Autowired
    public ImageController(ImageRepository imageRepository, ImageDataRepository imageDataRepository) {
        this.imageRepository = imageRepository;
        this.imageDataRepository = imageDataRepository;
    }

    @PostMapping(path = "addImage")
    public ResponseEntity<Image> addImage(@RequestParam(defaultValue = "NAME") String name,
                                          @RequestParam(defaultValue = "DESC") String description){
        ImageData imageData = ImageData.builder().build();
        imageDataRepository.saveAndFlush(imageData);

        Image image = Image.builder()
                .name(name)
                .description(description)
                .imageData(imageData)
                .build();

        return ResponseEntity.ok().body(imageRepository.saveAndFlush(image));
    }

    @GetMapping(path = "findImageByName")
    public ResponseEntity<ImageRepository.ImageInformationView> findImageByName(@RequestParam String name){
        Optional<ImageRepository.ImageInformationView> optionalImage = imageRepository.findByName(name);

        if (optionalImage.isPresent()){
            return ResponseEntity.ok().body(optionalImage.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping(path = "patchImage")
    public ResponseEntity<ImageRepository.ImageInformationView> patchImage(@RequestBody Image image){
        Optional<Image> optionalImage = imageRepository.findById(image.getId());

        if (optionalImage.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Image managedImage = optionalImage.get();
        managedImage.setName(image.getName());
        managedImage.setDescription(image.getDescription());

        imageRepository.saveAndFlush(managedImage);
        return ResponseEntity.ok(imageRepository.findByName(image.getName()).orElseThrow());
    }
}
