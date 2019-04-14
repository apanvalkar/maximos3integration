package com.aniruddh.s3.doclinks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.ibm.tivoli.maximo.oslc.provider.AttachmentStorage;

import psdi.iface.mic.MicUtil;
import psdi.mbo.MboRemote;
import psdi.util.MXException;
import psdi.util.logging.MXLogger;
import psdi.util.logging.MXLoggerFactory;

public class S3AttachmentStorage extends AttachmentStorage{

	private static MXLogger logger = MXLoggerFactory.getLogger("maximo.s3");
	
	@Override
	public void cleanupStorage() throws RemoteException, MXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createAttachment(String fileName, byte[] fileBytes, String mimeType) throws RemoteException, MXException {
		
		String awsAccessKey = MicUtil.getProperty("mxe.s3.accesskey");
		String awsSecretKey = MicUtil.getProperty("mxe.s3.secretkey");
		String bucketName = MicUtil.getProperty("mxe.s3.bucket");
		String regionName = MicUtil.getProperty("mxe.s3.region");
		//String pathPrefix = MicUtil.getProperty("mxe.s3.pseudo.pathprefix");
		
		ByteArrayInputStream fileToUpload = new ByteArrayInputStream(fileBytes);
		
		logger.info("S3 Properties: " + awsAccessKey + "  " + awsSecretKey + "  " + bucketName  + "    " + regionName);
		
		logger.info("fileName: " + fileName);
		
		logger.info("bytes: " + fileBytes);
		
		logger.info("mimeType: " + mimeType);
				
		String pathPrefix = MicUtil.getProperty("mxe.s3.pseudo.pathprefix");
		 
		fileName = fileName.substring(pathPrefix.length() + 1);
				
		uploadToS3(awsAccessKey, awsSecretKey, regionName, bucketName, fileName, fileToUpload, mimeType);
		
		try {
			fileToUpload.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void deleteAttachment(MboRemote arg0) throws RemoteException, MXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] getAttachment(MboRemote doclink) throws RemoteException, MXException {
		// TODO Auto-generated method stub
		String awsAccessKey = MicUtil.getProperty("mxe.s3.accesskey");
		String awsSecretKey = MicUtil.getProperty("mxe.s3.secretkey");
		String bucketName = MicUtil.getProperty("mxe.s3.bucket");
		String regionName = MicUtil.getProperty("mxe.s3.region");
		
		 String pathPrefix = MicUtil.getProperty("mxe.s3.pseudo.pathprefix");
		 String urlName = doclink.getString("urlname");
		 urlName = urlName.substring(pathPrefix.length() + 1);
		
		
		logger.info("Final Url: " + urlName);
		logger.info("S3 Properties: " + awsAccessKey + "  " + awsSecretKey + "  " + bucketName  + "    " + regionName);
		
		byte [] fileBytes = readFromS3(awsAccessKey, awsSecretKey, regionName, bucketName,urlName);
		
		
		return fileBytes;
	}

	@Override
	public byte[] getAttachment(String urlName) throws RemoteException, MXException {
		
		String awsAccessKey = MicUtil.getProperty("mxe.s3.accesskey");
		String awsSecretKey = MicUtil.getProperty("mxe.s3.secretkey");
		String bucketName = MicUtil.getProperty("mxe.s3.bucket");
		String regionName = MicUtil.getProperty("mxe.s3.region");
		
		 String pathPrefix = MicUtil.getProperty("mxe.s3.pseudo.pathprefix");
		 
		 urlName = urlName.substring(pathPrefix.length() + 1);
		
		
		logger.info("Final Url: " + urlName);
		logger.info("S3 Properties: " + awsAccessKey + "  " + awsSecretKey + "  " + bucketName  + "    " + regionName);
		
		byte [] fileBytes = readFromS3(awsAccessKey, awsSecretKey, regionName, bucketName,urlName);
		
		
		return fileBytes;
		
	}

	@Override
	public String getAttachmentQualifiedName(MboRemote doclink, String documentName) throws RemoteException, MXException {
		
		
		
		logger.info("documentName: " + documentName);
		logger.info("doctype: " + doclink.getString("doctype"));
		logger.info("document: " + doclink.getString("document"));
		
		 String pathPrefix = MicUtil.getProperty("mxe.s3.pseudo.pathprefix");
	    return pathPrefix + "/" + documentName;
	}
	

			  

	@Override
	public void setupStorage() throws RemoteException, MXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InputStream streamAttachment(MboRemote arg0) throws RemoteException, MXException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean uploadToS3(String awsAccessKey, String awsSecretKey, String clientRegion, 
			String bucketName, String fileName, ByteArrayInputStream fileBytes, String mimeType) {
		
try {
        	
        	BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        	AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
        							.withRegion(clientRegion)
        	                        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
        	                        .build();
        	
        	
        	
        	ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(mimeType);
            metadata.addUserMetadata("x-amz-meta-title", fileName);
            //metadata.setContentLength(fileBytes.toString().length());
            
            logger.info(" Upload file size: " + fileBytes.toString().length());
            
            logger.info("bytes: " + fileBytes.toString());
            logger.info(" Uploading file: " + fileName);
        	PutObjectRequest request = new PutObjectRequest(bucketName, fileName , fileBytes, metadata);
            
           // request.setMetadata(metadata);
            s3Client.putObject(request);
            
            logger.info("File uploaded.......................................");
        }
        catch(AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process 
            // it, so it returned an error response.
            e.printStackTrace();
        }
        catch(SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
		
		return false;
		
	}

	private byte[] readFromS3(String awsAccessKey, String awsSecretKey, String clientRegion, 
			String bucketName, String fileName) {
		

		BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
    	AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
    							.withRegion(clientRegion)
    	                        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
    	                        .build();
		
		S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, fileName));
		InputStream objectData = object.getObjectContent();
		// Process the objectData stream.
		byte[] fileByteArray = null;
		try {
			fileByteArray = IOUtils.toByteArray(objectData);
			
			objectData.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return fileByteArray;
		
	}
}
