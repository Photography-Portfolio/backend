package com.buinak.imageapi.controller;

import com.buinak.imageapi.entity.Image;
import com.buinak.imageapi.repository.ImageRepository;
import com.buinak.imageapi.service.ImageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ImageControllerTest {

    @LocalServerPort
    public int port;

    String BASE_URL;

    RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

    @Autowired
    ImageController imageController;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ImageService imageService;

    @Before
    public void setUp() {
         BASE_URL = "http://localhost:" + port;
    }

    @Test
    public void addImage() {
        MockMultipartFile multipartFile = new MockMultipartFile("data", "testimg.jpeg", "text/plain", "some xml".getBytes());

        Image newImage = imageController.addImage("NAME1", "DESC1", multipartFile).getBody();
        Image image = restTemplate.getForObject(BASE_URL + "/images/" + newImage.getId(), Image.class);

        assertThat(image.getName()).isEqualTo("NAME1");
        assertThat(image.getDescription()).isEqualTo("DESC1");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void listImages() {
        MockMultipartFile firstFile = new MockMultipartFile("data", "testimg.jpeg", "text/plain", "some xml".getBytes());
        MockMultipartFile secondFile = new MockMultipartFile("data2", "testimg2.jpeg", "text/plain", "some xml".getBytes());

        imageController.addImage("list", "list", firstFile);
        imageController.addImage("list2", "list2", secondFile);
        List<String> images = restTemplate.getForObject(BASE_URL + "/images/links", List.class);

        assertThat(images.size()).isEqualTo(2);
        assertThat(images.get(0)).isEqualTo(BASE_URL + File.separator + "testimg.jpeg");
        assertThat(images.get(1)).isEqualTo(BASE_URL + File.separator + "testimg2.jpeg");

    }

    @Test
    public void patchImage() {
        MockMultipartFile multipartFile = new MockMultipartFile("data", "testimg.jpeg", "text/plain", "some xml".getBytes());
        Image image = imageController.addImage("NAME2", "DESC2", multipartFile).getBody();
        image.setDescription("string");
        image.setName("string");

        Image patchedImage = restTemplate.patchForObject(BASE_URL + "/images/" + image.getId(), image, Image.class);

        assertThat(patchedImage.getName()).isEqualTo("string");
        assertThat(patchedImage.getDescription()).isEqualTo("string");
    }

    @Test(expected = HttpClientErrorException.class)
    public void deleteImage() {
        MockMultipartFile multipartFile = new MockMultipartFile("data", "testimg.jpeg", "text/plain", "some xml".getBytes());
        Image image = imageController.addImage("NAME3", "DESC3", multipartFile).getBody();

        restTemplate.exchange(BASE_URL + "/images/" + image.getId(), HttpMethod.DELETE, HttpEntity.EMPTY, Long.class);
        restTemplate.exchange(BASE_URL + "/images/" + image.getId(), HttpMethod.DELETE, HttpEntity.EMPTY, Long.class);

        fail("Test did not throw exception");
    }
}
