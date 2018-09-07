package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;
import com.oxygenxml.cmis.ui.ItemsPresenter;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Describes the copy action on a document by extending the AbstractAction class
 * 
 * @author bluecc
 *
 */
public class CreateDocumentAction extends AbstractAction {
  // Parent of the resource
  private IResource currentParent;
  // Presenter to use to show the resources
  private ItemsPresenter itemsPresenter;
  // New document created
  private DocumentImpl documentCreated;
  private String versioningState;

  /**
   * Constructor that receives data to process for the creation and presentation
   * of the newly created document
   * 
   * @param resource
   * @param currentParent
   * @param itemsPresenter
   */
  public CreateDocumentAction(IResource currentParent, ItemsPresenter itemsPresenter, String versioningState) {

    // Set a name and use a native icon
    super("Create document " + versioningState, UIManager.getIcon("FileView.fileIcon"));

    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
    this.versioningState = versioningState;
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the document
   * 
   * @param e
   * @exception UnsupportedEncodingException
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    // Get input from user
    String getInput = JOptionPane.showInputDialog(null, "Plase enter a name", "myfile");
    System.out.println("The input=" + getInput);
    Document doc;
    Document pwc = null;

    // Try creating the document with the string from the input
    try {
      // Create a versioned document with the state of MAJOR
      doc = CMISAccess.getInstance().createResourceController().createVersionedDocument(
          ((FolderImpl) currentParent).getFolder(), getInput, "", "text/plain", "VersionableType",
          VersioningState.valueOf(versioningState));

      // Checkout the document
      try {

        documentCreated = new DocumentImpl(doc);
        pwc = documentCreated.checkOut(documentCreated.getDocType());

      } catch (Exception e2) {
        // Show the exception if there is one
        JOptionPane.showMessageDialog(null, "Exception " + e2.getMessage());
      }

    } catch (UnsupportedEncodingException e1) {

      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Unsupported encoding: " + e1.getMessage());

    } catch (org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException e2) {
      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Document already exists " + e2.getMessage());
    }

    // -------- Open into Oxygen
    String urlAsTring = null;
    if (pwc != null) {
      // Get the <Code>getCustomURL</Code> of the document created
      urlAsTring = CmisURLConnection.generateURLObject(pwc, CMISAccess.getInstance().createResourceController());

      System.out.println(urlAsTring);

      // Get the workspace of the plugin
      PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();

      // Check if it's not null
      if (pluginWorkspace != null) {

        // Try openning the url in the workspace
        try {
          pluginWorkspace.open(new URL(urlAsTring));

        } catch (MalformedURLException e1) {

          // Show the exception if there is one
          JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
        }

      }
    } else {
      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception PWC is null");
    }
    // --------
    
    currentParent.refresh();
    itemsPresenter.presentFolderItems(currentParent.getId());
  }

}
