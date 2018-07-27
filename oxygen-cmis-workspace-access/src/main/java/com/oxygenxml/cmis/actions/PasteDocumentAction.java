package com.oxygenxml.cmis.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.chemistry.opencmis.client.api.Document;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;
import com.oxygenxml.cmis.ui.BreadcrumbView;
import com.oxygenxml.cmis.ui.ItemsPresenter;

public class PasteDocumentAction extends AbstractAction {
  private IResource resource = null;
  private ItemsPresenter itemsPresenter;

  public PasteDocumentAction(String name ,IResource resource, ItemsPresenter itemsPresenter) {
    super(name);

    this.resource = resource;
    this.itemsPresenter = itemsPresenter;
  }

  /**
   * get string from Clipboard
   */
  private String getSysClipboardText() {
    String ret = null;
    Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

    Transferable clipTf = sysClip.getContents(null);

    if (clipTf != null) {

      if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        try {
          ret = (String) clipTf.getTransferData(DataFlavor.stringFlavor);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    return ret;
  }
  // Is called every time  the class is instantiated
  @Override
  public boolean isEnabled() {
    return super.isEnabled() && getSysClipboardText() != null;
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    if (getSysClipboardText() != null) {

      Document docClipboard = CMISAccess.getInstance().createResourceController().getDocument(getSysClipboardText());
      FolderImpl currentFolder = BreadcrumbView.currentFolder;
      CMISAccess.getInstance().createResourceController().addToFolder(currentFolder.getFolder(), docClipboard);
      
      itemsPresenter.presentFolderItems(currentFolder.getId());
    }
  }

}