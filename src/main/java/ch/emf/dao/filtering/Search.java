package ch.emf.dao.filtering;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Classe qui mémorise toutes les informations nécessaires à un filtrage de données.
 * Plusieurs filtres peuvent être définis, séparés par des opérateurs de type
 * AND, OR ou NOT. L'ordre de tri (sort) peut aussi être fixé. On peut aussi
 * limiter le nombre d'enregistrements retourné (setMaxResults).<br>
 * <br>
 * Exemple d'utilisation :<br>
 * <pre>
 *   Search search = new Search(Personne.class);
 *   addFilterEqual("nom", "Dupond");
 *   search.setMaxResults(10);
 *   search.addSortAsc("nom");
 *   search.addSortAsc("prenom");
 * </pre>
 * <br>
 * Ensuite :<br>
 * <pre>
 *   dao.getList(search);
 * </pre>
 *
 * @author Jean-Claude Stritt
 *
 * @opt nodefillcolor LemonChiffon
 * @depend - - - Filter
 * @depend - - - Sort
 */
public class Search {
  private Class<?> entity;
  private List<Field> entityFields;

  private List<Field> fields;
  private List<Filter> filters;
  private List<Sort> sorts;
  private List<String> functions;;
  private List<Field> groupbyFields;;
  private List<String> havingConditions;;

  private int firstResult;
  private int maxResults;
  private boolean distinct;

  public Search( Class<?> cl ) {
    this.entity = cl;
    entityFields = Arrays.asList(cl.getDeclaredFields());
    fields = new ArrayList<>();
    filters = new ArrayList<>();
    sorts = new ArrayList<>();
    functions = new ArrayList<>();
    groupbyFields = new ArrayList<>();
    havingConditions = new ArrayList<>();
    firstResult = -1;
    maxResults = -1;
    distinct = false;
  }


  /* GETTER AND SETTER */

  public Class<?> getEntity() {
    return entity;
  }

  public List<Field> getFields() {
    return fields;
  }

  public List<Filter> getFilters() {
    return filters;
  }

  public void setFilters(List<Filter> filters) {
    this.filters = filters;
  }

  public List<Sort> getSorts() {
    return sorts;
  }

  public void setSort(List<Sort> sorts) {
    this.sorts = sorts;
  }

  public List<String> getFunctions() {
    return functions;
  }

  public List<Field> getGroupbyFields() {
    return groupbyFields;
  }

  public List<String> getHavingConditions() {
    return havingConditions;
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

  public boolean isDistinct() {
    return distinct;
  }

  public void setDistinct( boolean distinct ) {
    this.distinct = distinct;
  }



  /***** FIELDS *****/

  private Field findField( String fieldName ) {
    Field f = null;
    for (Field field : entityFields) {
      if (field.getName().equalsIgnoreCase(fieldName)) {
        f = field;
        break;
      }
    }
    return f;
  }

  private void addFunction( String fieldName ) {
    String s = fieldName.toLowerCase();
    if (s.startsWith("sum(") || s.startsWith("avg(")
            || s.startsWith("max(") || s.startsWith("min(")
            || s.startsWith("count(")) {
      functions.add(fieldName);
    }
  }

  public void addField( Field field ) {
    if (field != null) {
      fields.add(field);
    }
  }

  public void addField( String fieldName ) {
    Field f = findField(fieldName);
    if (f == null) {
      addFunction(fieldName);
    } else {
      addField(f);
    }
  }

  public void addFields( Field... myFields ) {
    if (myFields != null) {
      for (Field field : myFields) {
        addField(field);
      }
    }
  }

  public void addFields( String... fieldNames ) {
    if (fieldNames != null) {
      for (String fieldName : fieldNames) {
        addField(fieldName);
      }
    }
  }




  /* **** FILTERS **** */

  public void addFilter( Filter filter ) {
    filters.add(filter);
  }

  public void addFilters( Filter... myFilters ) {
    if (myFilters != null) {
      for (Filter filter : myFilters) {
        addFilter(filter);
      }
    }
  }

  public void addFilterEqual( Field field, Object value ) {
    addFilter(Filter.equal(field, value));
  }

  public void addFilterEqual( String fieldName, Object value ) {
    addFilterEqual(findField(fieldName), value);
  }

  public void addFilterNotEqual( Field field, Object value ) {
    addFilter(Filter.notEqual(field, value));
  }

  public void addFilterNotEqual( String fieldName, Object value ) {
    addFilterNotEqual(findField(fieldName), value);
  }

  public void addFilterLessThan( Field field, Object value ) {
    addFilter(Filter.lessThan(field, value));
  }

  public void addFilterLessThan( String fieldName, Object value ) {
    addFilterLessThan(findField(fieldName), value);
  }

  public void addFilterGreaterThan( Field field, Object value ) {
    addFilter(Filter.greaterThan(field, value));
  }

  public void addFilterGreaterThan( String fieldName, Object value ) {
    addFilterGreaterThan(findField(fieldName), value);
  }

  public void addFilterLessOrEqual( Field field, Object value ) {
    addFilter(Filter.lessOrEqual(field, value));
  }

  public void addFilterLessOrEqual( String fieldName, Object value ) {
    addFilterLessOrEqual(findField(fieldName), value);
  }

  public void addFilterGreaterOrEqual( Field field, Object value ) {
    addFilter(Filter.greaterOrEqual(field, value));
  }

  public void addFilterGreaterOrEqual( String fieldName, Object value ) {
    addFilterGreaterOrEqual(findField(fieldName), value);
  }

  public void addFilterLike( Field field, String value ) {
    addFilter(Filter.like(field, value));
  }

  public void addFilterLike( String fieldName, String value ) {
    addFilterLike(findField(fieldName), value);
  }

  public void addFilterIlike( Field field, String value ) {
    addFilter(Filter.ilike(field, value));
  }

  public void addFilterIlike( String fieldName, String value ) {
    addFilterIlike(findField(fieldName), value);
  }

  public void addFilterBetween( Field field, Object startValue, Object endValue ) {
    addFilter(Filter.between(field, startValue, endValue));
  }

  public void addFilterBetween( String fieldName, Object startValue, Object endValue ) {
    addFilterBetween(findField(fieldName), startValue, endValue);
  }

  public void addFilterIn( Field field, Object... values ) {
    addFilter(Filter.in(field, values));
  }

  public void addFilterIn( String fieldName, Object... values ) {
    addFilterIn(findField(fieldName), values);
  }

  public void addFilterNotIn( Field field, Object... values ) {
    addFilter(Filter.notIn(field, values));
  }

  public void addFilterNotIn( String fieldName, Object... values ) {
    addFilterNotIn(findField(fieldName), values);
  }

  public void addFilterIsNull( Field field ) {
    addFilter(Filter.isNull(field));
  }

  public void addFilterIsNull( String fieldName ) {
    addFilterIsNull(findField(fieldName));
  }

  public void addFilterIsNotNull( Field field ) {
    addFilter(Filter.isNotNull(field));
  }

  public void addFilterIsNotNull( String fieldName ) {
    addFilterIsNotNull(findField(fieldName));
  }

  public void addFilterIsEmpty( Field field ) {
    addFilter(Filter.isEmpty(field));
  }

  public void addFilterIsEmpty( String fieldName ) {
    addFilterIsEmpty(findField(fieldName));
  }

  public void addFilterIsNotEmpty( Field field ) {
    addFilter(Filter.isNotEmpty(field));
  }

  public void addFilterIsNotEmpty( String fieldName ) {
    addFilterIsNotEmpty(findField(fieldName));
  }
 
  public void addFilterAnd() {
    addFilter(Filter.and());
  }

  public void addFilterOr() {
    addFilter(Filter.or());
  }

  public void addFilterOpenParenthesis() {
    addFilter(Filter.open_parenthesis());
  }

  public void addFilterCloseParenthesis() {
    addFilter(Filter.close_parenthesis());
  }

  private Date getDate(int day, int month, int year) {

    // récupère un objet calendrier et le remplit avec la date spécifiée
    Calendar cal = Calendar.getInstance();

    // met les infos de temps à zéro
    cal.set(Calendar.DAY_OF_MONTH, day);
    cal.set(Calendar.MONTH, month-1);
    cal.set(Calendar.YEAR, year);

    // met les infos de temps à zéro
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

      // retourne la date
    Date date = cal.getTime();
    return date;
  }

  public void addFilterByOp( Field field, Operator op, Object value ) {
    int year = 0;
    if (field.getType().equals(Date.class)) {
      try {
        year = Integer.parseInt(value.toString());
      } catch (NumberFormatException e) {
      }
    }
    if (year == 0) {
      switch (op) {
        case NOT_EQUAL:
          addFilterNotEqual(field, value);
          break;
        case LESS_OR_EQUAL:
          addFilterLessOrEqual(field, value);
          break;
        case GREATER_OR_EQUAL:
          addFilterGreaterOrEqual(field, value);
          break;
        case LESS_THAN:
          addFilterLessThan(field, value);
          break;
        case GREATER_THAN:
          addFilterGreaterThan(field, value);
          break;
        case LIKE:
          addFilterLike(field, value.toString() + "%");
          break;
        default:
          if (field.getType().equals(String.class)) {
            addFilterLike(field, value.toString() + "%");
          } else {
            addFilterEqual(field, value);
          }
      }
    } else { // traite le cas des années pour la recherche
      Date d1 = getDate(1, 1, year);
      Date d2 = getDate(31, 12, year);
      switch (op) {
        case NOT_EQUAL:
          addFilterLessThan(field, d1);
          addFilterOr();
          addFilterGreaterThan(field, d2);
          break;
        case LESS_OR_EQUAL:
          addFilterLessOrEqual(field, d2);
          break;
        case GREATER_OR_EQUAL:
          addFilterGreaterOrEqual(field, d1);
          break;
        case LESS_THAN:
          addFilterLessThan(field, d1);
          break;
        case GREATER_THAN:
          addFilterGreaterThan(field, d2);
          break;
        default:
          addFilterBetween(field, d1, d2);
      }
    }
  }

  public void addFilterByOp( String fieldName, Operator op, Object value ) {
    addFilterByOp(findField(fieldName), op, value);
  }



  /* GROUP BY FIELDS */

  public void addGroupByField( Field field ) {
    if (field != null) {
      groupbyFields.add(field);
    }
  }

  public void addGroupByField( String fieldName ) {
    addGroupByField(findField(fieldName));
  }

  public void addGroupByField( Field... myFields ) {
    if (myFields != null) {
      for (Field field : myFields) {
        addGroupByField(field);
      }
    }
  }

  public void addGroupByField( String... fieldNames ) {
    if (fieldNames != null) {
      for (String fieldName : fieldNames) {
        addGroupByField(fieldName);
      }
    }
  }


  /* HAVING CONDITIONS */

  public void addHavingCondition( String condition ) {
    if (!condition.isEmpty()) {
      havingConditions.add(condition.replace("(*)", "(e)"));
    }
  }

  public void addHavingConditions( String... conditions ) {
    if (conditions != null) {
      for (String cond : conditions) {
        addHavingCondition(cond);
      }
    }
  }


  /* SORT */

  public void addSort( Sort sort ) {
    if (sort != null) {
      sorts.add(sort);
    }
  }

  public void addSorts( Sort... mySorts ) {
    if (mySorts != null) {
      for (Sort sort : mySorts) {
        addSort(sort);
      }
    }
  }

  public void addSort( String fieldName, boolean desc, boolean ignoreCase ) {
    if (fieldName != null) {
      addSort(new Sort(fieldName, desc, ignoreCase));
    }
  }

  public void addSort( String fieldName, boolean desc ) {
    addSort(fieldName, desc, false);
  }

  public void addSortAsc( String fieldName ) {
    addSort(fieldName, false, false);
  }

  public void addSortAsc( String... fieldNames ) {
    for (String fieldName : fieldNames) {
      addSort(fieldName, false, false);
    }
  }

  public void addSortAsc( String fieldName, boolean ignoreCase ) {
    addSort(fieldName, false, ignoreCase);
  }

  public void addSortDesc( String fieldName ) {
    addSort(fieldName, true, false);
  }

  public void addSortDesc( String... fieldNames ) {
    for (String fieldName : fieldNames) {
      addSort(fieldName, true, false);
    }
  }

  public void addSortDesc( String fieldName, boolean ignoreCase ) {
    addSort(fieldName, true, ignoreCase);
  }

}
