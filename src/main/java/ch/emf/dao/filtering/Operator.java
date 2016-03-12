package ch.emf.dao.filtering;

/**
 * Classe énumération pour définir tous les opérateurs
 * disponibles lors d'un filtrage en JPQL (JPA).
 *
 * @author Jean-Claude Stritt
 *
 * @opt nodefillcolor LemonChiffon
 */
public enum Operator {
  EQUAL(0, "="),
  NOT_EQUAL(1, "<>"),
  LESS_THAN(2, "<"),
  GREATER_THAN(3, ">"),
  LESS_OR_EQUAL(4, "<="),
  GREATER_OR_EQUAL(5, ">="),
  LIKE(6, "LIKE"),
  ILIKE(7, "ILIKE"),
  BETWEEN(10, "BETWEEN"),
  IN(20, "IN"),
  NOT_IN(21, "NOT IN"),
  IS_NULL(30, "IS NULL"),
  IS_NOT_NULL(31, "IS NOT NULL"),
  IS_EMPTY(32, "IS EMPTY"),
  IS_NOT_EMPTY(33, "IS NOT EMPTY"),
  AND(100, "AND"),
  OR(101, "OR"),
  NOT(102, "NOT"),
  OPEN_PARENTHESIS(103, "("),
  CLOSE_PARENTHESIS(104, ")"),
  SOME(200, "SOME"),
  ALL(201, "ALL"),
  NONE(202, "NONE");
  protected int value;
  protected String op;

  private Operator( int value, String op ) {
    this.value = value;
    this.op = op;
  }

  /**
   * Retourne le code numérique de l'opération.
   * @return le code
   */
  public int getValue() {
    return value;
  }

  /**
   * Etablit le code numérique de l'opération (utilisé par le constructeur).
   * @param value la valeur à passer
   */
  public void setValue( int value ) {
    this.value = value;
  }

  /**
   * Retourne l'opérateur utilisé sous la forme d'une chaîne de caractères.
   * @return l'opérateur
   */
  public String getOp() {
    return op;
  }

  /**
   * Etablit l'opérateur (utilisé par le constructeur).
   * @param op l'opérateur à passer
   */
  public void setOp( String op ) {
    this.op = op;
  }

  /**
   * Méthode statique permettant de reconnaitre un opérateur dans une
   * chaîne de caractères.
   *
   * @param searchStr la chaîne de caractères à analyser
   * @return l'opérateur trouvé.
   */
  public static Operator getEnum( String searchStr ) {
    String s = searchStr.toUpperCase();
    if (s.contains(NOT_EQUAL.getOp())) {
      return NOT_EQUAL;
    } else if (s.contains(LESS_OR_EQUAL.getOp())) {
      return LESS_OR_EQUAL;
    } else if (s.contains(GREATER_OR_EQUAL.getOp())) {
      return GREATER_OR_EQUAL;
    } else if (s.contains(LESS_THAN.getOp())) {
      return LESS_THAN;
    } else if (s.contains(GREATER_THAN.getOp())) {
      return GREATER_THAN;
    } else if (s.contains(LIKE.getOp())) {
      return LIKE;
    } else {
      return EQUAL;
    }
  }

  /**
   * Méthode statique permettant de nettoyer une expression de filtrage de tout
   * opérateur contenu dans cette classe énumération.
   *
   * @param searchStr la chaîne à analyser
   * @return la chaîne nettoyée de ses opérateurs
   */
  public static String getCleanSearchStr( String searchStr ) {
    String s = searchStr; //.toUpperCase();
    for (Operator op : Operator.values()) {
      s = s.replace(op.getOp(), "");
    }
    return s.trim();
  }

}
