package ch.emf.dao.filtering;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Permet de définir un filtre pour la classe Search
 * dans le but de limiter les résultats d'une requête JPQL.
 * Ce code est un code simplifié d'un projet
 * "hibernate-generic-dao" disponible sur le net.<br>
 * Par exemple NOT, SOME, ALL et NONE ne sont pas implémentés.<br>
 * <br>
 * Exemples d'utilisation :<br>
 * <pre>
 *   Filter.equal("nom","Paul")
 *   Filter.and()
 *   Filter.greaterThan("age", 53)
 * </pre>
 *
 * @author Jean-Claude Stritt
 *
 * @opt nodefillcolor LemonChiffon
 */
public class Filter {
  protected Field field;
  protected Operator operator;
  protected Object value1;
  protected Object value2;

  // constructeurs
  public Filter( Field field, Operator operator, Object value1, Object value2 ) {
    this.field = field;
    this.operator = operator;
    this.value1 = value1;
    this.value2 = value2;
  }

  public Filter( Field field, Operator operator, Object value ) {
    this(field, operator, value, null);
  }

  public Filter( Field field, Operator operator ) {
    this(field, operator, null, null);
  }

  public Filter( Operator operator ) {
    this(null, operator, null, null);
  }

  // getters
  public Field getField() {
    return field;
  }

  public Operator getOperator() {
    return operator;
  }

  public Object getValue1() {
    return value1;
  }

  public Object getValue2() {
    return value2;
  }

  /**
   * Création d'un filtre de type "égalité" (=).
   * 
   * @param field le champ à filtrer
   * @param value une valeur de filtre pour ce champ
   * @return un filtre du type pré-cité
   */
  public static Filter equal( Field field, Object value ) {
    return new Filter(field, Operator.EQUAL, value);
  }

  /**
   * Création d'un filtre de type "n'est pas égal" (différent).
   * 
   * @param field le champ à filtrer
   * @param value une valeur de filtre pour ce champ
   * @return un filtre du type pré-cité
   */
  public static Filter notEqual( Field field, Object value ) {
    return new Filter(field, Operator.NOT_EQUAL, value);
  }

  /**
   * Création d'un filtre de type "plus petit que".
   * 
   * @param field le champ à filtrer
   * @param value une valeur de filtre pour ce champ
   * @return un filtre du type pré-cité
   */
  public static Filter lessThan(Field field, Object value) {
    return new Filter(field, Operator.LESS_THAN, value);
  }

  /**
   * Création d'un filtre de type "plus grand que".
   * 
   * @param field le champ à filtrer
   * @param value une valeur de filtre pour ce champ
   * @return un filtre du type pré-cité
   */
  public static Filter greaterThan( Field field, Object value ) {
    return new Filter(field, Operator.GREATER_THAN, value);
  }

  /**
   * Création d'un filtre de type "plus petit ou égal".
   * 
   * @param field le champ à filtrer
   * @param value une valeur de filtre pour ce champ
   * @return un filtre du type pré-cité
   */
  public static Filter lessOrEqual( Field field, Object value ) {
    return new Filter(field, Operator.LESS_OR_EQUAL, value);
  }

  /**
   * Création d'un filtre de type "plus grand ou égal".
   * 
   * @param field le champ à filtrer
   * @param value une valeur de filtre pour ce champ
   * @return un filtre du type pré-cité
   */
  public static Filter greaterOrEqual( Field field, Object value ) {
    return new Filter(field, Operator.GREATER_OR_EQUAL, value);
  }

  /**
   * Création d'un filtre de type "like".
   * 
   * @param field le champ à filtrer
   * @param value la valeur à filtrer de type String
   * @return un filtre du type pré-cité
   */
  public static Filter like( Field field, String value ) {
    return new Filter(field, Operator.LIKE, value);
  }

  /**
   * Création d'un filtre de type "ilike".
   * 
   * @param field le champ à filtrer
   * @param value la valeur à filtrer de type String
   * @return un filtre du type pré-cité
   */
  public static Filter ilike( Field field, String value ) {
    return new Filter(field, Operator.ILIKE, value);
  }

  /**
   * Création d'un filtre de type "between".
   * 
   * @param field le champ à filtrer
   * @param value1 la première valeur
   * @param value2 la dernière valeur
   * @return un filtre du type pré-cité
   */
  public static Filter between( Field field, Object value1, Object value2 ) {
    return new Filter(field, Operator.BETWEEN, value1, value2);
  }

	/**
	 * Création d'un filtre de type "in". Nécessite ici une collection
   * quelconque de valeurs.
   * 
   * @param field le champ à filtrer
   * @param coll une collection d'objets qui doivent exister
   * @return un filtre du type pré-cité
	 */
	public static Filter in( Field field, Collection<?> coll ) {
		return new Filter(field, Operator.IN, coll);
	}

	/**
	 * Création d'un filtre de type "in". Nécessite ici une série
   * quelconque de valeurs.
   * 
   * @param field le champ à filtrer
   * @param values un tableau d'objets qui doivent exister
   * @return un filtre du type pré-cité
	 */
	public static Filter in( Field field, Object... values ) {
		return new Filter(field, Operator.IN, values);
	}

	/**
	 * Création d'un filtre de type "not in". Nécessite ici une collection
   * quelconque de valeurs.
   * 
   * @param field le champ à filtrer
   * @param coll une collection d'objets qui ne doivent pas exister
   * @return un filtre du type pré-cité
	 */
	public static Filter notIn( Field field, Collection<?> coll ) {
		return new Filter(field, Operator.IN, coll);
	}

	/**
	 * Création d'un filtre de type "not in". Nécessite ici une série
   * quelconque de valeurs.
   * 
   * @param field le champ à filtrer
   * @param values un tableau d'objets qui ne doivent pas exister
   * @return un filtre du type pré-cité
	 */
	public static Filter notIn( Field field, Object... values ) {
		return new Filter(field, Operator.IN, values);
	}

	/**
	 * Création d'un filtre de type "est nul".
   * 
   * @param field le champ à filtrer
   * @return un filtre du type pré-cité
	 */
	public static Filter isNull( Field field ) {
		return new Filter(field, Operator.IS_NULL);
	}

	/**
	 * Création d'un filtre de type "n'est pas nul".
   * 
   * @param field le champ à filtrer
   * @return un filtre du type pré-cité
	 */
	public static Filter isNotNull( Field field ) {
		return new Filter(field, Operator.IS_NOT_NULL);
	}

	/**
	 * Création d'un filtre de type "est vide".
   * 
   * @param field le champ à filtrer
   * @return un filtre du type pré-cité
	 */
	public static Filter isEmpty( Field field ) {
		return new Filter(field, Operator.IS_EMPTY);
	}

	/**
	 * Création d'un filtre de type "n'est pas vide".
   * 
   * @param field le champ à filtrer
   * @return un filtre du type pré-cité
	 */
	public static Filter isNotEmpty(Field field) {
		return new Filter(field, Operator.IS_NOT_EMPTY);
	}

  /**
   * Création d'un filtre ET.
   * 
   * @return un filtre du type pré-cité
   */
  public static Filter and() {
    return new Filter(Operator.AND);
  }

  /**
   * Création d'un filtre OR.
   * 
   * @return un filtre du type pré-cité
   */
  public static Filter or() {
    return new Filter(Operator.OR);
  }

  /**
   * Création d'un filtre de type NOT.
   * 
   * @return un filtre du type pré-cité
   */
  public static Filter not() {
    return new Filter(Operator.NOT);
  }

  /**
   * Création d'un filtre de type parenthèse ouvrante.
   * 
   * @return un filtre du type pré-cité
   */
  public static Filter open_parenthesis() {
    return new Filter(Operator.OPEN_PARENTHESIS);
  }

  /**
   * Création d'un filtre de type parenthèse fermante.
   * 
   * @return un filtre du type pré-cité
   */
  public static Filter close_parenthesis() {
    return new Filter(Operator.CLOSE_PARENTHESIS);
  }

  /**
   * Retourne VRAI si l'opérateur nécessite une seule operande,
   * ce qui est le cas pour : <br>
   * <pre>
   *   EQUAL, NOT_EQUAL, LESS_THAN, LESS_OR_EQUAL,
   *   GREATER_THAN, GREATER_OR_EQUAL, LIKE
   * </pre>
   * @return true ou false
   */
  public boolean takesSingleValue() {
    return operator.getValue() <= 7;
  }

  /**
   * Retourne VRAI si l'opérateur nécessite deux operandes,
   * ce qui est le cas pour : <br>
   * <pre>
   *   BETWEEN
   * </pre>
   * @return true ou false
   */
  public boolean takesTwoValues() {
    return operator == Operator.BETWEEN;
  }


  /**
   * Retourne VRAI si l'opérateur nécessite une liste de valeurs,
   * ce qui est le cas pour : <br>
   * <pre>
   *   IN, NOT_IN
   * </pre>
   * @return true ou false
   */
  public boolean takesListOfValues() {
    return operator == Operator.IN || operator == Operator.NOT_IN;
  }

  /**
   * Retourve VRAI si une ou plusieurs valeurs sont nécessaires.
   * 
   * @return true ou false
   */
  public boolean takesValues() {
    return takesSingleValue() || takesTwoValues() || takesListOfValues();
  }

  /**
   * Retourne VRAI si l'opérateur ne nécessite aucune valeur,
   * ce qui est le cas pour : <br>
   * <pre>
   *   NULL, NOT_NULL, EMPTY, NOT_EMPTY
   * </pre>
   * @return true ou false
   */
  public boolean takesNoValue() {
    return operator.getValue() >= 30 && operator.getValue() <= 33;
  }

  /**
   * Retourne VRAI si l'opérateur nécessite un seul filtre à spécifier,
   * ce qui est le cas pour : <br>
   * <pre>
   *   NOT, SOME, ALL, NONE
   * </pre>
   * @return true ou false
   */
  public boolean takesSingleSubFilter() {
    return operator == Operator.NOT || operator.getValue() >= 200;
  }

  /**
   * Retourne VRAI si l'opérateur nécessite une suite de plusieurs filtres,
   * ce qui est le cas pour :<br>
   * <code>AND, OR</code>
   * @return true ou false
   */
  public boolean takesListOfSubFilters() {
    return operator == Operator.AND || operator == Operator.OR;
  }


  /**
   * Retourne VRAI si l'opérateur ne nécessite aucune propriété,
   * ce qui est le cas pour :<br>
   * <code>AND, OR, NOT</code>
   * @return true ou false
   */
  public boolean takesNoProperty() {
    return operator.getValue() >= 100 && operator.getValue() <= 104;
  }

  @Override
  public String toString() {
    String s = "";
    if (field != null) {
      s = s + field.getName() + " ";
    }
    s = s + operator.getOp();
    if (value1 != null) {
      s = s + " " + value1;
    }
    if (value2 != null) {
      s = s + " AND " + value2;
    }
    return s;
  }

}
