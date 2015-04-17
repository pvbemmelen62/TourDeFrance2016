
package nl.xs4all.pvbemmel.sudoku.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.event.HyperlinkEvent.*;

import nl.xs4all.pvbemmel.sudoku.util.*;

@SuppressWarnings("serial")
public class AboutDialog extends JDialog {
  static Logger getLogger() {
    return Logger.getLogger(AboutDialog.class.getName());
  }
  private Map<String,String> placeHolderMap;

  public AboutDialog(Map<String,String> placeHolderMap) {
    // If owner null, then will show default java icon; workaround:
    super(new JFrame("Hidden toplevel"));
    this.placeHolderMap = placeHolderMap;
    URL imgUrl = getClass().getResource("/images/logo-25-percent.png");
    ImageIcon imgIcon = new ImageIcon(imgUrl);
    Image iconImage = imgIcon.getImage();
    getOwner().setIconImage(iconImage);

    setTitle("About Sudoku Application");
    setIconImage(iconImage); //: probably not used; but in future it may be.
    setModalityType(ModalityType.APPLICATION_MODAL);

    Container cp = getContentPane();
    URL aboutUrl = getClass().getResource("/html/about.html");
    String text = null;
    try {
      text = StreamUtil.getAsString(aboutUrl.openStream());
    }
    catch (IOException e1) {
      text = "<html><body>Error while reading <code>about.html</code>"
        + "</body></html>";
    }
    // It is advisable to use "XXXPlaceHolder" for each placeholder key.
    for(String key : this.placeHolderMap.keySet()) {
      text = text.replaceAll(key, this.placeHolderMap.get(key));
    }
    JEditorPane editor = new JEditorPane("text/html", text);
    editor.setEditable(false);
    editor.addHyperlinkListener(new MyHyperLinkListener());
    Color labelBg = UIManager.getColor("Label.background");
    getLogger().fine("UIManager.getColor(\"Label.background\") : "
      + labelBg);
    editor.setBackground(labelBg);
    editor.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    JScrollPane scrollPane = new JScrollPane(editor);
    scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    cp.add(scrollPane, BorderLayout.CENTER);
    JPanel buttonPanel = new JPanel();
    JButton button = new JButton(new CloseAction("Close"));
    button.setMnemonic(KeyEvent.VK_C);
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(button);
    buttonPanel.add(Box.createHorizontalGlue());
    cp.add(buttonPanel, BorderLayout.SOUTH);
    pack();
  }
  private class CloseAction extends AbstractAction {
    CloseAction(String label) {
      super(label);
    }
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }
  private class MyHyperLinkListener implements HyperlinkListener {
    public void hyperlinkUpdate(HyperlinkEvent e) {
      if (e.getEventType() != EventType.ACTIVATED) {
        return;
      }
      URL url = e.getURL();
      try {
        Desktop.getDesktop().browse(url.toURI());
      }
      catch (Exception e1) {
        String msg = "Error while trying to open browser:" + e1;
        getLogger().info(msg);
      }
    }
  }
}    