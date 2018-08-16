package com.oxygenxml.cmis.core.urlhandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.UserCredentials;

import ro.sync.basic.util.URLUtil;
import ro.sync.ecss.extensions.api.webapp.plugin.UserActionRequiredException;

// Auth - URLStreamHandlerWithContext
// File Browsing
// Checkin / Checkout

public class CmisURLConnection extends URLConnection {

  private static final Logger logger = Logger.getLogger(CmisURLConnection.class.getName());

  private CMISAccess cmisAccess;
  private ResourceController bigCtrl;
  private UserCredentials credentials;

  // KEYWORDS
  public static final String CMIS_PROTOCOL = "cmis";
  private static final String REPOSITORY_PARAM = "repo";
  private static final String PATH_PARAM = "path";
  private static final String CONTENT_SAMPLE = "Empty";
  private static final String NONE_STATE = "none";
  private static final String MAJOR_STATE = "major";

  // CONSTRUCTOR
  public CmisURLConnection(URL url, CMISAccess cmisAccess, UserCredentials credentials) {
    super(url);
    this.cmisAccess = cmisAccess;
    this.credentials = credentials;
  }

  /**
   * 
   * @param object
   * @param _ctrl
   * @return
   * @throws UnsupportedEncodingException
   */
  public static String generateURLObject(CmisObject object, ResourceController ctrl, String parentPath) {

    // Builder for building custom URL
    StringBuilder urlb = new StringBuilder();

    // Get server URL
    String originalProtocol = ctrl.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);
    String repository = ctrl.getSession().getSessionParameters().get(SessionParameter.REPOSITORY_ID);
    // Encode server URL
    originalProtocol = URLUtil.encodeURIComponent(originalProtocol);

    // Generate first part of custom URL
    urlb.append((CMIS_PROTOCOL + "://")).append(originalProtocol).append("/").append(repository);

    // Get path of Cmis Object
    List<String> objectPath = ((FileableCmisObject) object).getPaths();

    parentPath = URLUtil.decodeURIComponent(parentPath);
    if (parentPath.contains(repository)) {
      parentPath = parentPath.replace(repository + "/", "");
    }

    // Append object path to URL
    for (int i = 0; i < objectPath.size(); i++) {
      logger.info("=> " + parentPath + " " + objectPath.get(i));
      // Check if path(i) start with path of parent folder
      if (objectPath.get(i).startsWith(parentPath)) {
        logger.info("here");
        for (String pth : objectPath.get(i).split("/")) {
          if (!pth.isEmpty()) {
            urlb.append("/").append(URLUtil.encodeURIComponent(pth));
          }
        }
      } else {
        urlb.append("/").append(object.getName());
        break;
      }
    }

    return urlb.toString();
  }

  /**
   * Overloads the one with the parent path
   * 
   * @param object
   * @param ctrl
   * @return
   */
  public static String generateURLObject(CmisObject object, ResourceController ctrl) {
    // Builder for building custom URL
    StringBuilder urlb = new StringBuilder();

    // Get server URL
    String originalProtocol = ctrl.getSession().getSessionParameters().get(SessionParameter.ATOMPUB_URL);
    String repository = ctrl.getSession().getSessionParameters().get(SessionParameter.REPOSITORY_ID);
    // Encode server URL
    originalProtocol = URLUtil.encodeURIComponent(originalProtocol);

    // Generate first part of custom URL
    urlb.append((CMIS_PROTOCOL + "://")).append(originalProtocol).append("/").append(repository);

    // Get path of Cmis Object
    List<String> objectPath = ((FileableCmisObject) object).getPaths();

    // Append object path to URL
    for (int i = 0; i < objectPath.size(); i++) {

      logger.info("here");
      for (String pth : objectPath.get(i).split("/")) {
        if (!pth.isEmpty()) {
          urlb.append("/").append(URLUtil.encodeURIComponent(pth));
        }
      }
    }
    return urlb.toString();
  }

  /**
   * Gets the CmisObject identified by the given URL.
   * 
   * @param url
   *          URL identifying a CMIS resource.
   * 
   * @return The CMIS object identified by the custom URL.
   * 
   * @throws MalformedURLException
   *           If the URL doesn't contain the expected syntax.
   * @throws UnsupportedEncodingException
   * @throws UserActionRequiredException
   */
  public CmisObject getCMISObject(String url)
      throws CmisUnauthorizedException, CmisObjectNotFoundException, MalformedURLException {
    // Decompose the custom URL in query elements
    Map<String, String> param = new HashMap<>();

    // Get from custom URL server URL for connection
    URL serverURL = getServerURL(url, param);

    // Get repository ID from custom URL for connection
    String repoID = param.get(REPOSITORY_PARAM);
    if (repoID == null) {
      throw new MalformedURLException("Mising repository ID inside: " + url);
    }

    // Accessing the server using params which we gets
    cmisAccess.connectToRepo(serverURL, repoID, credentials);
    bigCtrl = cmisAccess.createResourceController();

    // Get the object path
    String path = param.get(PATH_PARAM);

    // Get and return from server cmis object
    CmisObject objectFromURL = null;
    try {
      objectFromURL = bigCtrl.getSession().getObjectByPath(path);
    } catch (CmisObjectNotFoundException e) {
      if (path.lastIndexOf("/") == path.length() - 1) {
        path = path.substring(0, path.lastIndexOf("/"));
        objectFromURL = bigCtrl.getSession().getObjectByPath(path);
      } else {
        throw new CmisObjectNotFoundException();
      }
    }

    return objectFromURL;
  }

  /**
   * Builder server URL form given custom URL.
   * 
   * @param customURL
   * @param queryParams
   * @return
   * @throws MalformedURLException
   * @throws UnsupportedEncodingException
   */
  public static URL getServerURL(String customURL, Map<String, String> param) throws MalformedURLException {
    logger.info("getServerURL() => " + customURL);
    // Replace CMIS part
    if (customURL.startsWith(CmisURLConnection.CMIS_PROTOCOL)) {
      customURL = customURL.replaceFirst((CMIS_PROTOCOL + "://"), "");
    } else {
      // Test only!
      customURL = customURL.replaceFirst(customURL.substring(0, customURL.indexOf("://") + "://".length()), "");
    }

    String originalProtocol = customURL;

    // Get server URL, put it into originalProtocol and replace from customURL
    originalProtocol = originalProtocol.substring(0, originalProtocol.indexOf("/"));
    customURL = customURL.replaceFirst(originalProtocol, "");
    customURL = customURL.replaceFirst("/", "");

    // Save Repository and object path in HashMap
    if (param != null) {
      param.put(REPOSITORY_PARAM, customURL.substring(0, customURL.indexOf("/")));
      customURL = customURL.replaceFirst(param.get(REPOSITORY_PARAM), "");
      customURL = URLUtil.decodeURIComponent(customURL);
      param.put(PATH_PARAM, customURL);
    }

    // Creating server URL
    originalProtocol = URLUtil.decodeURIComponent(originalProtocol);
    String protocol = originalProtocol.substring(0, originalProtocol.indexOf("://"));

    URL url = new URL(originalProtocol + "/");
    URL serverURL = new URL(protocol, url.getHost(), url.getPort(),
        url.getPath().substring(0, url.getPath().lastIndexOf("/")));

    return serverURL;
  }

  @Override
  public void connect() throws IOException {
  }

  @Override
  public InputStream getInputStream() throws IOException {
    Document document = (Document) getCMISObject(getURL().toExternalForm());
    return document.getContentStream().getStream();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return new ByteArrayOutputStream() {
      @Override
      public void close() throws IOException {
        Document document = null;
        String docUrl = null;

        try {
          docUrl = getURL().toExternalForm();
          document = (Document) getCMISObject(docUrl);
        } catch (CmisObjectNotFoundException e) {
          // If created document doesn't exist we create one
          docUrl = createDocument();
          // Getting this document from returned URL to update it with new
          // content
          document = (Document) getCMISObject(docUrl);
        }

        // All bytes have been written.
        byte[] byteArray = toByteArray();
        ContentStream contentStream = new ContentStreamImpl(document.getName(), BigInteger.valueOf(byteArray.length),
            document.getContentStreamMimeType(), new ByteArrayInputStream(byteArray));

        document.setContentStream(contentStream, true);

      }
    };
  }

  /**
   * Create new document and generate URL if doesn't exist
   * 
   * @param document
   * @throws UnsupportedEncodingException
   * @throws MalformedURLException
   * @throws IOException
   */
  public String createDocument() throws MalformedURLException, UnsupportedEncodingException {
    HashMap<String, String> param = new HashMap<>();
    getServerURL(url.toExternalForm(), param);

    String path = param.get(PATH_PARAM);

    String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
    path = path.replace(fileName, "");

    String mimeType = MimeTypes.getMIMEType(fileName.substring(fileName.indexOf("."), fileName.length()));
    if (mimeType == "application/octet-stream") {
      mimeType = "text/xml";
    }

    /**
     * Differences between Alfresco & Jetty.
     * 
     * Sometimes Jetty server don't get object from path which have at the end
     * backslash. In this case I catch the CmisObjectNotFoundException and
     * remove invalid part of path.
     */
    Folder rootFolder = null;
    try {
      rootFolder = (Folder) cmisAccess.getSession().getObjectByPath(path);
    } catch (CmisObjectNotFoundException e) {
      if (path.lastIndexOf("/") == path.length() - 1) {
        path = path.substring(0, path.lastIndexOf("/"));
        rootFolder = (Folder) cmisAccess.getSession().getObjectByPath(path);
      }
    }

    /**
     * Differences between Alfresco & Jetty.
     * 
     * I noticed the Alfresco keep all object like "Versionable" and Jetty like
     * "None-Versionable". In this case I create a none-ver. object and if the
     * server support only "Versionable" objects I catch the
     * CmisConstraintException to handle this exception.
     */
    Document document;
    try {
      document = bigCtrl.createDocument(rootFolder, fileName, CONTENT_SAMPLE, mimeType, NONE_STATE);
    } catch (CmisConstraintException e) {
      document = bigCtrl.createDocument(rootFolder, fileName, CONTENT_SAMPLE, mimeType, MAJOR_STATE);
    }

    return generateURLObject(document, bigCtrl, path);
  }

  /**
   * 
   * @param url1
   * @return ResourceController
   * @throws MalformedURLException
   * @throws UnsupportedEncodingException
   * @throws UserActionRequiredException
   */
  public ResourceController getResourceController(URL url1) throws MalformedURLException {
    getCMISObject(url1.toExternalForm());
    return bigCtrl;
  }

  /**
   * 
   * @return CMISAccess
   */
  public CMISAccess getCMISAccess() {
    return cmisAccess;
  }

  /**
   * 
   * @return UserCredentials
   */
  public UserCredentials getUserCredentials() {
    return credentials;
  }
}
