package org.apache.velocity.runtime.resource.loader;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
 * @author <a href="mailto:shiva.krishnaah@gmail.com">Shiva Krishna</a>
 * @version $Id$
 */
    public class S3ResourceLoader extends ResourceLoader {
        private String accessKey;
        private String secretKey;
        private String bucketName;
        private AmazonS3 amazonS3;
        @Override
        public void init(ExtendedProperties extendedProperties) {
            accessKey = StringUtils.nullTrim(extendedProperties.getString("s3.accessKey"));
            secretKey = StringUtils.nullTrim(extendedProperties.getString("s3.secretKey"));
            bucketName = StringUtils.nullTrim(extendedProperties.getString("s3.bucketName"));
            log.debug("Access Key "+accessKey+" Secret Key "+secretKey+" Bucket Name "+bucketName);

            amazonS3 = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));

        }

        @Override
        public InputStream getResourceStream(String s3Key) throws ResourceNotFoundException {

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
            ObjectMetadata metadata=amazonS3.getObjectMetadata(bucketName,resource.getName());
            return metadata.getLastModified().getTime();
        }

    }