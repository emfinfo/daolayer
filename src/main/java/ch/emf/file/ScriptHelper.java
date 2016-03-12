package ch.emf.file;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classe de méthodes statiques d'aide pour gérer des scripts de type sql.
 *
 * @author jcstritt
 *
 * @hidden
 */
public class ScriptHelper {

  /**
   * Lire un fichier texte et retourner l'entier des lignes dans un seul objet
   * String. Le fichier est d'abord recherché dans les fichiers internes de
   * l'application puis avec le chemin spécifié.
   *
   * @param fileName            un nom de fichier
   * @param charset             l'encodage du fichier (normalement "UTF-8")
   * @param suppressEmptyLines  supprimer les lignes vierges
   * @param suppressSqlComments supprimer les lignes de commentaire sql ("-- ")
   * @return un objet contenant l'ensemble des lignes du fichier
   * @throws IOException l'exception levée en cas d'erreur
   */
  public static String readTextFile(String fileName, String charset,
          boolean suppressEmptyLines,
          boolean suppressSqlComments) throws IOException {
    String line;
    StringBuilder stringBuilder = new StringBuilder();
    String ls = System.getProperty("line.separator");

    // recherche et ouverture du fichier
    InputStream is;
    URL url = ClassLoader.getSystemResource(fileName);
    if (url != null) {
      is = url.openStream();
    } else {
      is = new FileInputStream(fileName);
    }

    // si ok, on lit les lignes du fichier
    if (is != null) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
      do {
        line = reader.readLine();
        if (line != null) {
          if (!suppressEmptyLines || !line.trim().isEmpty()) {
            if (!suppressSqlComments || !(line.startsWith("-- ") || line.startsWith("/*"))) {
              stringBuilder.append(line);
              stringBuilder.append(ls);
            }
          }
        }
      } while (line != null);
      is.close();
    }
    return stringBuilder.toString();
  }

  /**
   * Depuis un fichier de script, lit toutes les commandes SQL dans une liste.
   * Le fichier doit être de type UTF-8. Les retours à la ligne sont supprimés.
   * Le fichier peut être dans les ressources de l'application ou en dehors.
   *
   * @param sqlScriptFileName le nom d'un fichier de script
   * @return une liste de String contenant toutes les commandes SQL
   */
  public static List<String> readSqlScriptFile(String sqlScriptFileName) {
    List<String> cmdList = new ArrayList<String>();
    try {
      String sql = readTextFile(sqlScriptFileName, "UTF-8", true, true);
      String ls = System.getProperty("line.separator");
      sql = sql.replace(ls, "");
      String[] commands = sql.split(";");
      cmdList = Arrays.asList(commands);
    } catch (IOException ex) {
    }
    return cmdList;
  }

  /**
   * Idem à la méthode précédente, mais on peut spécifier une ancien nom de BD
   * qui sera modifié avec un nouveau nom. Ceci est utile pour créer un nouveau
   * nom de BD basé sur un ancien.
   *
   * @param sqlScriptFileName le nom d'un fichier de script
   * @param oldDbName un ancien nom de BD contenu dans le script
   * @param newDbName un nouveau nom de BD
   * @return une liste de String contenant toutes les commandes SQL
   */
  public static List<String> readSqlScriptFile(String sqlScriptFileName,
          String oldDbName,
          String newDbName) {
    List<String> cmdList = readSqlScriptFile(sqlScriptFileName);
    for (int i = 0; i < cmdList.size(); i++) {
      String cmd = cmdList.get(i);
      String c = cmd.toUpperCase();
      if (c.startsWith("DROP SCHEMA ")
              || c.startsWith("CREATE SCHEMA ")
              || c.startsWith("USE ")) {
        cmdList.set(i, cmd.replaceAll(oldDbName, newDbName));
      }
    }
    return cmdList;
  }
}
