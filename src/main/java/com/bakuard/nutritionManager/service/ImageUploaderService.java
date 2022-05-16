package com.bakuard.nutritionManager.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.ImageRepository;
import com.bakuard.nutritionManager.validation.ValidateException;

import org.apache.commons.codec.digest.DigestUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

public class ImageUploaderService implements DisposableBean {

    private static Logger logger = LoggerFactory.getLogger(ImageUploaderService.class);


    private final AmazonS3 s3;
    private final ImageRepository imageRepository;

    public ImageUploaderService(AppConfigData config, ImageRepository imageRepository) {
        this.imageRepository = imageRepository;

        s3 = AmazonS3ClientBuilder.standard().
                withCredentials(config.getAwsCredentialsProvider()).
                withRegion(Regions.US_EAST_1).
                build();

        Policy policy = new Policy()
                .withStatements(
                        new Statement(Statement.Effect.Allow)
                                .withPrincipals(Principal.AllUsers)
                                .withActions(S3Actions.GetObject)
                                .withResources(new Resource("arn:aws:s3:::nutritionmanagerimages/*")),
                        new Statement(Statement.Effect.Allow)
                                .withPrincipals(new Principal(config.getAwsUserId()))
                                .withActions(S3Actions.PutObject)
                                .withResources(new Resource("arn:aws:s3:::nutritionmanagerimages/*"))
                );

        s3.setBucketPolicy("nutritionmanagerimages", policy.toJson());
    }

    public URL uploadProductImage(UUID userId, MultipartFile image) {
        try {
            return uploadImage(userId, image, "productimages");
        } catch(Exception e) {
            throw new ValidateException("Fail to upload product image", e).
                    setUserMessageKey("ImageUploaderService.uploadProductImage");
        }
    }

    public URL uploadDishImage(UUID userId, MultipartFile image) {
        try {
            return uploadImage(userId, image, "dishimages");
        } catch(Exception e) {
            throw new ValidateException("Fail to upload dish image", e).
                    setUserMessageKey("ImageUploaderService.uploadDishImage");
        }
    }

    public URL uploadMenuImage(UUID userId, MultipartFile image) {
        try {
            return uploadImage(userId, image, "menuimages");
        } catch(Exception e) {
            throw new ValidateException("Fail to upload menu image", e).
                    setUserMessageKey("ImageUploaderService.uploadMenuImage");
        }
    }

    @Transactional
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24)
    public void removeUnusedImages() throws AmazonServiceException {
        logger.info("delete unused images");

        List<String> unusedImages = imageRepository.getUnusedImages();

        if(!unusedImages.isEmpty()) {
            logger.debug("There are {} unused images", unusedImages.size());

            DeleteObjectsRequest dor = new DeleteObjectsRequest("nutritionmanagerimages").
                    withKeys(unusedImages.toArray(String[]::new));
            s3.deleteObjects(dor);

            imageRepository.removeUnusedImages();
        }
    }

    @Override
    public void destroy() throws Exception {
        s3.shutdown();
    }


    private URL uploadImage(UUID userId, MultipartFile image, String folderName) {
        try {
            byte[] imageData = image.getBytes();

            String imageKey = folderName + '/' + DigestUtils.md2Hex(imageData).toUpperCase();

            URL imageUrl = imageRepository.getImageUrl(userId, imageKey);

            if(imageUrl == null) {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(imageData.length);
                s3.putObject("nutritionmanagerimages", imageKey, image.getInputStream(), metadata);

                imageUrl = s3.getUrl("nutritionmanagerimages", imageKey);

                imageRepository.addImageUrl(userId, imageKey, imageUrl);
            }

            return imageUrl;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

}
