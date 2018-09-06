package com.oxygenxml.cmis.actions;

import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.ui.ItemsPresenter;

/**
 * Describes the create folder action on a document by extending the
 * AbstractAction class
 * 
 * @author bluecc
 *
 */
public class CreateFolderAction extends AbstractAction {

  // Presenter of the items
  private ItemsPresenter itemsPresenter;
  // Parent folder where new folder will be created
  private IResource currentParent;

  /**
   * Constructor that gets the parent where new folder will be created and a
   * presenter to know what to present
   * 
   * @param currentParent
   * @param itemsPresenter
   */
  public CreateFolderAction(IResource currentParent, ItemsPresenter itemsPresenter) {
    // Give a name and a native icon
    super("Create Folder", UIManager.getIcon("FileView.directoryIcon"));

    this.currentParent = currentParent;
    this.itemsPresenter = itemsPresenter;
  }

  /**
   * When the event was triggered cast the resource to custom interface for
   * processing the folder
   * 
   * @param e
   * 
   * @see com.oxygenxml.cmis.core.model.model.impl.FolderImpl
   */
  @Override
  public void actionPerformed(ActionEvent e) {

    // Get input from user
    String getInput = JOptionPane.showInputDialog(null, "Plase enter a name", "myfolder");
    System.out.println("The input=" + getInput);

    // Set current folder where we want a new folder
    System.out.println("Current parrent=" + currentParent.getDisplayName());
    FolderImpl currentFolder = (FolderImpl) currentParent;
    // Try creating the folder in the currentParent using the input
    try {
      CMISAccess.getInstance().createResourceController().createFolder(((FolderImpl) currentParent).getFolder(),
          getInput);

    } catch (Exception e1) {

      // Show the exception if there is one
      JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());

    }

    // Present the updated content of the current folder
    itemsPresenter.presentFolderItems(currentFolder.getId());
  }
}
