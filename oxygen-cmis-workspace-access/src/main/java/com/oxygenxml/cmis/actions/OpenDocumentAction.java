package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.urlhandler.CmisURLConnection;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;

/**
 * Describes the delete folder action on a document by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class OpenDocumentAction extends AbstractAction {
  // The resource to open
  private transient IResource resource = null;
  private final boolean allowEditOriginal = false;

  /**
   * Constructor that gets the resource to open
   * 
   * @param resource
   */
  public OpenDocumentAction(IResource resource) {

    // Set a name and a native icon
    super("Open document", UIManager.getIcon("Tree.openIcon"));

    this.resource = resource;

    setEnabled(((DocumentImpl) resource).canUserOpen());
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the document.
   * 
   * <b>This action will delete everything inside the folder (folders,
   * documents)</b>
   * 
   * @param e
   * @exception MalformedURLException
   *              , UnsupportedEncodingException
   * 
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {

    openDocumentPath();

  }

  public void openDocumentPath() {
    boolean editable = false;
    DocumentImpl currDoc = (DocumentImpl) resource;
    if (currDoc.isVersionable()) {

      if (currDoc.isCheckedOut() && currDoc.canUserUpdateContent()) {
        editable = true;
      } else {
        editable = allowEditOriginal;
      }
    } else {
      editable = true;
    }

    // if (currDoc.isVersionable() currDoc.canUserUpdateContent() ||
    // currDoc.canUserCheckout()) {
    // editable = true;
    // }
    // -------Oxygen

    // Initialize the URL
    String urlAsTring = null;

    urlAsTring = CmisURLConnection.generateURLObject(currDoc.getDoc(),
        CMISAccess.getInstance().createResourceController());

    // Get the workspace of the plugin
    PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();

    // Check if it's not null
    if (pluginWorkspace != null) {

      // Try opening in the Oxygen the URL
      try {

        if (pluginWorkspace.open(new URL(urlAsTring))) {

          // if null - image preview opened
          WSEditor editorAccess = pluginWorkspace.getEditorAccess(new URL(urlAsTring),
              PluginWorkspace.MAIN_EDITING_AREA);

          if (editorAccess != null) {
            editorAccess.setEditable(editable);
          } else {

            pluginWorkspace.openInExternalApplication(new URL(urlAsTring), true);

          }
        }

      } catch (MalformedURLException e1) {

        // Show the exception if there is one
        JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
      }

    }
    // ------
  }
}
