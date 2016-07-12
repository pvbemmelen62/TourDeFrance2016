package nl.xs4all.pvbemmel.letour;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;

import org.jdom2.*;
import org.jdom2.Attribute;
import org.jdom2.filter.*;
import org.jdom2.input.*;
import org.jdom2.xpath.*;
import org.w3c.tidy.*;
import org.xml.sax.*;

import static nl.xs4all.pvbemmel.letour.DocumentStuff.*;

public class TourDeFrance {
  private static Tidy tidy;

  private static Tidy getTidy() {
    if (tidy == null) {
      tidy = new Tidy();
      // tidy.setDocType("loose"); //: DOMBuilder().build(doc) generates
      // exception.
      tidy.setDocType("omit");
      tidy.setQuiet(true);
      tidy.setShowWarnings(false);
      tidy.setXHTML(false); // if true, then xpath expression require prefixes.
      tidy.setForceOutput(true);
      tidy.setTrimEmptyElements(false);
      tidy.setInputEncoding("utf8");
      tidy.setOutputEncoding("utf8");
    }
    return tidy;
  }

  /**
   * Parse teams page from www.letour.fr, and construct cyclistsIndex.html and
   * teamsIndex.html .
   * 
   * <pre>
   * In Firefox: File->Open Location->http://www.letour.fr
   * Click "TEAMS"
   * Click "START LIST"
   * File->Save As->Web Page, HTML only,
   *     choose filename data/starters-html-only.html
   * $ vi data/starters-html-only.html
   * Change &lt;html&gt; shit in line 2 (1?) , to &lt;html&gt;
   * </pre>
   */
  public static void main(String[] args) throws IOException, JDOMException,
      TransformerException, ParserConfigurationException, SAXException {
    File file = new File("data/starters-tidy.html");
    Document doc = null;
    if (file.exists()) {
      FileInputStream fis = new FileInputStream(file);
      doc = readDocument(fis);
      fis.close();
    }
    else {
      doc = getDoc("data/starters-html-only.html");
      printDocument(doc, new FileOutputStream(file));
    }
    // To understand the html structure, take a look at
    // data/starters-tidy-screendump.png
    //
    List<Team> teams = new ArrayList<Team>();
    XPathFactory xpfac = XPathFactory.instance();
    XPathExpression<Element> xp = xpfac.compile("//ul[@class=\"equipes\"]/li",
        Filters.element());
    List<Element> lis = xp.evaluate(doc);
    XPathExpression<Element> xp1 = xpfac.compile("./h2/a", Filters.element());
    for (Element li : lis) {
      Element anchor = xp1.evaluateFirst(li);
      String aText = anchor.getValue();
      String[] aParts = aText.split("/");
      String teamName = Text.normalizeString(aParts[0]);
      String teamCountry = Text.normalizeString(aParts[1]);

      Team team = new Team(teamName, teamCountry);
      teams.add(team);
      XPathExpression<Element> xp2 = xpfac.compile(
          "./span[@class=\"manager\"]", Filters.element());
      Element span = xp2.evaluateFirst(li);
      String managers = span.getTextNormalize();
      String[] mgrList = managers.substring("Sporting managers: ".length())
          .split("/");
      for (String mgr : mgrList) {
        team.addManager(mgr.trim());
      }
      XPathExpression<Element> xp3 = xpfac
          .compile("./table", Filters.element());
      Element table = xp3.evaluateFirst(li);
      XPathExpression<Element> xp4 = xpfac.compile("./tr", Filters.element());
      List<Element> trs = xp4.evaluate(table);
      for (Element tr : trs) {
        XPathExpression<Element> xp5 = xpfac.compile("./td[@class=\"bib\"]",
            Filters.element());
        Element td = xp5.evaluateFirst(tr);
        int riderNumber = Integer.parseInt(td.getTextNormalize());
        XPathExpression<Element> xp6 = xpfac.compile("./td[@class=\"rider\"]",
            Filters.element());
        td = xp6.evaluateFirst(tr);
        span = td.getChild("span");
        Attribute classAttr = span.getAttribute("class");
        String riderCountry = null;
        for (String ss : classAttr.getValue().split("\\s+")) {
          if (ss.startsWith("flag-")) {
            riderCountry = ss.substring("flag-".length());
            break;
          }
        }
        anchor = td.getChild("a");
        String riderName = anchor.getTextNormalize();
        Cyclist cyclist = new Cyclist(riderNumber, riderName, riderCountry);
        team.addCyclist(cyclist);
      }
    }
    Element root = new Element("teams");
    Document teamsDoc = new Document(root);
    for (Team team : teams) {
      Element teamNode = new Element("team");
      root.addContent(teamNode);
      Element teamNameNode = new Element("name");
      teamNode.addContent(teamNameNode);
      teamNameNode.setText(team.getName() + " (" + team.getCountry() + ")");
      Element mgrsNode = new Element("managers");
      teamNode.addContent(mgrsNode);
      for (String mgr : team.getManagers()) {
        Element mgrNode = new Element("manager");
        mgrsNode.addContent(mgrNode);
        mgrNode.setText(mgr);
      }
      for (Cyclist c : team.getCyclists()) {
        Element cNode = new Element("cyclist");
        teamNode.addContent(cNode);
        Element nameNode = new Element("name");
        nameNode.setText(c.getName());
        cNode.addContent(nameNode);
        Element numNode = new Element("number");
        numNode.setText("" + c.getNumber());
        cNode.addContent(numNode);
        Element countryNode = new Element("country");
        countryNode.setText(c.getCountry());
        cNode.addContent(countryNode);
      }
    }
    FileOutputStream fos = new FileOutputStream(new File("data/teams.xml"));
    printDocument(teamsDoc, fos);
    fos.close();
    //
    printTeamsIndex(teams);
    printCyclistsIndex(teams);
    printTeamsArray(teams);
  }
  private static void printTeamsIndex(List<Team> teams) throws JDOMException,
      IOException, TransformerException {
    TreeMap<String, Integer> teamsIndex = new TreeMap<String, Integer>();
    for (Team team : teams) {
      String name = team.getName();
      if (name.toLowerCase().startsWith("team ")) {
        name = name.substring("team ".length());
      }
      teamsIndex.put(name, team.getCyclists().get(0).getNumber() / 10);
    }
    Element html = new Element("html");
    Document doc = new Document(html);
    Element style = new Element("style");
    style.setAttribute("type", "text/css");
    html.addContent(style);
    style.setText("td {\n" + "  padding: 0 30 0 0;\n" + "}\n");
    Element body = new Element("body");
    html.addContent(body);
    Element table = new Element("table");
    body.addContent(table);
    for (String teamName : teamsIndex.keySet()) {
      Element tr = new Element("tr");
      table.addContent(tr);
      Element td = new Element("td");
      tr.addContent(td);
      td.setText(teamName);
      td = new Element("td");
      tr.addContent(td);
      td.setText("" + teamsIndex.get(teamName));
    }
    FileOutputStream fos = new FileOutputStream("data/teamsIndex.html");
    printDocument(doc, fos);
  }
  private static void printTeamsArray(List<Team> teams) throws JDOMException,
    IOException {
    
    FileWriter fw = new FileWriter("data/teamArray.java");
    fw.write("ArrayList<Object> teamArray = new ArrayList<Object>();\n");
    fw.write("teamArray = {\n");
    for(int i=0; i<teams.size(); ++i) {
      Team team = teams.get(i);
      String teamName = team.getName();
      if(teamName.toLowerCase().startsWith("team ")) {
        teamName = teamName.substring("team ".length());
      }
      fw.write("  new Integer(" + i + "), " + "\"" + teamName + "\",\n");
    }
    fw.write("}\n");
    fw.close();
  }

  private static String foreign = "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ";
  private static String european = "AAAAAAACEEEEIIIIDNOOOOOxOUUUUY_baaaaaaaceeeeiiiidnooooo÷øuuuuyþy";

  public static String toEuropean(String s) {
    StringBuilder sb = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); ++i) {
      char c = s.charAt(i);
      int ix = foreign.indexOf(c);
      sb.append(ix == -1 ? c : european.charAt(ix));
    }
    return sb.toString();
  }
  public static void printCyclistsIndex(List<Team> teams) throws JDOMException,
      IOException, TransformerException {
    TreeMap<String, ArrayList<Object>> cyclistsIndex = new TreeMap<String, ArrayList<Object>>();
    for (Team team : teams) {
      for (Cyclist c : team.getCyclists()) {
        String name = c.getName();
        String[] parts = name.split("\\s+");
        for (String part : parts) {
          ArrayList<Object> value = new ArrayList<Object>();
          value.add(part);
          value.add(c);
          value.add(team);
          String key = toEuropean(part);
          cyclistsIndex.put(key.toLowerCase() + " " + name, value);
        }
      }
    }
    Element html = new Element("html");
    Document doc = new Document(html);
    Element style = new Element("style");
    style.setAttribute("type", "text/css");
    html.addContent(style);
    style.setText(
      "td {\n"
    + "  padding: 0 10 0 0;\n"
    + "}\n"
    + ".cycnum {\n"
    + "  text-align: right;\n"
    + "  padding-right: 30;\n"
    + "}\n"
    + "div#topbar {\n"
    + "  position: fixed;\n"
    + "  top: 0;\n"
    + "  left: 0;\n"
    + "  border: 3px solid #8AC007;\n"
    + "  font-size: 200%;\n"
    + "  background: white;\n"
    + "}\n"
    );
    Element body = new Element("body");
    html.addContent(body);
    Element div = new Element("div");
    div.setAttribute("id", "topbar");
    body.addContent(div);
    int a = (int)'a';
    int z = (int)'z';
    for(int i=a; i<z; ++i) {
      Element anchor = new Element("a");
      char upper = Character.toUpperCase((char)i);
      anchor.setAttribute("href", "#"+upper+"_anchor");
      anchor.setText(""+upper);
      div.addContent(anchor);
    }
    //
    div = new Element("div");
    div.setText("");
    div.setAttribute("style", "height: 20px;");
    body.addContent(div);
    //
    Element table = new Element("table");
    body.addContent(table);
    {
      Element thead = new Element("thead");
      table.addContent(thead);
      thead.setAttribute("align", "left");
      Element tr = new Element("tr");
      thead.addContent(tr);
      for (String s : new String[] { "Key", "Name", "Number", "Country",
          "Team" }) {
        Element th = new Element("th");
        tr.addContent(th);
        th.setText(s);
      }
    }
    Character firstChar = null;
    for (ArrayList<Object> value : cyclistsIndex.values()) {
      String part = (String) value.get(0);
      Cyclist c = (Cyclist) value.get(1);
      Team t = (Team) value.get(2);
      Element tr = new Element("tr");
      table.addContent(tr);
      Element td = new Element("td");
      tr.addContent(td);
      Character fc = part.charAt(0);
      boolean sameFirstChar = firstChar!=null && firstChar.equals(fc);
      if(!sameFirstChar) {
        firstChar = fc;
        tr.setAttribute("id", firstChar + "_anchor");
      }
      td.setText(part);
      td = new Element("td");
      tr.addContent(td);
      td.setText(c.getName());
      td = new Element("td");
      tr.addContent(td);
      td.setText("" + c.getNumber());
      td.setAttribute("class", "cycnum");
      td = new Element("td");
      tr.addContent(td);
      td.setText(c.getCountry());
      td = new Element("td");
      tr.addContent(td);
      td.setText(t.getName());
    }
    FileOutputStream fos = new FileOutputStream("data/cyclistsIndex.html");
    printDocument(doc, fos);

  }
  public static Document getDoc(String fileName) throws IOException {
    return getDoc(new File(fileName));
  }
  public static org.jdom2.Document getDoc(File file) throws IOException {
    InputStream is = new FileInputStream(file);
    org.w3c.dom.Document doc = getTidy().parseDOM(is, null);
    // try {
    // printDocument(doc, System.out);
    // }
    // catch (TransformerException e) {
    // e.printStackTrace();
    // }
    return new DOMBuilder().build(doc);
  }
  public static void writeInputStreamToOutputStream(InputStream is,
      OutputStream os) throws IOException {
    while (true) {
      int i = is.read();
      if (i == -1) {
        break;
      }
      os.write(i);
    }
  }
//  public static String convertStreamToString(InputStream is) {
//    java.util.Scanner s = new java.util.Scanner(is);
//    s.useDelimiter("\\A");
//    String rv = s.hasNext() ? s.next() : "";
//    s.close();
//    return rv;
//  }
}
