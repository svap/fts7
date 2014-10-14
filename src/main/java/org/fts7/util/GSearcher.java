/*
 * Copyright 2014 Sergey Apollonov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fts7.util;

import org.fts7.searchRes.SearchResultItem;
import org.fts7.searchRes.SearchResult;
import org.fts7.searchRes.ObjectItem;
import org.fts7.Search;
import javax.swing.filechooser.*;
import javax.swing.event.*;
import java.awt.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * GUI full-text search utility, based on Swing Java GUI classes.
 * <p>
 * Usage: java -cp fts7.jar org.fts7.util.GSearcher
 * 
 * @author Sergey Apollonov
 */
public class GSearcher extends javax.swing.JFrame {

    // Creates new form NewJFrame
    public GSearcher() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fc = new javax.swing.JFileChooser();
        searchPhrase = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        selectButton = new javax.swing.JButton();
        LabelFilename = new java.awt.Label();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();

        fc.setAcceptAllFileFilterUsed(false);
        fc.setDialogTitle("Choose an index file");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Full text Searcher");

        searchButton.setText("Search");
        searchButton.setEnabled(false);
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        selectButton.setText("Select Index");
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        jEditorPane1.setEditable(false);
        jEditorPane1.setContentType("text/html"); // NOI18N
        jScrollPane1.setViewportView(jEditorPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(searchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(selectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(searchPhrase, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                            .addComponent(LabelFilename, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(selectButton)
                    .addComponent(LabelFilename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchPhrase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // search button
    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        // TODO add your handling code here:
        try {
            Search se=new Search(LabelFilename.getText());
            SearchResult res=se.search(searchPhrase.getText(), 1);
            
            // print the search result (res)
            StringBuilder bb= new StringBuilder();
            bb.append("<html>");
            for(SearchResultItem a:res){ 
            bb.append(String.format("<p>%d. ", a.n));
            for(ObjectItem f:a.objects) // print found file names
                                       bb.append(String.format("<a href=\"file:%s\">%s</a><br>",f.name,f.name));
            // print the best relevant piece of a file content
            bb.append(a.getContentRelevantPiece(6,60, "<font color=red>%s</font>"));
            }
            
            bb.append(String.format("<p>Total found %d  totalSearchTime=%d printed first 20 items (page 1)",res.cnt,res.totalSearchTime));
            bb.append("</html>");
            jEditorPane1.setText(bb.toString());
            
            jEditorPane1.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
              if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } 
                catch (Exception e1) {e1.printStackTrace();} 
              }
            }
            }
            });
            }
        catch (Exception e) {e.printStackTrace();}
    }//GEN-LAST:event_searchButtonActionPerformed

    // select an index file
    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        // TODO add your handling code here:
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(new FileFilter() {
 
              public String getDescription() {
                      return "Index file (*.db)";
                      }
 
              public boolean accept(File f) {
              if (f.isDirectory()) {
                       return true;
                } else {
                        return f.getName().toLowerCase().endsWith(".db");
                       }
              }
        });
        if (fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
            String name=fc.getSelectedFile().getAbsolutePath();
            LabelFilename.setText(name);
            searchButton.setEnabled(true);
        }
    }//GEN-LAST:event_selectButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GSearcher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GSearcher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GSearcher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GSearcher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GSearcher().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Label LabelFilename;
    private javax.swing.JFileChooser fc;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchPhrase;
    private javax.swing.JButton selectButton;
    // End of variables declaration//GEN-END:variables
}
