/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package godpostgresql;

/**
 *
 * @author test
 */

import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import static javafx.concurrent.Worker.State.FAILED;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javax.swing.*;

public class SimpleSwingBrowser implements Runnable {
       public static JFXPanel jfxPanel;
       public  static WebEngine engine;
       public  static WebView view;
       //boolean exit=true;

       private JFrame frame = new JFrame();
       private JPanel panel = new JPanel(new BorderLayout());
       private JLabel lblStatus = new JLabel();

       private JButton btnGo = new JButton("Reload");
      // private JTextField txtURL = new JTextField();
       private JProgressBar progressBar = new JProgressBar();

       private void initComponents() {
           jfxPanel = new JFXPanel();
           html=FenP.html;
           //System.out.println(" htnl Scene:"+html);
           createScene();

           ActionListener al = new ActionListener() {
               @Override public void actionPerformed(ActionEvent e) {
                   //initComponents();
                   loadURL(html);
                 //  frame.pack();
                 //  frame.setVisible(true);
                   
                 ///  engine.reload();

              }
          };

           btnGo.addActionListener(al);
          // txtURL.addActionListener(al);

           progressBar.setPreferredSize(new Dimension(150, 18));
           progressBar.setStringPainted(true);

           JPanel topBar = new JPanel(new BorderLayout(5, 0));
           topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
          // topBar.add(txtURL, BorderLayout.CENTER);
           topBar.add(btnGo, BorderLayout.EAST);


           JPanel statusBar = new JPanel(new BorderLayout(5, 0));
           statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
           statusBar.add(lblStatus, BorderLayout.CENTER);
           statusBar.add(progressBar, BorderLayout.EAST);

           panel.add(topBar, BorderLayout.NORTH);
           panel.add(jfxPanel, BorderLayout.CENTER);
           panel.add(statusBar, BorderLayout.SOUTH);

           frame.getContentPane().add(panel);
       }

       private void createScene() {

           Platform.runLater(new Runnable() {
               @Override public void run() {

                   WebView view = new WebView();
                   engine = view.getEngine();

                   engine.titleProperty().addListener(new ChangeListener<String>() {
                       public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
                           SwingUtilities.invokeLater(new Runnable() {
                               @Override public void run() {
                                   frame.setTitle(newValue);
                               }
                           });
                       }
                   });

                   engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                       public void handle(final WebEvent<String> event) {
                           SwingUtilities.invokeLater(new Runnable() {
                               @Override public void run() {
                                   lblStatus.setText(event.getData());
                               }
                           });
                       }
                   });

                /*   engine.locationProperty().addListener(new ChangeListener<String>() {
                       public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                           SwingUtilities.invokeLater(new Runnable() {
                               @Override public void run() {
                                  // txtURL.setText(newValue);
                               }
                           });
                       }
                   });*/

                   engine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
                       public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
                           SwingUtilities.invokeLater(new Runnable() {
                               @Override public void run() {
                                   progressBar.setValue(newValue.intValue());
                               }
                           });
                       }
                   });

                   engine.getLoadWorker()
                           .exceptionProperty()
                           .addListener(new ChangeListener<Throwable>() {

                               public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
                                   if (engine.getLoadWorker().getState() == FAILED) {
                                       SwingUtilities.invokeLater(new Runnable() {
                                           @Override public void run() {
                                               JOptionPane.showMessageDialog(
                                                       panel,
                                                       (value != null) ?
                                                       engine.getLocation() + "" + value.getMessage() :
                                                       engine.getLocation() + "Unexpected error.",
                                                       "Loading error...",
                                                       JOptionPane.ERROR_MESSAGE);
                                           }
                                       });
                                   }
                               }
                           });

                   jfxPanel.setScene(new Scene(view));
               }
           });
       }

       public void loadURL(final String url) {
           Platform.runLater(new Runnable() {
               @Override public void run() {
                   engine.setJavaScriptEnabled(true);
                   engine.loadContent(url);
                   
                   //engine.set(0.5);
               }
           });
       }
       private static String toURL(String str) {
           try {
               return new URL(str).toExternalForm();
           } catch (MalformedURLException exception) {
                   return null;
           }
       }
       @Override public void run() {
          frame.setPreferredSize(new Dimension(550, 600));
          frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
          initComponents();
          loadURL(html);
          frame.pack();
          frame.setVisible(true);
       }
       public static void main(String[] args) {
           SwingUtilities.invokeLater(new SimpleSwingBrowser());
         }
   String html;          

}