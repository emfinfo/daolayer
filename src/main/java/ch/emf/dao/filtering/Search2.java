package ch.emf.dao.filtering;

import java.util.ArrayList;
import java.util.List;

/**
 * Une classe pour préparer une requête JPQL de type select. La base de la
 * requête est fournie au constructeur de la classe ce qui permet même des
 * "inner join", chose que la classe "Search" standard ne permet pas,
 * car elle est basée sur une seule table. Les méthodes de cette classe
 * ajoutent ensuite les différents filtres (pour la clause WHERE avec
 * des AND automatiques entre deux -modifiable-).<br>
 * <br>
 * Exemple d'utilisation :<br>
 * <pre>
 *   Search2 search = new Search2("select p from Pesee p join p.bonLivraison b");
 *   search.addFilterEqual("b.imprime", true);
 *   search.addFilterEqual("b.client", cl);
 *   search.addFilterEqual("b.chantier", ch);
 *   search.addFilterEqual("p.article", ar);
 *   search.addFilterBetween("b.datePesee", dateDebut, dateFin);
 *   search.addSortFields("b.datePesee", "b.code");
 * </pre>
 * <br>
 * Ensuite :<br>
 * <pre>
 *   pesees = dao.getList(search);
 * </pre>
 *
 * @author jcstritt
 *
 * @opt nodefillcolor LemonChiffon
 */
public class Search2 {
  private int firstResult;
  private int maxResults;

  private String jpql;
  private List<Object> values;
  private int n;
  private boolean addOp;
  private String defLogOp;

  /**
   * Constructeur 1.
   *
   * @param jpql une chaine de car. représentant las base de la requête JPQL
   * @param defLogOp opérateur par défaut entre les filtres ("and" ou "or")
   */
  public Search2(String jpql, String defLogOp) {
    this.jpql = jpql;
    this.defLogOp = defLogOp;
    firstResult = -1;
    maxResults = -1;
    values = new ArrayList<>();
    n = 0;
    addOp = this.jpql.toLowerCase().contains(" where ");
  }

  /**
   * Constructeur 2.<br>
   * L'opérateur par défaut entre les filtres est un "ET" ("and").
   *
   * @param jpql une chaine de car. représentant las base de la requête JPQL
   */
  public Search2(String jpql) {
    this(jpql, "and");
  }

  /**
   * Setter pour modifier l'opérateur logique par défaut ("and") ajouté
   * avant chaque filtre. S'il est null ou vide, aucun opérateur logique ne sera ajouté.
   *
   * @param defLogOp l'opérateur logique par défaut ajouté automatiquement avant chaque filtre
   */

  public void setDefaultLogicalOp(String defLogOp) {
    this.defLogOp = defLogOp;
  }

  /**
   * Ajoute un simple filtre basé sur un opérateur et une valeur.<br>
   * Il n'y a pas de test sur le contenu de la valeur du filtre et
   * il n'y pas non plus d'opérateur logique rajouté avant le filtre lui-même.
   *
   * @param op l'opérateur du filtre (ex: "=").
   * @param prop le nom de la propriété de filtrage
   * @param value la valeur de test
   */
  public void addSimpleFilter(String op, String prop, Object value) {
    n++;
    values.add(value);
    jpql += prop + op + "?" + n;
  }

  /**
   * Méthode privée pour ajouter un opérateur logique avant un filtre particuler.
   */
  private void addLogicalOp() {
    if (addOp) {
      if (defLogOp != null && !defLogOp.isEmpty()) {
        jpql += " " + defLogOp + " ";
      }
    } else {
      jpql += " where ";
    }
    addOp = true;
  }

  /**
   * Méthode privée pour ajouter un filtre sans spécification de valeur pour
   * les opérateurs tels que "is null", "is empty", etc.
   *
   * @param op l'opérateur du filtre (ex: "is null").
   * @param prop le nom de la propriété de filtrage
   */
  private void addFilterNoValue(String op, String prop) {
    addLogicalOp();
    jpql += prop + " " + op;
  }

  /**
   * N'ajoute un filtre que si la valeur spécifiée est non nulle.<br>
   * L'opérateur logique par défaut est rajoutée avant le filtre (sauf s'il est vide);
   *
   * @param op l'opérateur du filtre (ex: "=").
   * @param prop le nom de la propriété de filtrage
   * @param value la valeur de test
   */
  public void addFilter(String op, String prop, Object value) {
    if (value != null) {
      addLogicalOp();
      addSimpleFilter(op, prop, value);
    }
  }

  /**
   * Ajoute un filtre qui teste une égalité.
   *
   * @param prop le nom de la propriété de filtrage
   * @param value la valeur de test
   */
  public void addFilterEqual(String prop, Object value) {
    addFilter(Operator.EQUAL.getOp(), prop, value);
  }

  /**
   * Ajoute un filtre qui teste une double égalité avec un opérateur "ou" entre-deux.
   *
   * @param prop le nom de la propriété de filtrage
   * @param value1 la première valeur de test
   * @param value2 la deuxième valeur de test
   */
  public void addFilterEqual(String prop, Object value1, Object value2) {
    if (value1 != null && value2 != null) {
      addLogicalOp();
      jpql += "(";
      addSimpleFilter(Operator.EQUAL.getOp(), prop, value1);
      jpql += " or ";
      addSimpleFilter(Operator.EQUAL.getOp(), prop, value2);
      jpql += ")";
    }
  }

  /**
   * Ajoute un filtre qui teste une inégalité.
   *
   * @param prop le nom de la propriété de filtrage
   * @param value la valeur de test
   */
  public void addFilterNotEqual(String prop, Object value) {
    addFilter(Operator.NOT_EQUAL.getOp(), prop, value);
  }

  /**
   * Ajoute un filtre qui teste si des valeurs dans la BD sont plus
   * petites qu'une valeur de référence spécifiée.
   *
   * @param prop le nom de la propriété de filtrage
   * @param value la valeur de référence
   */
  public void addFilterLessThan(String prop, Object value) {
    addFilter(Operator.LESS_THAN.getOp(), prop, value);
  }

  /**
   * Ajoute un filtre qui teste si des valeurs dans la BD sont plus
   * petites ou égales à une valeur de référence spécifiée.
   *
   * @param prop le nom de la propriété de filtrage
   * @param value la valeur de référence
   */
  public void addFilterLessOrEqual(String prop, Object value) {
    addFilter(Operator.LESS_OR_EQUAL.getOp(), prop, value);
  }

  /**
   * Ajoute un filtre qui teste si des valeurs dans la BD sont plus
   * grandes q'une valeur de référence spécifiée.
   *
   * @param prop le nom de la propriété de filtrage
   * @param value la valeur de référence
   */
  public void addFilterGreatherThan(String prop, Object value) {
    addFilter(Operator.GREATER_THAN.getOp(), prop, value);
  }

  /**
   * Ajoute un filtre qui teste si des valeurs dans la BD sont plus
   * grandes ou égales à une valeur de référence spécifiée.
   *
   * @param prop le nom de la propriété de filtrage
   * @param value la valeur de référence
   */
  public void addFilterGreaterOrEqual(String prop, Object value) {
    addFilter(Operator.GREATER_OR_EQUAL.getOp(), prop, value);
  }

  /**
   * Ajoute un filtre qui teste si une valeur dans la DB se trouve
   * entre deux valeurs de références pour une propriété donnée.
   *
   * @param prop le nom de la propriété de filtrage
   * @param startValue la valeur de référence inférieure
   * @param endValue la valeur de référence supérieure
   */
  public void addFilterBetween(String prop, Object startValue, Object endValue) {
    addFilter(" " + Operator.BETWEEN.getOp() + " ", prop, startValue);
    addFilter("", "", endValue);
  }

  /**
   * Ajoute un filtre qui teste si des valeurs dans la BD ressemblent (like)
   * à une valeur de référence spécifiée pour une propriété donnée.
   *
   * @param prop le nom de la propriété de filtrage
   * @param value la valeur de référence
   */
  public void addFilterLike(String prop, String value) {
    addFilter(" " + Operator.LIKE.getOp() + " ", prop, value);
  }

  /**
   * Ajoute un filtre qui teste si les valeurs d'une propriété spécifiée sont nulles.
   *
   * @param prop le nom de la propriété de filtrage
   */
  public void addFilterIsNull(String prop) {
    addFilterNoValue(Operator.IS_NULL.getOp(), prop);
  }

  /**
   * Ajoute un filtre qui teste si les valeurs d'une propriété spécifiée sont non nulles.
   *
   * @param prop le nom de la propriété de filtrage
   */
  public void addFilterIsNotNull(String prop) {
    addFilterNoValue(Operator.IS_NOT_NULL.getOp(), prop);
  }

  /**
   * Ajoute un filtre qui teste si les valeurs d'une propriété spécifiée sont vides (empty).
   *
   * @param prop le nom de la propriété de filtrage
   */
  public void addFilterIsEmpty(String prop) {
    addFilterNoValue(Operator.IS_EMPTY.getOp(), prop);
  }

  /**
   * Ajoute un filtre qui teste si les valeurs d'une propriété spécifiée sont non vides (not empty).
   *
   * @param prop le nom de la propriété de filtrage
   */
  public void addFilterIsNotEmpty(String prop) {
    addFilterNoValue(Operator.IS_NOT_EMPTY.getOp(), prop);
  }

  /**
   * Ajoute une condition logique ET (and) après les filtres existants.
   */
  public void addFilterAnd() {
    jpql += " and ";
  }

  /**
   * Ajoute une condition logique OU (or) après les filtres existants.
   */
  public void addFilterOr() {
    jpql += " or ";
  }

  /**
   * Ajoute une parenthèse ouvrante à la clause WHERE en cours de création.
   */
  public void addFilterOpenParenthesis() {
    jpql += "(";
  }

  /**
   * Ajoute une parenthèse fermante à la clause WHERE en cours de création.
   */
  public void addFilterCloseParenthesis() {
    jpql += ")";
  }

  /**
   * Ajoute une expression de regroupement (groupe by) après les filtres déjà en cours.
   *
   * @param prop le nom de la propriété sur laquelle il faut regrouper
   */
  public void addGroupByField(String prop) {
    jpql += " group by " + prop;
  }

  /**
   * Ajoute une expression "qui contient" (having) après les filtres déjà en cours.
   *
   * @param condition la condition après le mot-clé "having"
   */
  public void addHavingCondition(String condition) {
    jpql += " having " + condition;
  }

  /**
   * Ajoute une ou plusieurs propriétés de tri après la requête en cours.<br>
   * Exemple: "NOM ASC", "NPA DESC".
   *
   * @param propNames un tableau de propriétés de tri optionnelles
   */
  public void addSortFields(String... propNames) {
    if (propNames != null) {
      jpql += " order by ";
      int i = 0;
      for (String fieldName : propNames) {
        if (i > 0) {
          jpql += ",";
        }
        jpql += fieldName;
        i++;
      }
    }
  }

  /**
   * Getter pour l'attribut "index du premier résultat" (firstResult).<br>
   * Cela permet la pagination des résultats à retrouver.
   *
   * @return un entier exprimant l'index du premier résultat
   */
  public int getFirstResult() {
    return firstResult;
  }

  /**
   * Setter pour l'attribut "index du premier résultat" (firstResult).<br>
   * Cela permet la pagination des résultats à retrouver.
   *
   * @param firstResult l'index du premier résutat à retourner
   */
  public void setFirstResult( int firstResult ) {
    this.firstResult = firstResult;
  }

  /**
   * Getter pour l'attribut "nombre maximal de résultats attendus" (maxResults).<br>
   * Cela permet la pagination des résultats à retrouver.
   *
   * @return un entier exprimant le nombre maximal de résultats attendus
   */
  public int getMaxResults() {
    return maxResults;
  }

/**
   * Setter pour l'attribut "nb maximal de résultats attendu" (maxResults).<br>
   * Cela permet la pagination des résultats à retrouver.
   *
   * @param maxResults le nombre maximal de résultats attendus dans une requête
   */
    public void setMaxResults( int maxResults ) {
    this.maxResults = maxResults;
  }

  /**
   * Permet de récupérer la requête jpql juste avant que les valeurs de paramètres ne soient injectées.
   *
   * @return la requête jpql en cours
   */
  public String getJpql() {
    return jpql;
  }

  /**
   * Un tableau avec les valeurs à injecter comme parmètres dans la requête jpql en cours.
   *
   * @return un tableau de valeurs (Objects)
   */
  public Object[] getParams() {
    Object[] params = values.toArray();
    return params;
  }

}
