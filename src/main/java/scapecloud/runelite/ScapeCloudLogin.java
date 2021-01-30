/*
 * Copyright (c) 2021, ScapeCloud
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package scapecloud.runelite;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.awt.event.ActionEvent;

@Singleton
public class ScapeCloudLogin {

    @Inject
    private ConfigManager manager;

    @Inject
    private ScapeCloudAPI api;

    private JFrame frame;

    private JLabel emailLabel;
    private JLabel passLabel;

    private JTextField emailField;
    private JPasswordField passwordField;

    private JButton createButton;
    private JButton loginButton;

    private boolean initialized = false;

    public void display() {
        if (!initialized) initialize();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void initialize() {
        System.out.println(UIManager.getLookAndFeel().getClass());
        frame = new JFrame("ScapeCould Login");
        frame.setSize(300, 300);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setIconImage(ImageUtil.loadImageResource(getClass(), "scapecloud_icon.png"));

        emailLabel = new JLabel("Email:");
        emailLabel.setBounds(20, 10, 75, 30);

        emailField = new JTextField();
        emailField.setBounds(20, 40, 250, 30);

        passLabel = new JLabel("Password:");
        passLabel.setBounds(20, 75, 75, 30);

        passwordField = new JPasswordField();
        passwordField.setBounds(20, 105, 250, 30);

        createButton = new JButton("Sign Up");
        createButton.setBounds(75, 160, 150, 30);

        loginButton = new JButton("Log In");
        loginButton.setBounds(75, 210, 150, 30);

        createButton.addActionListener(this::create);
        loginButton.addActionListener(this::login);

        frame.getContentPane().setLayout(null);
        frame.getContentPane().add(emailLabel);
        frame.getContentPane().add(emailField);
        frame.getContentPane().add(passLabel);
        frame.getContentPane().add(passwordField);
        frame.getContentPane().add(createButton);
        frame.getContentPane().add(loginButton);

        initialized = true;
    }


    private void create(ActionEvent e) {
        LinkBrowser.browse("https://scape-cloud.tmwed.vercel.app/pricing");
    }

    private void login(ActionEvent e) {
        if (emailField.getText().length() > 0 && passwordField.getPassword().length > 0) {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            api.authenticate(email, password,
                    () -> {
                        manager.setConfiguration("scape-cloud", "email", email);
                        manager.setConfiguration("scape-cloud", "password", password);
                        JOptionPane.showMessageDialog(frame, "Login Successful", "ScapeCloud Login", JOptionPane.INFORMATION_MESSAGE);
                        frame.setVisible(false);
                    },
                    (error) -> JOptionPane.showMessageDialog(frame, error.getError().getMessage(), "ScapeCloud Login", JOptionPane.ERROR_MESSAGE)
            );
        }
    }
}
