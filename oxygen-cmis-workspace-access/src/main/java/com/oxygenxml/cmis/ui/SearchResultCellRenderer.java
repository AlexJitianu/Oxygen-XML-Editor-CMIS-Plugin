package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
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

import sun.swing.DefaultLookup;

public class SearchResultCellRenderer extends JPanel implements ListCellRenderer<IResource> {
  private JPanel iconPanel;
  private JLabel iconLabel;

  private JPanel descriptionPanel;
  private JLabel nameRsource;
  private JLabel pathResource;
  private JLabel lineResource;

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
    nameRsource = new JLabel();
    descriptionPanel.add(nameRsource, c);

    c.gridx = 0;
    c.gridy = 1;
    c.insets = new Insets(5, 10, 5, 10);
    c.anchor = GridBagConstraints.BASELINE_LEADING;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    pathResource = new JLabel();
    descriptionPanel.add(pathResource, c);

    c.gridx = 0;
    c.gridy = 2;
    c.insets = new Insets(5, 10, 5, 10);
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    lineResource = new JLabel();
    descriptionPanel.add(lineResource, c);

    // Notification panel
    notifierPanel = new JPanel(new BorderLayout());
    notifierPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Will be drawn in paintComponent
    notification = new JLabel() {
      // @Override
      // protected void paintComponent(Graphics g) {
      // // TODO Auto-generated method stub
      //
      // Graphics2D g2d = (Graphics2D) g;
      // g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      // RenderingHints.VALUE_ANTIALIAS_ON);
      // super.paintComponent(g);
      // g2d.setPaint(Color.BLUE);
      // g2d.fill(new Ellipse2D.Double(getWidth() - 18 - 5, getHeight() / 2 - 9,
      // 18, 18));
      //
      // final String text = "" + notification.getText();
      // final Font oldFont = g2d.getFont();
      //
      // g2d.setFont(oldFont.deriveFont(oldFont.getSize() - 1f));
      //
      // final FontMetrics fm = g2d.getFontMetrics();
      // g2d.setPaint(Color.WHITE);
      // g2d.drawString(text, getWidth() - 9 - 5 - fm.stringWidth(text) / 2,
      // getHeight() / 2 + (fm.getAscent() - fm.getLeading() - fm.getDescent())
      // / 2);
      // g2d.setFont(oldFont);
      // }
    };
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

    setComponentOrientation(list.getComponentOrientation());

    Color bg = null;
    Color fg = null;

    nameRsource.setText(value.getDisplayName());

    if (value instanceof DocumentImpl && value != null) {

      DocumentImpl doc = ((DocumentImpl) value);

      if (doc.isPrivateWorkingCopy() && doc.isCheckedOut()) {

        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/workingcopy.png")));
        System.out.println("DocPWC:" + doc.getDisplayName());

      } else if (doc.isCheckedOut()) {

        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/checkedout.png")));
        System.out.println("Doc:" + doc.getDisplayName());

      } else {

        iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/file.png")));

      }
      System.out.println();

      pathValue = contentProv.getPath(doc, ctrl);

      notifyValue = "By:" + doc.getCreatedBy();
      // TODO: use breadcrumb view for the path

      System.out.println("Line=" + contentProv.getLineDoc(doc, matchPattern));
      String resultContext = contentProv.getLineDoc(doc, matchPattern);

      if (resultContext != null) {
        resultContext = escapeHTML(resultContext);

        if (matchPattern != null) {
          resultContext = getReadyHTMLSplit(resultContext, matchPattern);
        }
      }

      lineResource.setText("<html>" + (resultContext != null ? resultContext : "Loading...") + "</html>");

    } else if (value instanceof FolderImpl && value != null) {

      FolderImpl folder = ((FolderImpl) value);
      iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/folder.png")));

      notifyValue = "By:" + folder.getCreatedBy();
      pathValue = contentProv.getPath(folder, ctrl);

      lineResource.setText("");

    }

    pathResource.setText(pathValue);
    notification.setText(notifyValue);

    JList.DropLocation dropLocation = list.getDropLocation();
    if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index) {

      bg = DefaultLookup.getColor(this, ui, "List.dropCellBackground");
      fg = DefaultLookup.getColor(this, ui, "List.dropCellForeground");

      isSelected = true;
    }

    if (isSelected) {
      setBackgroundC(this, bg == null ? list.getSelectionBackground() : bg);
      setForegroundC(this, fg == null ? list.getSelectionForeground() : fg);
    } else {
      setBackgroundC(this, list.getBackground());
      setForegroundC(this, list.getForeground());
    }

    return this;
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

  static String getReadyHTMLSplit(String context, String matchPattern) {
    String toReturn = "";

    System.out.println("COntext=" + context);
    System.out.println("Pattern=" + matchPattern);
    String[] splits = context.split(matchPattern);

    for (int index = 0; index < splits.length; index += 1) {

      String styledContext = splits[index];
      String styledMatch = "";
      if (index != splits.length - 1) {
        styledMatch = "<nobr style='background-color:yellow; color:gray'>" + matchPattern + "</nobr>";
      }
      toReturn += (styledContext + styledMatch);
    }

    return toReturn;
  }

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
