package org.apache.velocity.runtime.resource.loader;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.util.StringUtils;

import java.io.InputStream;

/**
 * Created by Krishna on 20/06/15.
 */

    public class S3ResourceLoader extends ResourceLoader {
        private String accessKey;
        private String secretKey;
        private String bucketName;
        @Override
        public void init(ExtendedProperties extendedProperties) {
            accessKey = StringUtils.nullTrim(extendedProperties.getString("s3.accessKey"));
            secretKey = StringUtils.nullTrim(extendedProperties.getString("s3.secretKey"));
            bucketName = StringUtils.nullTrim(extendedProperties.getString("s3.bucketName"));
            log.debug("Access Key "+accessKey+" Secret Key "+secretKey+" Bucket Name "+bucketName);

        }

        @Override
        public InputStream getResourceStream(String s3Key) throws ResourceNotFoundException {
            AmazonS3 amazonS3 = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
            S3Object object = amazonS3.getObject(
                    new GetObjectRequest(bucketName, s3Key));
            return object.getObjectContent();
        }

        @Override
        public boolean isSourceModified(Resource resource) {
            return false;
        }

        @Override
        public long getLastModified(Resource resource) {
            AmazonS3 amazonS3 = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
            ObjectMetadata metadata=amazonS3.getObjectMetadata(bucketName,resource.getName());
            return metadata.getLastModified().getTime();
        }

    }