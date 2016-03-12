package ch.emf.dao.filtering;

import java.util.ArrayList;
import java.util.List;

/**
 * Une classe pour préparer une requête JPQL de type select. La base de la
 * requête est fournie au constructeur de la classe ce qui permet même des 
 * "inner join", chose que la classe "Search" standard ne permet pas, 
 * car elle est basée sur une seule table. Les méthodes de cette classe 
 * ajoutent ensuite les différents filtres (pour la clause WHERE avec 
 * des AND automatiques entre deux).<br>
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
 * @hidden
 */
public class Search2 {
  private int firstResult;
  private int maxResults;

  private String jpql;
  private List<Object> values;
  private int n;
  private boolean addAnd;

  public Search2(String jpql) {
    this.jpql = jpql;
    firstResult = -1; 
    maxResults = -1; 
    values = new ArrayList<>();
    n = 0;
    addAnd = this.jpql.toLowerCase().contains(" where ");
    if (!addAnd) {
      this.jpql += " where ";
    }
  }

  public void addFilter(String op, String fieldName, Object value) {
    if (value != null) {
      n++;
      values.add(value);
      if (addAnd) {
        jpql += " and ";
      }
      addAnd = true;
      jpql += fieldName + op + "?" + n;
    }
  }

  public void addFilterNoValue(String op, String fieldName) {
    if (addAnd) {
      jpql += " and ";
    }
    addAnd = true;
    jpql += fieldName + op;
  }

  public void addFilterEqual(String fieldName, Object obj) {
    addFilter(Operator.EQUAL.getOp(), fieldName, obj);
  }

  public void addFilterNotEqual(String fieldName, Object obj) {
    addFilter(Operator.NOT_EQUAL.getOp(), fieldName, obj);
  }

  public void addFilterLessThan(String fieldName, Object obj) {
    addFilter(Operator.LESS_THAN.getOp(), fieldName, obj);
  }

  public void addFilterLessOrEqual(String fieldName, Object obj) {
    addFilter(Operator.LESS_OR_EQUAL.getOp(), fieldName, obj);
  }

  public void addFilterGreatherThan(String fieldName, Object obj) {
    addFilter(Operator.GREATER_THAN.getOp(), fieldName, obj);
  }

  public void addFilterGreaterOrEqual(String fieldName, Object obj) {
    addFilter(Operator.GREATER_OR_EQUAL.getOp(), fieldName, obj);
  }

  public void addFilterBetween(String fieldName, Object obj1, Object obj2) {
    addFilter(" " + Operator.BETWEEN.getOp() + " ", fieldName, obj1);
    addFilter("", "", obj2);
  }

  public void addFilterLike(String fieldName, String s) {
    addFilter(" " + Operator.LIKE.getOp() + " ", fieldName, s);
  }

  public void addFilterIsNull(String fieldName) {
    addFilterNoValue(" " + Operator.IS_NULL.getOp(), fieldName);
  }

  public void addFilterIsNotNull(String fieldName) {
    addFilterNoValue(" " + Operator.IS_NOT_NULL.getOp(), fieldName);
  }

  public void addFilterIsEmpty(String fieldName) {
    addFilterNoValue(" " + Operator.IS_EMPTY.getOp(), fieldName);
  }

  public void addFilterIsNotEmpty(String fieldName) {
    addFilterNoValue(" " + Operator.IS_NOT_EMPTY.getOp(), fieldName);
  }
  
  public void addGroupByField(String fieldName) {
    jpql += " group by " + fieldName;
  }
  
  public void addHavingCondition( String condition ) {
    jpql += " having " + condition;
  }  

  public void addSortFields(String... fieldNames) {
    if (fieldNames != null) {
      jpql += " order by ";
      int i = 0;
      for (String fieldName : fieldNames) {
        if (i > 0) {
          jpql += ",";
        }
        jpql += fieldName;
        i++;
      }
    }
  }
  
  public int getFirstResult() {
    return firstResult;
  }

  public void setFirstResult( int firstResult ) {
    this.firstResult = firstResult;
  }

  public int getMaxResults() {
    return maxResults;
  }

  public void setMaxResults( int maxResults ) {
    this.maxResults = maxResults;
  }  

  public String getJpql() {
//    System.out.println("*** JPQL = " + jpql);
    return jpql;
  }

  public Object[] getParams() {
    Object[] params = values.toArray();
    return params;
  }

}
