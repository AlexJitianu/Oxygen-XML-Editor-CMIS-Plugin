package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.chemistry.opencmis.client.api.ObjectId;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.ui.BreadcrumbView;
import com.oxygenxml.cmis.ui.ItemListView;
import com.oxygenxml.cmis.ui.ItemsPresenter;

/**
 * Describes the delete folder action on a document by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class DeleteFolderAction extends AbstractAction {

  // The resource to be deleted
  private IResource resource;
  // Parent of that resource
  private IResource currentParent;
  // Presenter to be able to update the content of the parent
  private ItemsPresenter itemsPresenter;

  /**
   * Constructor that gets the resource to be deleted , currentParent and the
   * presenter to be able to show the updated content of it
   * 
   * @param resource
   * @param currentParent
   * @param itemsPresenter
   */
  public DeleteFolderAction(IResource resource, IResource currentParent, ItemsPresenter itemsPresenter) {
    // Set a name
    super("Delete");

    this.resource = resource;
    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the folder.
   * 
   * <b>This action will delete everything inside the folder (folders,
   * documents)</b>
   * 
   * @param e
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {

    // Cast to the custom interface to use it's methods
    FolderImpl folderToDelete = ((FolderImpl) resource);

    // Try deleting the folder
    try {
      CMISAccess.getInstance().createResourceController().deleteFolderTree(folderToDelete.getFolder());

      // Present the newly updated content of the parent folder
      itemsPresenter.presentFolderItems(currentParent.getId());

    } catch (Exception ev) {

      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception " + ev.getMessage());
    }

  }
}
