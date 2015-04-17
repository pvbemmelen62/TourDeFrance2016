
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
import javax.swing.text.*;
import javax.swing.text.html.*;

import nl.xs4all.pvbemmel.sudoku.util.*;

@SuppressWarnings("serial")
public class UsageDialog extends JDialog {
  private static Logger getLogger() {
    return Logger.getLogger(UsageDialog.class.getName());
  }
  private Map<String,String> placeHolderMap;
  private JEditorPane editorPane;

  public UsageDialog(Map<String,String> placeHolderMap) {
    // If owner null, then will show default java icon; workaround:
    super(new JFrame("Hidden toplevel"));
    this.placeHolderMap = placeHolderMap;
    URL imgUrl = getClass().getResource("/images/logo-25-percent.png");
    ImageIcon imgIcon = new ImageIcon(imgUrl);
    Image iconImage = imgIcon.getImage();
    getOwner().setIconImage(iconImage);

    setTitle("Usage");
    setIconImage(iconImage); //: probably not used; but in future it may be.
    setModalityType(ModalityType.APPLICATION_MODAL);

    Container cp = getContentPane();
    URL aboutUrl = getClass().getResource("/html/usage.html");
    String text = null;
    try {
      text = StreamUtil.getAsString(aboutUrl.openStream());
    }
    catch (IOException e1) {
      text = "<html><body>Error while reading <code>usage.html</code>"
        + "</body></html>";
    }
    // It is advisable to use "XXXPlaceHolder" for each placeholder key.
    for(String key : this.placeHolderMap.keySet()) {
      text = text.replaceAll(key, this.placeHolderMap.get(key));
    }
    editorPane = new JEditorPane("text/html", text);
    editorPane.setEditable(false);
    editorPane.addHyperlinkListener(new MyHyperLinkListener());
    Color labelBg = UIManager.getColor("Label.background");
    getLogger().fine("UIManager.getColor(\"Label.background\") : "
      + labelBg);
    editorPane.setBackground(labelBg);
    editorPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    JScrollPane scrollPane = new JScrollPane(editorPane);
    scrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    editorPane.setCaretPosition(0);
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
      if(url==null) {
        String descript = e.getDescription();
        if(descript!=null && descript.startsWith("#")) {
          String id = descript.substring(1);
          HTMLDocument doc = (HTMLDocument)editorPane.getDocument();
          Element text = doc.getElement(id);
          if (text != null) {
            // JEditorPane: scroll to line but display at top | Oracle Community
            // https://community.oracle.com/thread/1362328?start=0&tstart=0
            editorPane.setCaretPosition(text.getStartOffset());
            Rectangle rect2;
            try {
              rect2 = editorPane.modelToView(text.getStartOffset());
              Rectangle rect3 = new Rectangle(rect2.x, rect2.y,
                  rect2.width, editorPane.getVisibleRect().height);
              editorPane.scrollRectToVisible(rect3);            
            }
            catch (BadLocationException e1) {
              getLogger().warning(""+e1);
            } 
          }
        }
      }
      else {
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
  public static class Test {
    public static void main(String[] args) {
      Test test = new Test();
      SwingUtilities.invokeLater(test.new GUIRunnable());
    }
    class GUIRunnable implements Runnable {
      public void run() {
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
        }
        URL imgUrl = getClass().getResource("/images/logo-25-percent.png");
        getLogger().fine("imgUrl: " + imgUrl);
        String imgSrc = imgUrl.toString();
        Font font = UIManager.getFont("Label.font");
        getLogger().fine("font: " + font);
        String fontFamily = font.getFamily();
        String fontSize = ""+font.getSize()+"pt";
        Map<String,String> map = new HashMap<String,String>();
        map.put("fontFamilyPlaceHolder", fontFamily);
        map.put("fontSizePlaceHolder", fontSize);
        map.put("imgSrcPlaceHolder", imgSrc);
        map.put("versionPlaceHolder", "1.0.0");
        UsageDialog dialog = new UsageDialog(map);
        dialog.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            System.exit(0);
          }
        });
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        System.exit(0);
      }
    }
  }
}    