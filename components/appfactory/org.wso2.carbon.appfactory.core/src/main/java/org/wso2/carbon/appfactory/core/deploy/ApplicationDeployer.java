package org.wso2.carbon.appfactory.core.deploy;

public class ApplicationDeployer {

	public void deployArtifact(String applicationId, String version, String revision) {
		// ArtifactStorage storage = ServiceHolder.getArtifactStorage();
		// File file = storage.retrieveArtifact(applicationId, version,
		// revision);

		// ApplicationUploadClient applicationUploadClient = new
		// ApplicationUploadClient(deploymentServerUrl);
		//
		// UploadedFileItem uploadedFileItem = new UploadedFileItem();
		//
		// DataHandler dataHandler = new DataHandler(new FileDataSource(file));
		// uploadedFileItem.setDataHandler(dataHandler);
		// uploadedFileItem.setFileName(file.getName());
		// uploadedFileItem.setFileType("jar");
		//
		// UploadedFileItem[] uploadedFileItems = {uploadedFileItem};
		//
		// String remoteIp = null;
		// try {
		// URL deploymentURL = new URL(deploymentServerUrl);
		// remoteIp = deploymentURL.getHost();
		// } catch (MalformedURLException e) {
		// handleException("Deployment server url is malformed.");
		// }
		//
		// try {
		// if
		// (applicationUploadClient.authenticate(getAdminUsername(applicationId),
		// appFactoryConfiguration.getFirstProperty(
		// AppFactoryConstants.SERVER_ADMIN_PASSWORD), remoteIp)) {
		// applicationUploadClient.uploadCarbonApp(uploadedFileItems);
		// log.info(deployArtifact.getName() + " is successfully uploaded.");
		// } else {
		// handleException("Failed to login to " + remoteIp +
		// " to deploy artifact:" + deployArtifact.getName());
		// }
		// } catch (Exception e) {
		// handleException("Failed to upload the artifact:" + deployArtifact +
		// " of application:" +
		// applicationId + " to deployment location:" + deploymentServerUrl);
		// }
	}

}
