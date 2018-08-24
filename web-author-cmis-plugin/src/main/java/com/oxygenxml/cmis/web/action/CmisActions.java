package com.oxygenxml.cmis.web.action;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.webapp.AuthorDocumentModel;
import ro.sync.ecss.extensions.api.webapp.AuthorOperationWithResult;
import ro.sync.ecss.extensions.api.webapp.SessionStore;
import ro.sync.ecss.extensions.api.webapp.WebappRestSafe;
import ro.sync.ecss.extensions.api.webapp.access.WebappPluginWorkspace;
import ro.sync.ecss.extensions.api.webapp.plugin.URLStreamHandlerWithContextUtil;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

@WebappRestSafe
public class CmisActions extends AuthorOperationWithResult {
	private static final Logger logger = Logger.getLogger(CmisActions.class.getName());

	private CmisURLConnection connection;
	private UserCredentials credentials;
	private Document document;

	private static final String CHECK_OUT = "cmisCheckout";
	private static final String CHECK_IN = "cmisCheckin";
	private static final String CANCEL_CHECK_OUT = "cancelCmisCheckout";
	private static final String COMMENT = "comment";
	private static final String ACTION = "action";

	@Override
	public String getDescription() {
		return "";
	}

	private String generateJson(Boolean isCheckedOut) {
		StringBuilder json = new StringBuilder();

		json.append("{");
		json.append("\"isCheckedOut\"" + ":" + isCheckedOut);
		json.append("}");

		return json.toString();
	}

	private void actionManipulator(String actualAction, String comment, AuthorDocumentModel model) {
		try {
			if (actualAction.equals(CHECK_OUT)) {
				CmisCheckOutAction.checkOutDocument(document, connection);
			}
			if (actualAction.equals(CHECK_IN)) {
				CmisCheckInAction.checkInDocument(document, connection, comment);
			}
			if (actualAction.equals(CANCEL_CHECK_OUT)) {
				CmisCheckOutAction.cancelCheckOutDocument(document, connection);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Invalid object or object URL!");
		}

	}

	@Override
	public String doOperation(AuthorDocumentModel model, ArgumentsMap args)
			throws IllegalArgumentException, AuthorOperationException {

		AuthorAccess authorAccess = model.getAuthorAccess();
		authorAccess.getWorkspaceAccess();

		// Get Session Store
		WebappPluginWorkspace workspace = (WebappPluginWorkspace) PluginWorkspaceProvider.getPluginWorkspace();
		SessionStore sessionStore = workspace.getSessionStore();

		// Get URL and ContextID and create new instance of CmisURLConnection
		URL url = authorAccess.getEditorAccess().getEditorLocation();
		String urlWithoutContextId = URLStreamHandlerWithContextUtil.getInstance().toStrippedExternalForm(url);
		String contextId = url.getUserInfo();
		credentials = sessionStore.get(contextId, "credentials");
		connection = new CmisURLConnection(url, new CMISAccess(), credentials);

		try {
			document = (Document) connection.getCMISObject(urlWithoutContextId);
		} catch (CmisUnauthorizedException e1) {
			e1.printStackTrace();
		} catch (CmisObjectNotFoundException e1) {
			e1.printStackTrace();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		logger.info("Is versionable: " + document.isVersionable());

		String actualAction = (String) args.getArgumentValue(ACTION);
		String actualComment = (String) args.getArgumentValue(COMMENT);

		logger.info(" actualAction: " + actualAction);

		if (!actualAction.isEmpty()) {
			actionManipulator(actualAction, actualComment, model);
		}

		Boolean isCheckedOut = document.isVersionSeriesCheckedOut();

		return generateJson(isCheckedOut);
	}
}