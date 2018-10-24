package ch.emf.dao.models;

import ch.emf.dao.filtering.Filter;
import ch.emf.dao.filtering.Operator;
import ch.emf.dao.filtering.Search;
import ch.emf.dao.filtering.Sort;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Permet de mémoriser les informations d'une "classe-entité" JPA, soit :<br>
 * - la classe elle-même; <br>
 * - le nom de sa PK; <br>
 * - le type de la PK. <br>
 * Par rapport aux informations mémorisées, cela permet aussi toutes sortes d'opérations
 * de construction d'une requête JPQL.
 *
 * @author jcstritt
 *
 * @opt nodefillcolor LemonChiffon
 */
public class EntityInfo {

  private Class<?> cl;
  private String pkName;
  private Type pkType;
  private boolean tableSeqUsed;

  /**
   * Constructeur.
   *
   * @param cl la classe "entity" à gérer
   */
  public EntityInfo(Class<?> cl) {
    this.cl = cl;
    findPkInfo();
  }

  /**
   * Retourne la classe de l'entité JPA.
   *
   * @return entity class
   */
  public Class<?> getEntityClass() {
    return cl;
  }

  /**
   * Retourne le nom de la PK.
   *
   * @return PK name
   */
  public String getPkName() {
    return pkName;
  }

  /**
   * Retourne le type de la PK.
   *
   * @return PK type
   */
  public Type getPkType() {
    return pkType;
  }

  /**
   * Retourne TRUE si une table de séquence est utilisée pour les PK.
   *
   * @return true or false
   */
  public boolean isTableSeqUsed() {
    return tableSeqUsed;
  }

  /**
   * Méthode privée pour retrouver par introspection les infos de la PK.
   */
  private void findPkInfo() {
    boolean ok1 = false;
    boolean ok2 = false;
    tableSeqUsed = false;
    for (Field fld : cl.getDeclaredFields()) {
      Annotation an1 = fld.getAnnotation(javax.persistence.Id.class);
      Annotation an2 = fld.getAnnotation(javax.persistence.TableGenerator.class);
      if (an1 != null) {
        pkName = fld.getName();
        pkType = fld.getType();
        ok1 = true;
      }
      if (an2 != null) {
        tableSeqUsed = true;
        ok2 = true;
      }
      if (ok1 && ok2) {
        break;
      }
    }
  }

  /**
   * Méthode privée pour retrouver par introspection une valeur du générateur de PK.
   */
  private String getTableGeneratorValue(String key) {
    String value = "";
    for (Field fld : cl.getDeclaredFields()) {
      Annotation an = fld.getAnnotation(javax.persistence.TableGenerator.class);
      if (an != null) {
        String a = an.toString();
        a = a.substring(a.indexOf("(") + 1);
        a = a.substring(0, a.indexOf(")"));
        String t[] = a.split(",");
        Properties pr = new Properties();
        for (String t1 : t) {
          String[] p = t1.trim().split("=");
          if (p.length > 0) {
            pr.put(p[0], (p.length > 1) ? p[1] : "");
          }
        }
        value = pr.getProperty(key);
        break;
      }
    }
    return value;
  }

  /**
   * Retourne la valeur initiale de la PK dans la table de séquence.
   *
   * @return valeur initiale de la PK
   */
  public long getPkInitialValue() {
    String s = getTableGeneratorValue("initialValue");
    return (s.length() == 0) ? 1 : Long.parseLong(s);
  }

  /**
   * Retourne le pas entre chaque PK spécifié dans la table de séquence.
   *
   * @return la taille du pas entre chaque PK
   */
  public long getPkAllocationSize() {
    String s = getTableGeneratorValue("allocationSize");
    return (s.length() == 0) ? 1 : Long.parseLong(s);
  }

  /**
   * Retourne la partie "FROM" d'une requête jpql.
   *
   * @return une chaine de caractères avec la clause FROM
   */
  public String buildFromClause() {
    return "FROM " + cl.getSimpleName() + " e";
  }

  /**
   * Retourne une clause SELECT avec le nom de la classe-entité.
   *
   * @return une chaîne de caractères avec la clause SELECT
   */
  public String buildSelectClause() {
    return "SELECT e " + buildFromClause();
  }

  /**
   * Retourne une clause DELETE avec le nom de la classe-entité.
   *
   * @return une chaîne de caractères avec la clause DELETE
   */
  public String buildDeleteClause() {
    return "DELETE " + buildFromClause();
  }

  /**
   * Retourne une clause WHERE simple avec une propriété dont la valeur sera encore à
   * compléter.
   *
   * @param prop une propriété avec une valeur encore à fournir
   * @return une chaîne de caractères avec la clause WHERE
   */
  public String buildWhereClause(String prop) {
    String s = "";
    if (!prop.isEmpty()) {
      s = " WHERE e." + prop + " = ?1";
    }
    return s;
  }

  /**
   * Retourne une clause SELECT avec l'une des functions d'agrégation AVG, SUM, MIN, MAX
   * ou COUNT.
   *
   * @param func la fonction d'agrégation
   * @param propName la propriété sur laquelle se fait l'agrégation
   * @return une chaîne de caractères avec requête JPQL d'aggrégation
   */
  public String buildAggregateFunction(String func, String propName) {
    String s = "SELECT " + func + "(e";
    if (!propName.isEmpty()) {
      s += "." + propName;
    }
    s += ") " + buildFromClause();
    return s;
  }

  /**
   * Retourne une clause COUNT avec le nom de la classe-entité.
   *
   * @return une chaîne de caractères avec requête JPQL pour compter
   */
  public String buildCountClause() {
    return buildAggregateFunction("COUNT", "");
  }

  /**
   * Retourne une clause MAX sur une propriété numérique et le nom de la classe-entité.
   *
   * @param propName le nom d'une propriété
   * @return une chaîne de caractères avec requête JPQL avec la fonction MAX
   */
  public String buildMaxClause(String propName) {
    return buildAggregateFunction("MAX", propName);
  }

  /**
   * Construit une clause de mise à jour de la PK maximale actuellement utilisée.
   *
   * @param pkMax la PK max actuellement utilisée
   * @return une chaine jpql de mise à jour de cette PK dans la table de séquence
   */
  public String buildUpdatePkMaxClause(Object pkMax) {
    return "UPDATE " + getTableGeneratorValue("table")
            + " SET " + getTableGeneratorValue("valueColumnName") + "="
            + String.valueOf(pkMax)
            + " WHERE " + getTableGeneratorValue("pkColumnName") + "='"
            + getTableGeneratorValue("pkColumnValue") + "'";
  }

  /**
   * Retrouve une méthode dans la classe-entité JPA traitée.
   *
   * @param method le nom d'une méthode à retrouver
   * @return l'objet Method recherché
   */
  public Method findMethod(String method) {
    for (Method m : cl.getDeclaredMethods()) {
      if (m.getName().toLowerCase().contains(method.toLowerCase())) {
        return m;
      }
    }
    return null;
  }

  /**
   * Retourne une liste des attributs présents dans la classe-entité traitée.<br>
   * Utile pour créer des combobox de filtrage. Ne prend pas en compte les attributs
   * commençant par fk ou pk, ainsi que les attributs marqués avec "static".
   *
   * @return une liste des attributs de la classe-entité
   */
  public List<Field> getFields() {
    String name;
    boolean nok;
//    List<Field> fields = Arrays.asList(cl.getDeclaredFields());
    List<Field> fields = new ArrayList<>(Arrays.asList(cl.getDeclaredFields()));
    	
    Iterator<Field> i = fields.iterator();
    while (i.hasNext()) {
      Field field = i.next();
      name = field.getName().toLowerCase();
      nok = name.startsWith("fk") || name.startsWith("pk") || (field.getModifiers() &  Modifier.STATIC) > 0;
      if (nok) {
        i.remove();
      }
    }    
    
//    List<Integer> toRemoveList = new ArrayList<>();
//    for (int i = 0; i < fields.size(); i++) {
//      Field field = fields.get(i);
//      name = field.getName().toLowerCase();
//      nok = name.startsWith("fk") || name.startsWith("pk") || name.startsWith("serialVersionUID");
//      if (nok) {
//        toRemoveList.add(i);
//      }
//    }
//    for (int i = 0; i < toRemoveList.size(); i++) {
//      fields.remove((int)toRemoveList.get(i));
//    }
    return fields;
  }

  /**
   * Retourne la clause "SELECT" d'une requête JPQL en se basant sur le contenu de
   * l'object "search" passé en paramètre.
   *
   * @param search l'objet de recherche qui spécifie les champs du select
   * @return la clause "select" en JPQL
   */
  public String getSelectClause(Search search) {
    String s;
    List<Field> fields = search.getFields();
    List<String> functions = search.getFunctions();
    if (fields.isEmpty() && functions.isEmpty()) {
      s = buildSelectClause();
    } else {
      s = "SELECT ";
      if (search.isDistinct()) {
        s += "DISTINCT ";
      }
      int cnt = 0;
      for (Field field : fields) {
        cnt++;
        if (cnt > 1) {
          s += ", ";
        }
        s += "e." + field.getName();
      }
      for (String func : functions) {
        cnt++;
        if (cnt > 1) {
          s += ", ";
        }
        if (func.contains("(*)")) {
          s += func.replace("*", "e");
        } else {
          s += func.replace("(", "(e.");
        }
      }
      s += " " + buildFromClause();
    }
    return s;
  }

  private String getFieldName(Filter filter) {
    Field fld = filter.getField();
    String name = fld.getName();
//    boolean c1 = fld.getAnnotation(javax.persistence.JoinColumn.class) != null;
//    boolean c2 = (filter.getOperator().getValue() >= 2
//            && filter.getOperator().getValue() <= 5) || filter.isTakesTwoValues();
//    boolean c3 = !name.equals(pkName);
//    if (c1 && c2 && c3) {
//      name += "." + pkName;
//    }
    return name;
  }

  /**
   * Retourne la clause "WHERE" d'un requete JPQL en se basant sur le contenu de l'objet
   * "Search" passé en paramètre.
   *
   * @param search l'objet de recherche qui limite la recherche
   * @return la clause "where" en JPQL
   */
  public String getWhereClause(Search search) {
    String q = "";
    List<Filter> filters = search.getFilters();
    if (!filters.isEmpty()) {
      int cnt = 1;
      q = q + " WHERE";
      for (Filter filter : filters) {
        if (filter.takesNoValue()) {
          q = q + " (e." + filter.getField().getName()
                  + " " + filter.getOperator().getOp() + ")";
        } else if (filter.takesSingleValue()) {
          q = q + " (e." + getFieldName(filter)
                  + " " + filter.getOperator().getOp() + " ?" + cnt + ")";
          cnt++;
        } else if (filter.takesTwoValues()) {
          q = q + " (e." + getFieldName(filter)
                  + " " + filter.getOperator().getOp()
                  + " ?" + cnt
                  + " " + Operator.AND.getOp()
                  + " ?" + (cnt + 1)
                  + ")";
          cnt += 2;
        } else if (filter.takesNoProperty()) {
          q = q + " " + filter.getOperator().getOp();
        }
      }
    }
    // System.out.println("*** q= " + q);
    return q;
  }

  /**
   * Retourne la clause "GROUP BY" d'une requête JPQL en utilisant la liste des champs
   * ajoutés dans un objet "Search" avec des "addGroupByField".
   *
   * @param search l'objet de recherche
   * @return la clause "GROUP BY" en jpql
   */
  public String getGroupByClause(Search search) {
    String s = "";
    List<Field> fields = search.getGroupbyFields();
    if (!fields.isEmpty()) {
      s = " GROUP BY ";
      int cnt = 0;
      for (Field field : fields) {
        cnt++;
        if (cnt > 1) {
          s += ", ";
        }
        s += "e." + field.getName();
      }
    }
    return s;
  }

  /**
   * Retourne la clause "HAVING" d'une requête JPQL en utilisant la liste des conditions
   * ajoutés dans un objet "Search" avec des "addHavingCondition".
   *
   * @param search l'objet de recherche
   * @return la clause "HAVING" en jpql
   */
  public String getHavingClause(Search search) {
    String s = "";
    List<Field> fields = search.getGroupbyFields();
    List<String> conditions = search.getHavingConditions();
    if (!fields.isEmpty() && !conditions.isEmpty()) {
      s = " HAVING ";
      int cnt = 0;
      for (String cond : conditions) {
        cnt++;
        if (cnt > 1) {
          s += ", ";
        }
        s += cond;
      }
    }
    return s;
  }

  /**
   * Retourne la clause "ORDER BY" d'une requête JPQL en utilisant les informations de tri
   * présent dans l'objet "Search" passé en paramêtre.
   *
   * @param search l'objet de recherche qui limite (filtre) la recherche
   * @return la clause "order by" en JPQL
   */
  public String getOrderByClause(Search search) {
    String q = "";
    List<Sort> sorters = search.getSorts();
    if (!sorters.isEmpty()) {
      int cnt = 0;
      for (Sort sort : sorters) {
        cnt++;
        if (cnt == 1) {
          q = q + " ORDER BY ";
        } else {
          q = q + ", ";
        }
        q = q + "e." + sort.getProperty();
        if (sort.isDesc()) {
          q = q + " DESC";
        }
      }
    }
    return q;
  }

  /**
   * Retourne la clause "ORDER BY" d'une requête JPQL en spécifiant directement la une
   * chaine de caractères avec la liste des champs de tri.
   *
   * @param sortFields les différents champs pour l'ordre de tri (ex: nom, prenom)
   * @return la clause "order by" en JPQL
   */
  public String getOrderByClause(String sortFields) {
    String q = "";
    if (sortFields.length() > 0) {
      String t[] = sortFields.split(",");
      if (t.length > 0) {
        q = q + " ORDER BY ";
        for (int i = 0; i < t.length; i++) {
          if (i > 0) {
            q = q + ", ";
          }
          q = q + "e." + t[i].trim();
        }
      }
    }
    return q;
  }

  /**
   * Retourne un tableau de paramètres pour un futur objet Query.<br>
   * Ces valeurs sont présentes dans les filtres ajoutés à un objet "Search".
   *
   * @param search l'objet de recherche
   * @return un tableau avec les valeurs des paramètres pour un objet "Query"
   */
  public Object[] getParams(Search search) {
    List<Object> params = new ArrayList<>();
    List<Filter> filters = search.getFilters();
    if (!filters.isEmpty()) {
      for (Filter filter : filters) {
        if (filter.takesTwoValues()) {
          params.add(filter.getValue1());
          params.add(filter.getValue2());
        } else if (filter.takesSingleValue()) {
          params.add(filter.getValue1());
        }
      }
    }
    return params.toArray();
  }

  /**
   * Surcharge de toString pour un meilleur affichage de l'objet.
   *
   * @return un string représentant l'objet
   */
  @Override
  public String toString() {
    return getEntityClass().getSimpleName()
            + ", " + getPkName()
            + ", " + getPkType()
            + ", " + isTableSeqUsed();
  }
}
