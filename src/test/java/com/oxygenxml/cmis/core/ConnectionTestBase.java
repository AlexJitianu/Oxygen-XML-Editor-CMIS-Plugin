package com.oxygenxml.cmis.core;

import java.io.IOException;
import java.io.Reader;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;

public class ConnectionTestBase {

  /**
   * Tests is a document exists in the parent folder.
   * 
   * @param document
   * @param folder
   * @return
   */
  protected boolean documentExists(Document document, Folder folder) {
    for(CmisObject child : folder.getChildren()) {
      if(child instanceof Document) {
        if(document.equals(child)) {
          return true;
        }
      }
    }
    return false;
  }
  

  protected String  read(Reader docContent) throws IOException {
    StringBuilder b = new StringBuilder();
    try {
      char[] c = new char[1024];
      int l = -1;
      while ((l = docContent.read(c)) != -1) {
        b.append(c, 0, l);
      }
    } finally {
      docContent.close();
    }
    return b.toString();
  }
}
