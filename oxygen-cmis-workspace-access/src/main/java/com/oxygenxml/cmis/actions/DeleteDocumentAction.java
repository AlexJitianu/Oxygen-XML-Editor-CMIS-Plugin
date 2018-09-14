package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.ui.DeleteDocDialog;
import com.oxygenxml.cmis.ui.ItemsPresenter;

import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;

/**
 * Describes the delete document action on a document by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class DeleteDocumentAction extends AbstractAction {

  // The resource to be deleted
  private IResource resource;
  // Parent of that resource
  private IResource currentParent;
  // Presenter to be able to update the content of the parent
  private ItemsPresenter itemsPresenter;
  private DeleteDocDialog inputDialog;
  private String deleteType;

  /**
   * Constructor that gets the resource to be deleted , currentParent and the
   * presenter to be able to show the updated content of it
   * 
   * @param resource
   * @param currentParent
   * @param itemsPresenter
   */
  public DeleteDocumentAction(IResource resource, IResource currentParent, ItemsPresenter itemsPresenter) {
    // Set a name
    super("Delete");

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;

    if (((DocumentImpl) resource).canUserDelete()) {
      this.enabled = true;
    } else {
      this.enabled = false;
    }

  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the document
   * 
   * <b>This action will delete one this version of the document</b>
   * 
   * @param e
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.DocumentImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    final PluginWorkspace pluginWorkspace = PluginWorkspaceProvider.getPluginWorkspace();
    final int defaultValueOfResult = -1;
    int result = defaultValueOfResult;

    // Cast to the custom type of Document
    final DocumentImpl doc = ((DocumentImpl) resource);

    // If status changed result = 0 means cancel and 1 means yes
    // FOr that a default value in necessarey
    do {
      // Create the input dialog
      inputDialog = new DeleteDocDialog((JFrame) pluginWorkspace.getParentFrame());

      deleteType = inputDialog.getDeleteType();
      result = inputDialog.getResult();

      if (result == 0) {
        break;
      }
    } while (result == defaultValueOfResult);

    if (result == 1) {
      try {

        // Try to delete <Code>deleteOneVersionDocument</Code>
        if (deleteType.equals("SINGLE")) {

          // Commit the deletion
          CMISAccess.getInstance().createResourceController().deleteOneVersionDocument(doc.getDoc());

        } else if (deleteType.equals("ALL")) {

          // Try to delete <Code>deleteAllVersionsDocument</Code>
          // Commit the deletion
          CMISAccess.getInstance().createResourceController().deleteAllVersionsDocument(doc.getDoc());
        }

        // Present the new content of the parent resource
        if (currentParent.getId().equals("#search.results")) {
          currentParent.refresh();

        } else {
          currentParent.refresh();
          itemsPresenter.presentResources(currentParent);
        }

      } catch (final Exception ev) {

        // Show the exception if there is one
        JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
      }
    }
  }
}
