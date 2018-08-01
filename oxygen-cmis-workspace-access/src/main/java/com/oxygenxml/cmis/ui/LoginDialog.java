package com.oxygenxml.cmis.ui;

import java.awt.Container;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.oxygenxml.cmis.core.UserCredentials;

import ro.sync.exml.workspace.api.standalone.ui.OKCancelDialog;

public class LoginDialog extends OKCancelDialog {
  private JTextField userField;
  private JTextField passwordField;

  public LoginDialog(JFrame frame) {
    
    super(frame, "Login", true);
    setSize(400,200);

    //Initialize components
    JLabel userLabel = new JLabel("Username");
    JLabel passwordLabel = new JLabel("Password");

    
    userField = new JTextField();
    passwordField = new JTextField();

    //Set Layout
    Container contentPane = getContentPane();
    contentPane.setLayout(new GridLayout(4, 2));

    //Add components
    contentPane.add(userLabel);
    contentPane.add(userField);
    contentPane.add(passwordLabel);
    contentPane.add(passwordField);

    setLocationRelativeTo(frame);

    // This solves the problem where the dialog was not getting
    // focus the second time it was displayed
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        userField.requestFocusInWindow();
      }
    });

    setModal(true);
  }

  public UserCredentials getUserCredentials() {
    
    //Create the user and set it's credentials
    UserCredentials user = new UserCredentials();

    user.setUsername(this.userField.getText());
    user.setPassword(this.passwordField.getText());

    return user;

  }
}
