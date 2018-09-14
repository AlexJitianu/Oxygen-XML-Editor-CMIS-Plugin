package com.oxygenxml.cmis.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.log4j.Logger;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.UserCredentials;

/**
 * Describes how the repositories a shown and their behaviors
 * 
 * @author bluecc
 *
 */
public class RepoComboBoxView extends JPanel implements RepositoriesPresenter {

  /**
   * Logging.
   */
  private static final Logger logger = Logger.getLogger(RepoComboBoxView.class);

  // Items to be shown
  private JComboBox<Repository> repoItems;
  // The list of the servers
  private List<Repository> serverReposList;
  // Current URL
  private URL serverURL;
  private ResourcesBrowser itemsPresenter;
  private BreadcrumbPresenter breadcrumbPresenter;

  /**
   * Constructor that receives an items presenter to show the items inside the
   * server and update the breadcrumb with breadcrumbPresenter
   * 
   * Creates the repositories component visually
   * 
   * @param itemsPresenter
   * @param breadcrumbPresenter
   */
  RepoComboBoxView(ResourcesBrowser itemsPresenter, BreadcrumbPresenter breadcrumbPresenter) {
    setOpaque(true);
    //setBackground(Color.cyan);

    this.itemsPresenter = itemsPresenter;
    this.breadcrumbPresenter = breadcrumbPresenter;

    repoItems = new JComboBox<Repository>();

    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    // Server Repository JLabel constraints
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;
    c.insets = new Insets(1, 10, 1, 10);
    JLabel serverUrlLabel = new JLabel("Repository:");
    serverUrlLabel.setOpaque(true);
    //serverUrlLabel.setBackground(Color.red);
    add(serverUrlLabel, c);

    // Url http JComboBox constraints
    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 1;
    c.gridwidth = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    repoItems.setOpaque(true);
    //repoItems.setBackground(Color.blue);

    repoItems.setEnabled(false);
    repoItems.setEditable(false);

    add(repoItems, c);
    /**
     * Gets the current selected url from combo box
     */
    repoItems.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        JComboBox<?> comboBox = (JComboBox<?>) e.getSource();

        Repository selected = (Repository) comboBox.getSelectedItem();
        System.out.println(selected.getId());

        // Present the items with the URL and repository
        itemsPresenter.presentResources(serverURL, selected.getId());

        // Reset the breadcrumb to show new items from repository
        breadcrumbPresenter.resetBreadcrumb(true);
      }
    });
  }

  /**
   * Implement presentRepositories using the serverURL
   * 
   * @exception Exception
   */
  @Override
  public void presentRepositories(URL serverURL) {
    repoItems.setEnabled(true);

    this.serverURL = serverURL;

    // Create the listRepo of repos.
    System.out.println(serverURL);

    // Check credentials for the URL
    UserCredentials userCredentials = null;
    try {
      userCredentials = AuthenticatorUtil.getUserCredentials(serverURL);

    } catch (Exception e1) {
      logger.error(e1, e1);

      JOptionPane.showMessageDialog(null, "Exception " + e1.getMessage());
    }

    if (userCredentials != null) {
      boolean loggedin = false;

      // Check if is logged in and there repositories to present
      do {

        // Check if logged in
        loggedin = AuthenticatorUtil.isLoggedin(serverURL);

        // Get the repositories
        serverReposList = CMISAccess.getInstance().connectToServerGetRepositories(serverURL, userCredentials);

      } while (serverReposList == null && !loggedin);

      System.out.println(serverReposList + "repos");

      // If there some put them in the model to be shown
      if (serverReposList != null && !serverReposList.isEmpty()) {

        // Present the items with the URL and repository
        itemsPresenter.presentResources(serverURL, serverReposList.get(0).getId());

        // Reset the breadcrumb to show new items from repository
        breadcrumbPresenter.resetBreadcrumb(true);

        DefaultComboBoxModel<Repository> model = new DefaultComboBoxModel<>();
        // Iterate all the elements
        for (Repository element : serverReposList) {
          model.addElement(element);
        }

        repoItems.setModel(model);

        /*
         * Render all the elements of the listRepo
         */
        repoItems.setRenderer(new DefaultListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
              boolean cellHasFocus) {
            String renderTex = "";
            if (value != null) {
              renderTex = ((Repository) value).getName();
            }

            if (renderTex.length() == 0) {
              renderTex = ((Repository) value).getId();
            }

            return super.getListCellRendererComponent(list, renderTex, index, isSelected, cellHasFocus);
          }
        });
      }
    }

  }
}
