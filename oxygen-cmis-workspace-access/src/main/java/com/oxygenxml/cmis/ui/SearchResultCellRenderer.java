package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.oxygenxml.cmis.core.CMISAccess;
import com.oxygenxml.cmis.core.ResourceController;
import com.oxygenxml.cmis.core.model.IResource;
import com.oxygenxml.cmis.core.model.impl.DocumentImpl;
import com.oxygenxml.cmis.core.model.impl.FolderImpl;

import ro.sync.exml.plugin.Plugin;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import sun.swing.DefaultLookup;

public class SearchResultCellRenderer extends JPanel implements ListCellRenderer<IResource> {
  private JPanel iconPanel;
  private JLabel iconLabel;

  private JPanel descriptionPanel;
  private JLabel nameResource;
  private JLabel propertiesResource;
  private JLabel lineResource;
  private JPanel lineResourcePanel;

  private JPanel notifierPanel;
  private JLabel notification;

  private ContentSearchProvider contentProv;

  // Graphics configurations
  private boolean isSelected;
  private String matchPattern;

  public SearchResultCellRenderer(ContentSearchProvider contentProvider, String matchPattern) {
    contentProv = contentProvider;
    this.matchPattern = matchPattern;

    setLayout(new BorderLayout());

    // Drawing will occur in paintComponent
    iconPanel = new JPanel(new BorderLayout());
    iconLabel = new JLabel();
    iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/tip.png")));
    iconPanel.add(iconLabel);
    iconPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Description panel
    descriptionPanel = new JPanel();
    descriptionPanel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(5, 10, 5, 10);
    c.anchor = GridBagConstraints.BASELINE_LEADING;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    nameResource = new JLabel();
    descriptionPanel.add(nameResource, c);

    c.gridx = 0;
    c.gridy = 1;
    c.insets = new Insets(5, 10, 0, 10);
    c.anchor = GridBagConstraints.BASELINE_LEADING;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    propertiesResource = new JLabel();
    descriptionPanel.add(propertiesResource, c);

    lineResourcePanel = new JPanel(new BorderLayout());
    lineResourcePanel.setPreferredSize(new java.awt.Dimension(100, 70));
    c.gridx = 0;
    c.gridy = 2;
    c.insets = new Insets(0, 10, 5, 10);
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 1;
    c.weighty = 1;
    c.gridheight = 2;
    c.fill = GridBagConstraints.BOTH;
    lineResource = new JLabel();

    // lineResource.setContentType("text/html");
    lineResourcePanel.add(lineResource, BorderLayout.CENTER);
    descriptionPanel.add(lineResourcePanel, c);

    // Notification panel
    notifierPanel = new JPanel(new BorderLayout());
    notifierPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Will be drawn in paintComponent
    notification = new JLabel();
    notifierPanel.add(notification);

    add(iconPanel, BorderLayout.WEST);
    add(descriptionPanel, BorderLayout.CENTER);
    add(notifierPanel, BorderLayout.EAST);
    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
  }

  @Override
  public Component getListCellRendererComponent(JList<? extends IResource> list, IResource value, int index,
      boolean isSelected, boolean cellHasFocus) {
    // Initialize the graphics configurations for the cell
    this.isSelected = isSelected;

    ResourceController ctrl = CMISAccess.getInstance().createResourceController();

    String pathValue = null;
    String notifyValue = null;
    String propertiesValues = null;

    setComponentOrientation(list.getComponentOrientation());

    Color bg = null;
    Color fg = null;
    String resourceText = styleString(value.getDisplayName());

    if (value instanceof DocumentImpl && value != null) {

      DocumentImpl doc = ((DocumentImpl) value);

      if (doc.isPrivateWorkingCopy() && doc.isCheckedOut()) {

        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/workingcopy.png")));
        // System.out.println("DocPWC:" + doc.getDisplayName());

      } else if (doc.isCheckedOut()) {

        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/checkedout.png")));
        // System.out.println("Doc:" + doc.getDisplayName());

      } else {

        try {
          iconLabel.setIcon((Icon) PluginWorkspaceProvider.getPluginWorkspace().getImageUtilities()
              .getIconDecoration(new URL("http://localhost/" + value.getDisplayName())));

        } catch (MalformedURLException e) {

          iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/file.png")));
        }

      }

      pathValue = contentProv.getPath(doc, ctrl);
      propertiesValues = contentProv.getProperties(doc);
      notifyValue = "By:" + doc.getCreatedBy();

      // System.out.println("Line=" + contentProv.getLineDoc(doc,
      // matchPattern));

      // Get the results from the server
      String resultContext = contentProv.getLineDoc(doc, matchPattern);

      resultContext = styleString(resultContext);

      lineResource.setText(
          "<html><code style=' overflow-wrap: break-word; word-wrap: break-word; margin: 5px; padding: 5px; background-color:red;text-align: center;vertical-align: middle;'>"
              + (resultContext != null ? resultContext : "No data") + "</code></html>");

    } else if (value instanceof FolderImpl && value != null) {

      FolderImpl folder = ((FolderImpl) value);
      iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/folder.png")));

      pathValue = contentProv.getPath(folder, ctrl);
      propertiesValues = contentProv.getProperties(folder);
      notifyValue = "By:" + folder.getCreatedBy();

      lineResource.setText(
          "<html><code style=' overflow-wrap: break-word; word-wrap: break-word; margin: 5px; padding: 5px; background-color:red;text-align: center;vertical-align: middle;'>"
              + "No data" + "</code></html>");

    }
    nameResource.setText("<html><div style=' overflow-wrap: break-word; word-wrap: break-word; background-color:red;'>"
        + (resourceText != null ? resourceText : "No data") + "</div></html>");

    propertiesResource.setText(propertiesValues);
    nameResource.setToolTipText(pathValue);
    notification.setText(notifyValue);

    JList.DropLocation dropLocation = list.getDropLocation();
    if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index) {

      bg = DefaultLookup.getColor(this, ui, "List.dropCellBackground");
      fg = DefaultLookup.getColor(this, ui, "List.dropCellForeground");

      isSelected = true;
    }

    if (isSelected) {
      setToolTipText(pathValue);
      setBackgroundC(this, bg == null ? list.getSelectionBackground() : bg);
      setForegroundC(this, fg == null ? list.getSelectionForeground() : fg);
    } else {
      setBackgroundC(this, list.getBackground());
      setForegroundC(this, list.getForeground());
    }

    return this;
  }

  private String styleString(String resultContext) {
    // Check if there is some data
    if (resultContext != null) {

      // Escape the HTML
      resultContext = escapeHTML(resultContext);

      System.out.println("Before split = " + resultContext);
      // Check if there is something in searchbar
      if (matchPattern != null) {
        // Split the words entered as keys
        String[] searchKeys = matchPattern.trim().split("\\s+");

        // Get the styled HTML splitted
        resultContext = getReadyHTMLSplit(resultContext, searchKeys);
      }

      System.out.println("After split = " + resultContext);
    }
    return resultContext;
  }

  private void setBackgroundC(Component c, Color background) {
    c.setBackground(background);

    if (c instanceof Container) {
      Component[] components = ((Container) c).getComponents();
      for (int i = 0; i < components.length; i++) {
        Component child = components[i];

        setBackgroundC(child, background);
      }
    }
  }

  private void setForegroundC(Component c, Color foreground) {
    c.setForeground(foreground);

    if (c instanceof Container) {
      Component[] components = ((Container) c).getComponents();
      for (int i = 0; i < components.length; i++) {
        Component child = components[i];

        setForegroundC(child, foreground);
      }
    }
  }

  // public ApplicationListResizeSensitive(boolean forwardSelection) {
  // super(forwardSelection);
  //
  // // The HTML content of the renderers may wrap to the list viewport
  // bounds, leading to
  // // a different height for the same value
  //
  // // Receives resize events and invalidates the list.
  // final ComponentAdapter parentComponentListener = new
  // ComponentAdapter() {
  //
  // @Override
  // public void componentResized(ComponentEvent e) {
  // size = e.getComponent().getSize();
  // clearRendererAllocationCache();
  // }
  // };
  //
  // addHierarchyListener(new HierarchyListener() {
  // /**
  // * A reference to the previous linked caret. Used to avoid
  // clearing the cache too often.
  // */
  // private Container oldParent = null;
  // @Override
  // public void hierarchyChanged(HierarchyEvent e) {
  // // Such events can be fired quite often. For example when
  //// changing the selected tab in a tabbed pane.
  //
  // Container parent = getParent();
  // if (parent != null
  // // A new parent.
  // && (parent != oldParent
  // // Just a precaution. The size of the parent changed. Maybe
  //// it can happen if inside
  // // a tabbed pane when switching.
  // || !ro.sync.basic.util.Equaler.verifyEquals(size,
  // parent.getSize()))) {
  // // Avoid linking multiple times.
  // parent.removeComponentListener(parentComponentListener);
  // parent.addComponentListener(parentComponentListener);
  // clearRendererAllocationCache();
  //
  // oldParent = parent;
  // }
  // }
  // });
  // }

  /**
   * Invalidates the bounds of the cells.
   */
  // public void clearRendererAllocationCache() {
  //
  // ListCellRenderer cellRenderer = getOriginalCellRenderer();
  // setCellRenderer(null);
  // setCellRenderer(cellRenderer);
  // }
  // protected void update(String htmlContent, boolean selected, boolean
  // isHovered, int htmlWidth) {
  // this.isSelected = selected;
  // this.isHovered = isHovered;
  // // Update the colors.
  //
  // StringBuilder html = new StringBuilder();
  // html.append("<html><body>");
  //
  // if (htmlWidth == -1) {
  // htmlWidth = getVisibleListWidth(0);
  // }
  //
  // html.append(" <table cellspacing='" + getRendererTableCellSpacing() + "'
  // cellpadding='0' width='")
  // .append(htmlWidth).append("'>");
  // html.append(htmlContent);
  // html.append("</table></body></html>");
  //
  // // Update the dimensions of the text label.
  // this.setBackground(UIManager.getColor("TextArea.background"));
  // this.setText(html.toString());
  //
  // View view = (View) this.getClientProperty(BasicHTML.propertyKey);
  // if (view != null) {
  // double w = view.getPreferredSpan(View.X_AXIS);
  // double h = view.getPreferredSpan(View.Y_AXIS);
  //
  // // Do not force label size beyond minimum size.
  // double minimumWidth = this.getMinimumSize().getWidth();
  // double minimumHeight = this.getMinimumSize().getHeight();
  // if (w < minimumWidth) {
  // w = minimumWidth;
  // }
  // if (h < minimumHeight) {
  // h = minimumHeight;
  // }
  //
  // this.setPreferredSize(new Dimension((int) w, (int) h));
  // } else {
  // this.setPreferredSize(new Dimension(10, 10));
  // }
  // }

  /**
   * Matcher used to get all the star and end indexes for replacing the original
   * data
   * 
   * 
   * @param context
   * @param searchKeys
   * @return The styled string to be showed
   */
  static String getReadyHTMLSplit(String context, String[] searchKeys) {
    String contextToSplit = context;
    StringBuffer stBuffer = new StringBuffer(contextToSplit);
    String styledMatch = "";

    System.out.println("COntext=" + stBuffer.toString() + " Size =" + (stBuffer.length() - 1));

    // Concatenate all the keys from the search
    String regex = "";
    for (String string : searchKeys) {
      regex += string + "|";
    }

    // Use a stack to store data because we will show them from the back in
    // order to not destroy the original string
    Stack<ObjectFound> foundObjects = new Stack<ObjectFound>();

    // Matters to preserve the order of the keys
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(stBuffer.toString());

    // While some results are found
    while (matcher.find()) {
      String found = matcher.group();

      // There is data
      if (!found.equals("")) {
        int startIndex = matcher.start();
        int endIndex = matcher.end();

        // Create a new object
        foundObjects.push(new ObjectFound(startIndex, endIndex, found.trim()));

//        System.out.print("Start index: " + startIndex);
//        System.out.print(" End index: " + endIndex);
//        System.out.println(" Found: " + found.trim());
      }
    }

    // Iterate all the objects from the stack
    while (!foundObjects.isEmpty()) {

      ObjectFound element = foundObjects.peek();
      styledMatch = "<nobr style=' background-color:yellow; color:gray'>" + element.getContent() + "</nobr>";
//      System.out.println("Index from list=" + element.getStartIndex());
//      System.out.println("Till = " + element.getEndIndex() + " The key =" + element.getContent());

      stBuffer.replace(element.getStartIndex(), element.getEndIndex(), styledMatch);
      foundObjects.pop();

    }
   // System.out.println(" FinalCOntext=" + stBuffer.toString());

    return stBuffer.toString();
  }

  /**
   * Escapes the HTML by replacing the signs with their codename
   * 
   * @param s
   * @return
   */
  public static String escapeHTML(String s) {
    StringBuilder out = new StringBuilder(Math.max(16, s.length()));
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
        out.append("&#");
        out.append((int) c);
        out.append(';');
      } else {
        out.append(c);
      }
    }
    return out.toString();
  }
}

/**
 * Class use only for the sake of storing the start,endindex and the data fround
 * 
 * @author bluecc
 *
 */
class ObjectFound {
  private int startIndex = 0;
  private int endIndex = 0;
  private String content = "";

  /**
   * Constructor
   * 
   * @param startIndex
   * @param endIndex
   * @param content
   */
  public ObjectFound(int startIndex, int endIndex, String content) {
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.content = content;
  }

  public int getStartIndex() {
    return this.startIndex;

  }

  public int getEndIndex() {
    return this.endIndex;

  }

  public String getContent() {
    return this.content;

  }
}