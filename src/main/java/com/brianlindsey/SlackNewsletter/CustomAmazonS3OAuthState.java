package com.brianlindsey.SlackNewsletter;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.slack.api.bolt.Initializer;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.service.OAuthStateService;
import com.slack.api.bolt.util.JsonOps;

import java.io.IOException;

/**
 * OAuthStateService implementation using Amazon S3.
 *
 * @see <a href="https://aws.amazon.com/s3/">Amazon S3</a>
 */
public class CustomAmazonS3OAuthState implements OAuthStateService {

    private final String bucketName;

    public CustomAmazonS3OAuthState(String bucketName) {
        this.bucketName = bucketName;
    }
    
    
    @Override
    public boolean isValid(Request request) {
    	System.out.println("request: " + request.toString());
        // 0) the state parameter is available in the query string
        String givenState = extractStateFromQueryString(request);
        System.out.println("givenState: " + givenState);
        if (givenState == null || givenState.trim().isEmpty()) {
            return false;
        }
        
        // 1) the value is the same with the one in the cookie-based session
        String stateInSession = extractStateFromSession(request);
        System.out.println("stateInSession: " + stateInSession);
        if (stateInSession == null || stateInSession.trim().isEmpty()) {
            return false;
        }
        if (!stateInSession.equals(givenState)) {
            return false;
        }
        // 2) the value is valid on the server-side
        System.out.println("isAvailableInDatabase(givenState): " + isAvailableInDatabase(givenState));
        
        return isAvailableInDatabase(givenState);
    }

    @Override
    public Initializer initializer() {
        return (app) -> {
            // The first access to S3 tends to be slow on AWS Lambda.
            AWSCredentials credentials = getCredentials();
            if (credentials == null || credentials.getAWSAccessKeyId() == null) {
                throw new IllegalStateException("AWS credentials not found");
            }
            boolean bucketExists = createS3Client().doesBucketExistV2(bucketName);
            if (!bucketExists) {
                throw new IllegalStateException("Failed to access the Amazon S3 bucket (name: " + bucketName + ")");
            }
        };
    }

    @Override
    public void addNewStateToDatastore(String state) throws Exception {
        AmazonS3 s3 = this.createS3Client();
        String value = "" + (System.currentTimeMillis() + getExpirationInSeconds() * 1000);
        PutObjectResult putObjectResult = s3.putObject(bucketName, getKey(state), value);
        System.out.println("putObjectResult: " + putObjectResult.toString());
    }

    @Override
    public boolean isAvailableInDatabase(String state) {
        AmazonS3 s3 = this.createS3Client();
        S3Object s3Object = getObject(s3, getKey(state));
        if (s3Object == null) {
            return false;
        }
        String millisToExpire = null;
        try {
            millisToExpire = IOUtils.toString(s3Object.getObjectContent());
            return Long.valueOf(millisToExpire) > System.currentTimeMillis();
        } catch (IOException e) {
            System.out.println("Failed to load a state data for state");
            return false;
        } catch (NumberFormatException ne) {
            System.out.println("Numberformatexception");
            return false;
        }
    }

    @Override
    public void deleteStateFromDatastore(String state) throws Exception {
    	System.out.println("deleting but not really");
        AmazonS3 s3 = this.createS3Client();
        s3.deleteObject(bucketName, getKey(state));
    }

    protected AWSCredentials getCredentials() {
        return DefaultAWSCredentialsProviderChain.getInstance().getCredentials();
    }

    protected AmazonS3 createS3Client() {
        return AmazonS3ClientBuilder.defaultClient();
    }

    private String getKey(String state) {
        return "state/" + state;
    }

    private S3Object getObject(AmazonS3 s3, String fullKey) {
    	System.out.println("getting object: " + s3.toString() + " fullKey: " + fullKey);
        try {
            return s3.getObject(bucketName, fullKey);
        } catch (AmazonS3Exception e) {
            System.out.println("Amazon S3 object metadata not found");
            return null;
        }
    }

}
