package ch.emf.dao.filtering;

/**
 * Permet de définir une façon de trier pour la classe Search
 * lors de filtrage de données en JPQL.<br>
 * <br>
 * Ce code est un code simplifié d'un projet "hibernate-generic-dao"
 * disponible sur le net.<br>
 * <br>
 * Exemples d'utilisation :<br>
 * <pre>
 *   Sort.asc("nom");
 *   Sort.desc("localite");
 * </pre>
 *
 * @author Jean-Claude Stritt
 *
 * @opt nodefillcolor LemonChiffon
 */
public class Sort {
  protected String property;
  protected boolean desc;
  protected boolean ignoreCase;

  // constructeurs
  public Sort( String property, boolean desc, boolean ignoreCase ) {
    this.property = property;
    this.desc = desc;
    this.ignoreCase = ignoreCase;
  }

  public Sort( String property, boolean desc ) {
    this(property, desc, false);
  }

  public Sort( String property ) {
    this(property, false, false);
  }

  // getters
  public String getProperty() {
    return property;
  }

  public boolean isDesc() {
    return desc;
  }

  public boolean isIgnoreCase() {
    return ignoreCase;
  }

  // méthodes
  public static Sort asc( String property ) {
    return new Sort(property);
  }

  public static Sort asc( String property, boolean ignoreCase ) {
    return new Sort(property, ignoreCase);
  }

  public static Sort desc( String property ) {
    return new Sort(property, true);
  }

  public static Sort desc( String property, boolean ignoreCase ) {
    return new Sort(property, true, ignoreCase);
  }

  @Override
  public String toString() {
    String s = "";
    String sens = desc ? "desc" : "asc";
    if (property == null) {
      s = "null";
    } else {
      s = s + "`" + property + "`";
      s = s + " " + sens;
      if (ignoreCase) {
        s = s + " (ignore case)";
      }
    }
    return s;
  }
}
