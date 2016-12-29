package ch.emf.dao;

import ch.emf.dao.filtering.Search;
import ch.emf.dao.filtering.Search2;
import ch.emf.file.ScriptHelper;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import javax.ejb.EJBContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;

/**
 * Couche DAO qui cache tout détail de la persistance à l'utilisateur de cette couche.
 * Elle permet de gérer n'importe quelle classe-entité associée à une base de données
 * relationnelle grâce à l'API JPA.
 *
 * @author Jean-Claude Stritt et Pierre-Alain Mettraux
 *
 * @opt nodefillcolor LemonChiffon
 * @depend - - - JpaConnectionAPI
 * @depend - - - Transaction
 * @depend - - - Logger
 * @depend - - - EntityInfo
 * @depend - - - Search
 * @depend - - - Search2
 */
public class JpaDao implements JpaDaoAPI {
  private final Class<?> clazz;
  private JpaConnectionAPI jpaConn = null;
  private EntityManager em = null;
  private Transaction tr;
  private final Map<Class<?>, EntityInfo> entitiesMap = new HashMap<>();

  public JpaDao() {
    clazz = this.getClass();
  }

  /**
   * Méthode privée pour retrouver toutes les "classes-entités" JPA.
   *
   * @param em l'entity manager
   */
  private void readEntities(EntityManager em) {
    if (em != null) {
      // System.out.println("readEntities *** size:" + em.getMetamodel().getEntities().size());
      for (EntityType<?> entityType : em.getMetamodel().getEntities()) {
        EntityInfo ei = new EntityInfo(entityType.getBindableJavaType());
        // System.out.println("ei: " + ei);
        // Logger.debug(clazz, "ei: " + ei);
        entitiesMap.put(ei.getEntityClass(), ei);
      }
    }
  }

  /**
   * Méthode privée pour effectuer un roolback après qu'une erreur ait été détectée.
   *
   * @param ex1 l'exception qui a provoqué l'erreur nécessitant le rollback
   * @param disp affiche ou pas l'erreur sur la console ou dans le fichier de log
   */
  private void rollbackAfterError(Exception ex1, boolean disp) {
    if (disp) {
      Logger.error(clazz, ex1.getMessage());
    }
    try {
      if (tr.isAutoCommit()) {
        tr.rollback();
      } else {
        tr.rollbackManualTransaction();
      }
    } catch (Exception ex2) {
      Logger.error(clazz, ex2.getMessage());
    }
  }

  /**
   * Méthode privée pour mettre à jour dans une table de séquence
   * la dernière valeur de PK.
   */
  private void updatePkMax(EntityInfo ei, Object pkMax) {
    if (ei.isTableSeqUsed()) {
      String sql = ei.buildUpdatePkMaxClause(pkMax);
      try {
        Query query = em.createNativeQuery(sql);
        query.executeUpdate();
        tr.commit();
      } catch (Exception ex1) {
        rollbackAfterError(ex1, true);
      }
    }
  }

  /**
   * Méthode privée pour construire une requête de type Query basée
   * sur une requête au format String et un tableau de valeurs en paramètre.
   *
   * @param jpql   une requête JQPL
   * @param params un tableau d'objets en paramètre pour la recherche
   *
   * @return une requête complète de type Query (JPA)
   */
  private Query getQuery(String jpql, Object[] params) {
    Query query = null;
    try {
      query = em.createQuery(jpql);
    } catch (Exception e) {
    }
    if (query != null && params != null && params.length > 0) {
      for (int i = 0; i < params.length; i++) {
        query.setParameter(i + 1, params[i]);
      }
    }
    return query;
  }

  /**
   * Méthode privée pour construire une requête de type Query basée
   * sur la classe, un attribut de recherche dans cette calsse et une valeur.
   *
   * @param cl    la classe pour identifier le type d'objet à récupérer
   * @param attr  un nom d'attribut pour la recherche
   * @param value la valeur de cet attribut
   *
   * @return une requête de type Query (JPA)
   */
  private Query getQuery(Class<?> cl, String attr, Object value) {
    Object params[] = {value};
    EntityInfo ei = getEntityInfo(cl);
    String jpql = ei.buildSelectClause() + ei.buildWhereClause(attr);
    return getQuery(jpql, params);
  }

  /**
   * Méthode privée pour construire une requête de type Query basée
   * sur des indications dans un objet Search.
   *
   * @param search l'objet avec toutes les informations de recherche.
   *
   * @return une requête de type Query (JPA)
   */
  private Query getQuery(Search search) {
    EntityInfo ei = getEntityInfo(search.getEntity());
    String jpql = ei.getSelectClause(search) + ei.getWhereClause(search) + ei.getOrderByClause(search);
    Object params[] = ei.getParams(search);
    return getQuery(jpql, params);
  }




  /**
   * Ouvre la persistance en spécifiant un nom d'unité de persistance.
   * Ce nom est une identification contenue dans le fichier "persistence.xml".
   *
   * @param pu un nom d'unité de persistance JPA (ex: "MySqlPU")
   */
  @Override
  public void open(String pu) {
    open(pu, null);
  }

  /**
   * Ouvre la persistance en spécifiant par la variable "props" quelques
   * propriétés
   * qui supplantent les informations contenues dans le fichier
   * persistence.xml.<br>
   * Ces propriétés doivent être initialisées avant l'appel à cette méthode
   * :<br>
   * <pre>
   *   String prefixKey = "eclipselink.jdbc.";
   *   Properties props = new Properties();
   *   props.put(prefixKey + "driver", driver);
   *   props.put(prefixKey + "url", url);
   *   props.put(prefixKey + "user", user);
   *   props.put(prefixKey + "password", psw);
   * </pre>
   *
   * @param pu    identifiant de l'unité de persistance
   * @param props une liste de propriétés pour la connexion
   */
  @Override
  public void open(String pu, Properties props) {
    if (jpaConn == null) {
      jpaConn = new JpaConnection();
    }
    if (!jpaConn.isConnected()) {
      em = jpaConn.connect(pu, props);
      tr = jpaConn.getTransaction();
      readEntities(em);
    }
  }

  /**
   * Permet de passer directement un objet "entity-manager" à la couche JpaDao
   * lorsque celui-ci est créé par une autre couche, par exemple par
   * un "serveur d'application" tel que GlassFish et son support
   * des transactions JTA.<br>

   * <br>
   * Pour utiliser correctement :<br>
   * - Avant une classe de type session bean, insérer :<br>
   * <code>&#064;TransactionManagement(TransactionManagementType.BEAN)</code><br>
   * - Après la récupération de l'entity manager (em), insérer encore :<br>
   * <code>&#064;Resource</code><br>
   * <code>private EJBContext context;</code><br>
   * - Passer ensuite les informations à cette méthode
   *
   * @param em  un objet "entity manager" de JPA
   * @param ctx le contexte EJB
   */
  @Override
  public void setEntityManager(EntityManager em, EJBContext ctx) {
    if (jpaConn == null) {
      jpaConn = new JpaConnection();
    }
    if (!jpaConn.isConnected()) {
      this.em = jpaConn.connect(em, ctx.getUserTransaction());
      tr = jpaConn.getTransaction();
      readEntities(this.em);
    }
  }

  /**
   * Permet de passer directement un objet "entity-manager" à la couche JpaDao,
   * lorsque celui-ci provient d'une autre couche, tel un serveur "Play framework".
   *
   * @param em un objet "entity-manager" de JPA déjà lié à une base de données
   */
  @Override
  public void setEntityManager(EntityManager em) {
    if (jpaConn == null) {
      jpaConn = new JpaConnection();
    }
    if (!jpaConn.isConnected()) {
      this.em = jpaConn.connect(em);
      tr = jpaConn.getTransaction();
      readEntities(this.em);
    }
  }

  /**
   * Retourne true (vrai) si l'on est toujours connecté à la
   * couche de persistance et sa base de données.
   *
   * @return true si la connexion est déjà établie
   */
  @Override
  public boolean isOpen() {
    return jpaConn.isConnected();
  }

  /**
   * Retourne la dernière erreur de connexion rencontrée.
   *
   * @return une chaîne de caractère avec la dernière erreur rencontrée
   */
  @Override
  public String getOpenError() {
    return jpaConn.getLastError();
  }

  /**
   * Ferme la couche de persistance avec la base connectée.
   */
  @Override
  public void close() {
    jpaConn.deconnect();
    em = jpaConn.getEntityManager();
  }




  /**
   * Retourne la version courante de cette couche d'intégration DAO-JPA.
   *
   * @return la version de l'implémentation JpaDao
   */
  @Override
  public String getVersion() {
    return jpaConn.getVersion();
  }

  /**
   * Retourne le chemin absolu où se trouve la base de données.
   * Cela permet d'y stocker des photos ou autres informations
   * que l'on peut charger ensuite en récupérant ce chemin.
   *
   * @param appPath le chemin vers l'application appelante
   * @return le chemin absolu vers la base de données
   */
  @Override
  public String getDataBasePath(String appPath) {
    return jpaConn.getDataBasePath(appPath);
  }

  /**
   * Retourne une référence sur l'objet "entity manager" de JPA. Normalement,
   * cet objet
   * est encapsulé dans cette couche et n'a pas à être appelé directement.
   *
   * @return une référence sur l'objet "entity manager" de JPA
   */
  @Override
  public EntityManager getEntityManager() {
    return jpaConn.getEntityManager();
  }

  /**
   * Retourne une instance sur le gestionnaire de transactions.
   * Normalement, cet objet est encapsulé dans cette couche et
   * n'a pas à être appelé directement.
   *
   * @return une référence sur l'objet qui gère les transactions
   */
  @Override
  public Transaction getTransaction() {
    return jpaConn.getTransaction();
  }




  /**
   * Ajoute un objet dans la persistance.
   *
   * @param e l'objet à ajouter
   *
   * @return =1 si l'objet a pu être créé, =0 autrement
   */
  @Override
  public <E> int create(E e) {
    int n = 0;
    try {
      em.persist(e);
      tr.commit();
      n = 1;
    } catch (Exception ex1) {
      rollbackAfterError(ex1, true);
    }
    return n;
  }

  /**
   * Pour la classe-entité spécifiée, lit un objet d'après sa PK.
   * On peut aussi lui indiquer de rafraichir l'objet pour disposer
   * de tous les objets liés à jour. On peut aussi lui indiquer de
   * tout de suite détacher l'objet de la persistance JPA.
   *
   * @param cl      une classe entité managée par JPA
   * @param pk      une pk pour identifier l'objet à lire
   * @param refresh TRUE pour rafraichir l'objet après la lecture
   * @param detach  TRUE pour rendre l'objet détaché après la lecture
   *
   * @return un objet lu depuis la BD et éventuellement rafraichi et détaché
   */
  @Override
  @SuppressWarnings("unchecked")
  public <E> E read(Class<?> cl, Object pk, boolean refresh, boolean detach) {
    try {
      Object e = em.find(cl, pk);
      if (e != null) {
        if (refresh) {
          refresh(e);
        }
        if (detach) {
          detach(e);
        }
      }
      return (E) e;
    } catch (Exception ex) {
      Logger.error(clazz, ex.getMessage());
      return null;
    }
  }

  /**
   * Modifie un objet dans la persistance.
   *
   * @param e l'objet à modifier
   *
   * @return -1=objet verrouillé momentanément, 0=objet modifié (problème), 1=ok objet modifié
   */
  @Override
  public <E> int update(E e) {
    int n = 0;
    try {
      em.merge(e);
      tr.commit();
      n = 1;
    } catch (OptimisticLockException ex1) {
      n = -1;
      rollbackAfterError(ex1, false);
    } catch (Exception ex2) {
      rollbackAfterError(ex2, true);
    }
    return n;
  }

  /**
   * Pour la classe-entité spécifiée, supprime un objet de la persistance
   * d'après sa PK.
   *
   * @param cl une classe entité managée par JPA
   * @param pk une pk pour identifier l'objet à supprimer
   *
   * @return -1=objet verrouillé momentanément, 0=non supprimé (problème), 1=ok objet supprimé
   */
  @Override
  public int delete(Class<?> cl, Object pk) {
    int n = 0;
    Object e = read(cl, pk, false, false);
    try {
      em.remove(e);
      tr.commit();
      n = 1;
    } catch (OptimisticLockException ex1) {
      n = -1;
      rollbackAfterError(ex1, false);
    } catch (Exception ex2) {
      rollbackAfterError(ex2, true);
    }
    return n;
  }

  /**
   * Pour la classe-entité spécifiée, retourne VRAI si un objet existe
   * dans la persistance.
   *
   * @param cl une classe entité managée par JPA
   * @param pk une pk pour identifier l'objet
   *
   * @return true si l'objet existe dans la persistance
   */
  @Override
  @SuppressWarnings("unchecked")
  public boolean exists(Class<?> cl, Object pk) {
    Object e = em.find(cl, pk);
    boolean ok = e != null;
    return ok;
  }




  /**
   * Méthode privée de plus bas niveau pour trouver un seul objet
   * basée sur une requête de type Query.
   *
   * @param query une requête de type Query (JPA)
   *
   * @return l'objet recherché
   */
  @SuppressWarnings("unchecked")
  private <E> E getSingleResult(Query query) {
    E result = null;
    try {
      result = (E) query.getSingleResult();
    } catch (NoResultException ex) {
    } catch (Exception ex) {
      Logger.error(clazz, ex.getMessage());
    }
    return result;
  }

  /**
   * Méthode de plus bas niveau pour retrouver un objet unique
   * d'après une requête jqpl et un tableau de valeurs paramètres
   *
   * @param jpql   une requête jpql déjà préparée, manque juste les paramètres
   * @param params un tableau de valeurs pour les paramètres de la requête
   *
   * @return l'objet recherché
   */
  @Override
  public <E> E getSingleResult(String jpql, Object[] params) {
    Query query = getQuery(jpql, params);
    return getSingleResult(query);
  }

  /**
   * Retrouve un objet unique d'une classe-entité donnée avec un critère
   * de recherche basée sur une égalité d'un attribut de cette classe avec
   * une valeur spécifiée.
   *
   * @param cl    une classe entité managée par JPA
   * @param attr  un nom d'attribut de la classe comme critère de recherche
   * @param value une valeur pour le critère de recherche
   *
   * @return l'objet recherché
   */
  @Override
  public <E> E getSingleResult(Class<?> cl, String attr, Object value) {
    Query query = getQuery(cl, attr, value);
    return getSingleResult(query);
  }

  /**
   * Retrouve un objet unique d'après un objet Search (spécification de critères
   * de recherche multiples).
   *
   * @param search un objet pour spécifier les critères de la recherche
   *
   * @return l'objet recherché de type
   */
  @Override
  public <E> E getSingleResult(Search search) {
    Query query = getQuery(search);
    return getSingleResult(query);
  }




  /**
   * Méthode privée de plus bas niveau pour récupérer une liste d'objets
   * d'après une requête JPA de type Query.
   *
   * @param query la requête encapsulée dans un objet de type Query de JPA
   * @param firstResult l'index du premier résultat escompté (-1 = pas précisé)
   * @param maxResults le nombre d'objets escomptés (-1 = pas précisé)
   *
   * @return une liste d'objets de l'entité spécifiée
   */
  @SuppressWarnings("unchecked")
  private <E> List<E> getList(Query query, int firstResult, int maxResults) {
    List<E> list = new ArrayList<>();
    if (firstResult >= 0) {
      query.setFirstResult(firstResult);
    }
    if (maxResults > 0) {
      query.setMaxResults(maxResults);
    }
    try {
      query.setHint("javax.persistence.cache.storeMode", "REFRESH");
      list = Collections.synchronizedList(query.getResultList());

      // détache la liste en démarrant une transaction bidon
      tr.beginManualTransaction();
      tr.commitManualTransaction();
      tr.finishManualTransaction();
//      System.out.println("JpaDao getList: list is managed = "+ isMerged(list));

    } catch (NoResultException ex) {
    } catch (Exception ex) {
      Logger.error(clazz, ex.getMessage());
    }
    return list;
  }

  /**
   * Méthode pour récupérer une liste d'objets d'après une requête jpql et
   * une liste de valeurs de paramètres pour la clause de recherche WHERE.
   *
   * @param jpql       une expression de type JPQL
   * @param params     une liste de valeurs pour les paramètres de la requête
   * @param maxResults une limitation des résultats si plus grand que zéro 0
   *
   * @return une liste d'objets de l'entité spécifiée
   */
//  @Override
//  public <E> List<E> getList(String jpql, Object[] params, int maxResults) {
//    Query query = getQuery(jpql, params);
//    return getList(query, -1, maxResults);
//  }

  /**
   * Pour la classe-entité spécifiée, récupère une liste d'objets triés.
   *
   * @param cl         une classe entité managée par JPA
   * @param sortFields les noms des propriétés de tri (séparés par des virgules)
   *
   * @return une liste d'objets de la classe-entité spécifiée
   */
  @Override
  public <E> List<E> getList(Class<?> cl, String sortFields) {
    EntityInfo ei = getEntityInfo(cl);
    String jpql = ei.buildSelectClause();
    if (!sortFields.isEmpty()) {
      jpql += ei.getOrderByClause(sortFields);
    }
    Query query = getQuery(jpql, null);
    return getList(query, -1, -1);
  }

  /**
   * Pour la classe-entité spécifiée, récupère une liste d'objets filtrés et
   * triés, ceci d'après un seul critère basé sur une propriété et sa valeur.
   *
   * @param cl         une classe entité managée par JPA
   * @param attr       un nom d'attribut comme critère de filtrage
   * @param value      une valeur pour le critère de filtrage
   * @param sortFields une liste des attributs de tri
   *
   * @return une liste d'objets filtrée d'après les paramètres spécifiés
   */
  @Override
  public <E> List<E> getList(Class<?> cl, String attr, Object value, String sortFields) {
    EntityInfo ei = getEntityInfo(cl);
    String jpql = ei.buildSelectClause() + ei.buildWhereClause(attr);
    if (!sortFields.isEmpty()) {
      jpql += ei.getOrderByClause(sortFields);
    }
    Object[] params = new Object[1];
    params[0] = value;
    Query query = getQuery(jpql, params);
    return getList(query, -1, -1);
  }

  /**
   * Pour la classe-entité spécifiée, récupère une liste d'objets filtrés
   * et non triés, ceci d'après un seul critère basé sur un attribut de la
   * classe et une valeur pour cet attribut.
   *
   * @param cl    une classe entité managée par JPA
   * @param attr  un nom d'attribut comme critère de filtrage
   * @param value une valeur pour le critère de filtrage
   *
   * @return une liste d'objets filtrés et non triés
   */
  @Override
  public <E> List<E> getList(Class<?> cl, String attr, Object value) {
    return getList(cl, attr, value, "");
  }

  /**
   * Pour la classe-entité spécifiée, récupère une liste d'objets en fournissant
   * encore un objet de type "Search" qui permet de stocker tous les paramètres
   * nécessaires pour une recherche ciblée :<br>
   * - choix des propriétés à récupérer (fields); <br>
   * - conditions de recherche (filters); <br>
   * - critères de tri (sorts); <br>
   * - premier objet à récupérer  (firstResult)<br>
   * - limitation du nombre d'objets (maxResults)
   *
   * @param search un objet pour spécifier les critères de la recherche
   *
   * @return une liste d'objets filtrée et triée d'après l'objet "search"
   */
  @Override
  public <E> List<E> getList(Search search) {
    Query query = getQuery(search);
    return getList(query, search.getFirstResult(), search.getMaxResults());
  }

  /**
   * Récupère une liste d'objets en fournissant un objet de type Search2.
   * Cette objet contient directement une requête JPQL et la liste des
   * paramètres de cette requête.
   *
   * @param search un objet pour spécifier les critères de la recherche
   *
   * @return une liste d'objets filtrée et triée d'après l'objet "search"
   */
  @Override
  public <E> List<E> getList(Search2 search) {
    Query query = getQuery(search.getJpql(), search.getParams());
    return getList(query, search.getFirstResult(), search.getMaxResults());
  }

  /**
   * Récupère une liste d'objets en effectuant une requête SQL native.
   *
   * @param sql       une requête SQL native
   * @param params    un tableau de paramètres pour satisfaire la requête
   * @param rsMapping un mapping pour le résultat
   *
   * @return une liste d'objets filtrée d'après la requête
   */
  @Override
  @SuppressWarnings("unchecked")
  public <E> List<E> getList(String sql, Object[] params, String rsMapping) {
    List<E> list = new ArrayList<>();
    Query query;
    try {
      if (rsMapping.isEmpty()) {
        query = em.createNativeQuery(sql);
      } else {
        query = em.createNativeQuery(sql, rsMapping);
      }
      if (params != null && params.length > 0) {
        for (int i = 0; i < params.length; i++) {
          query.setParameter(i + 1, params[i]);
        }
      }
      Logger.debug(clazz, sql);
      list = Collections.synchronizedList(query.getResultList());

      // détache la liste en démarrant une transaction bidon
      tr.beginManualTransaction();
      tr.commitManualTransaction();
      tr.finishManualTransaction();
//      System.out.println("JpaDao native getList: list is managed = "+ isMerged(list));

    } catch (Exception ex) {
      Logger.error(clazz, ex.getMessage() + " - " + sql);
    }
    return list;
  }

  /**
   * Récupère une liste d'objets en effectuant une requête SQL native.
   * Attention, il n'y a pas de mapping pour le résultat et on ne peut donc
   * pas caster avec une classe-entité.
   *
   * @param sql    une requête SQL native
   * @param params un tableau de paramètres pour satisfaire la requête
   *
   * @return une liste d'objets filtrée d'après la requête
   */
  @Override
  public <E> List<E> getList(String sql, Object[] params) {
    return getList(sql, params, "");
  }

  /**
   * Permet de récupérer une liste d'agrégats de données
   * composés de colonnes préchoisies de type Field.
   *
   * @param search un objet pour spécifier les critères de la recherche
   *
   * @return une liste d'éléments de tableau
   */
  @Override
  public <E> List<E> getAggregateList(Search search) {
    EntityInfo ei = getEntityInfo(search.getEntity());
    String select = ei.getSelectClause(search);
    String where = ei.getWhereClause(search);
    String groupby = ei.getGroupByClause(search);
    String having = ei.getHavingClause(search);
    String orderby = ei.getOrderByClause(search);
    String jpql = select + where + groupby + having + orderby;
    Object[] params = ei.getParams(search);
    Query query = getQuery(jpql, params);
    return getList(query, search.getFirstResult(), search.getMaxResults());
  }

  /**
   * Permet de récupérer une liste d'agrégats de données
   * composés de colonnes préchoisies.
   *
   * @param search un objet Search2 pour spécifier les critères de la recherche
   *
   * @return une liste d'éléments de tableau
   */
  @Override
  public <E> List<E> getAggregateList(Search2 search) {
    String jpql = search.getJpql();
    Object[] params = search.getParams();
    Query query = getQuery(jpql, params);
    return getList(query, search.getFirstResult(), search.getMaxResults());
  }

  /**
   * Exécute une commande SQL native pour une
   * mise à jour (insert/update) ou un effacement (delete).
   *
   * @param sql la requête SQL native
   *
   * @return le nombre d'enregistrements touchés
   */
  @Override
  public int executeCommand(String sql) {
    int n;
    try {
      Query query = em.createNativeQuery(sql);
      Logger.debug(clazz, sql);
      n = query.executeUpdate();
      tr.commit();
    } catch (Exception ex1) {
      n = 0;
      rollbackAfterError(ex1, true);
    }
    return n;
  }

  /**
   * Exécute un script SQL contenu dans un fichier. Celui-ci peut être dans les
   * ressources de l'application ou totalement ailleurs si on spécifie le
   * script avec son chemin complet. Les fichiers .sql générés par MySql
   * Workbench sont directement exécutables sans manipulation.
   *
   * @param sqlScriptFileName un nom de fichier script avec des commandes sql
   * @param objects tableau facultatif avec 2 objets (ancien nom de la BD, nouveau nom)
   *
   * @return le nombre total de commandes effectués par le script
   */
  @Override
  public int executeScript(String sqlScriptFileName, Object... objects) {
    List<String> commands;
    if (objects.length == 2) {
      commands = ScriptHelper.readSqlScriptFile(sqlScriptFileName,
              (String) objects[0], (String) objects[1]);
    } else {
      commands = ScriptHelper.readSqlScriptFile(sqlScriptFileName);
    }
    int n = 0;
    try {
      tr.beginManualTransaction();
      for (String sql : commands) {
        Query query = em.createNativeQuery(sql);
        Logger.debug(clazz, sql);
        int how = query.executeUpdate();
//        System.out.println(i + " " + sql + " how="+how);
        n = n + 1; // + how ;
      }
      tr.commitManualTransaction();
    } catch (Exception ex1) {
      Logger.error(clazz, ex1.getMessage());
      n = 0;
//      try {
//        tr.rollbackTransaction();
//      } catch (Exception ex2) {
//        Logger.error(clazz, ex2.getMessage());
//      }
    } finally {
      tr.finishManualTransaction();
    }
    return n;
  }

  /**
   * Pour la classe-entité spécifiée, efface tous les objets.
   *
   * @param cl une classe entité managée par JPA
   *
   * @return le nombre d'objets supprimés
   */
  @SuppressWarnings("unchecked")
  @Override
  public int deleteAll(Class<?> cl) {
    int n = 0;
    EntityInfo ei = getEntityInfo(cl);
    try {
      tr.beginManualTransaction();
      Query query = em.createQuery(ei.buildDeleteClause());
      n = query.executeUpdate();
      updatePkMax(ei, 0L);
      tr.commitManualTransaction();
    } catch (Exception ex1) {
      n = 0;
      rollbackAfterError(ex1, true);
    } finally {
      tr.finishManualTransaction();
    }
    return n;
  }

  /**
   * Permet d'effacer le contenu global d'une liste de tables d'après
   * un tenant spécifié avec son nom et sa valeur.
   *
   * @param tenantName le nom d'un tenant
   * @param tenantId l'id de ce tenant (une pk)
   * @param tables une liste des tables-entités à effacer
   *
   * @return le nombre d'enregistrements effacés
   */
  @Override
  public int deleteAll(String tenantName, int tenantId, String... tables) {
    int n = 0;
    String sql;
    Query query;
    try {
      tr.beginManualTransaction();
      for (String tableName : tables) {
        sql = "DELETE FROM " + tableName + " WHERE " + tenantName + "=" + tenantId;
        query = em.createNativeQuery(sql);
        n += query.executeUpdate();
      }
      tr.commitManualTransaction();
    } catch (Exception ex1) {
      n = 0;
      rollbackAfterError(ex1, true);
    } finally {
      tr.finishManualTransaction();
    }
    return n;
  }

  /**
   * Méthode privée pour récupérer la valeur de la PK d'un objet spécifié.
   */
  private Object getPk(Object e, Method m) {
    Object obj = null;
    try {
      m.setAccessible(true);
      obj = m.invoke(e);
    } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException ex) {
      Logger.error(clazz, ex.getMessage());
    }
    return obj;
  }

  /**
   * Méthode privée pour mettre à jour la valeur de la PK d'un objet spcéifié.
   */
  private void setPk(Object e, Method m, Object value) {
    try {
      m.setAccessible(true);
      m.invoke(e, value);
    } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException ex) {
    }
  }

  /**
   * Pour la classe-entité spécifiée, insert une liste globale d'objets.
   *
   * @param cl      une classe entité managée par JPA
   * @param list    une liste d'objets à insérer dans la persistance
   * @param resetPk TRUE s'il faut reconstruire les PK
   *
   * @return le nombre d'objets insérés, =0 autrement
   */
  @Override
  public <E> int insertList(Class<?> cl, List<E> list, boolean resetPk) {
    int n = 0;
    EntityInfo ei = getEntityInfo(cl);
//    Class type = ei.getEntityClass();
    if (count(ei) == 0 && list.size() > 0 && resetPk) {
      Method m = ei.findMethod("setPk");
      long i = ei.getPkInitialValue();
      long j = ei.getPkAllocationSize();
      for (E e : list) {
        setPk(e, m, i);
        i = i + j;
      }
    }
    try {
      tr.beginManualTransaction();
      for (E e : list) {
        em.persist(e);
      }
      updatePkMax(ei, getPkMax(ei));
      tr.commitManualTransaction();
      n = list.size();
    } catch (Exception ex1) {
      rollbackAfterError(ex1, true);
    } finally {
      tr.finishManualTransaction();
    }
    return n;
  }

/**
   * Pour la classe-entité spécifiée, met à jour une liste globale d'objets.
   * Si un objet n'existe pas, il est rajouté.
   *
   * @param <E> un type générique pour une classe-entité
   * @param cl une classe entité managée par JPA
   * @param list une liste d'objets à modifier (ou à ajouter) dans la persistance
   *
   * @return un tableau avec [0]= nb d'objets modifiés, [1]= nb d'objets ajoutés
   */
  @Override
  public <E> int[] updateList(Class<?> cl, List<E> list) {
    int n[] = new int[] {0, 0};
    EntityInfo ei = getEntityInfo(cl);
    Method m = ei.findMethod("getPk");
    try {
      tr.beginManualTransaction();
      for (E e : list) {
        if (exists(cl, getPk(e, m))) {
          em.merge(e);
          n[0]++;
        } else {
          em.persist(e);
          n[1]++;
        }
      }
      updatePkMax(ei, getPkMax(ei));
      tr.commitManualTransaction();
    } catch (Exception ex1) {
      n[0] = 0;
      n[1] = 0;
      rollbackAfterError(ex1, true);
    } finally {
      tr.finishManualTransaction();
    }
    return n;
  }

  /**
   * Détache tous les objets managés par JPA (liste en entrée-sortie).
   *
   * @param list une liste d'objets managés par JPA, puis non managés
   */
  @Override
  public <E> void detachList(List<E> list) {
    try {
      for (E e : list) {
        em.detach(e);
      }
    } catch (Exception ex1) {
    }
  }

  /**
   * Rafraichit tous les objets d'une liste d'objets en mémoire.
   *
   * @param list une liste d'objets managés par JPA
   */
  @Override
  public <E> void refreshList(List<E> list) {
    try {
      tr.beginManualTransaction();
      for (int i = 0; i < list.size(); i++) {
        list.set(i, em.merge(list.get(i)));
        em.refresh(list.get(i));
      }
      tr.commitManualTransaction();
    } catch (Exception ex1) {
      rollbackAfterError(ex1, true);
    } finally {
      tr.finishManualTransaction();
    }
  }

  /*
   * Méthode privée pour retrouver le nb d'objets d'après l'objet entity-info
   *
   * @return le nombre total d'objets pour l'entité
   */
  private long count(EntityInfo ei) {
    long l;
    try {
      Query query = em.createQuery(ei.buildCountClause());
      l = (Long) query.getSingleResult();
    } catch (Exception ex) {
      l = 0;
      Logger.error(clazz, ex.getMessage() + " " + ei.buildCountClause());
    }
    return l;
  }

  /**
   * Pour la classe-entité spécifiée, retourne le nombre total d'objets.
   *
   * @param cl une classe-entité
   *
   * @return le nombre total d'objets
   */
  @Override
  public long count(Class<?> cl) {
    EntityInfo ei = getEntityInfo(cl);
    return count(ei);
  }


  /**
   * Méthode privée pour récupérer une seule valeur (typiquement pour count, max, min).
   *
   * @param search un objet de recherche (limitée à du filtrage)
   *
   * @return un objet avec une valeur numérique
   */
  private Object getSingleValue(Search search ) {
    return getQuery(search).getSingleResult();
  }

  /**
   * Pour la classe-entité spécifiée, retourne le nombre d'objets filtrés
   * d'après un seul critère fourni (propriété et valeur).
   *
   * @param cl    une classe entité managée par JPA
   * @param attr  un nom d'attribut de la classe-entité comme critère de filtrage
   * @param value une valeur de filtrage pour cet attribut
   *
   * @return le nombre d'objets pour le critère spécifié
   */
  @Override
  public long count(Class<?> cl, String attr, Object value) {
    Search s = new Search(cl);
    s.addField("count(*)");
    s.addFilterEqual(attr, value);
    return (Long)getSingleValue(s);
  }

  /**
   * D'après la requête définie par un objet search, trouve le nombre
   * d'éléments.
   *
   * @param search un objet permettant le filtrage et le tri des données
   *
   * @return le nombre d'éléments retournés basé sur un objet "search"
   */
  @Override
  public long count(Search search) {
    Search s = new Search(search.getEntity());
    s.addField("count(*)");
    s.setFilters(search.getFilters());
    return (Long)getSingleValue(s);
  }

  /**
   * Pour la classe-entité spécifiée et un champ donné,
   * retourne la valeur entière minimale de ce champ.
   *
   * @param cl        une classe entité managée par JPA
   * @param fieldName le nom du champ où récupérer la valeur
   *
   * @return la valeur entière minimale pour le champ spécifié
   */
  @Override
  public int getMinIntValue(Class<?> cl, String fieldName) {
    Search s = new Search(cl);
    s.addFields("min(" + fieldName + ")");
    Object obj = getSingleValue(s);
    try {
      return (Integer)obj;
    } catch (Exception e) {
      return Integer.parseInt((String)obj);
    }
  }

  /**
   * Pour la classe-entité spécifiée et un champ donné,
   * retourne la valeur entière maximale de ce champ.
   *
   * @param cl        une classe entité managée par JPA
   * @param fieldName le nom du champ où récupérer la valeur
   *
   * @return la valeur entière maximale pour le champ spécifié
   */
  @Override
  public int getMaxIntValue(Class<?> cl, String fieldName) {
    Search s = new Search(cl);
    s.addFields("max(" + fieldName + ")");
    Object obj = getSingleValue(s);
    try {
      return (Integer)obj;
    } catch (Exception e) {
      return Integer.parseInt((String)obj);
    }
  }

  /**
   * On peut faire la même chose que les deux précédentes méthodes
   * getMinIntValue et getMaxIntValue, mais c'est à l'utilisateur de préparer
   * la requête avec la classe Search. Cela permet de trouver par exemple
   * un nombre maximale en dessous d'une limite (1000 par exemple).<br>
   * <br>
   * Exemple :<br>
   *   Search s = new Search(BonLivraisonTemp.class);<br>
   *   s.addFilterLessThan("code", 1000);<br>
   *   s.addFields("max(code)");<br>
   *
   * @param search un objet permettant le filtrage et le tri des données
   *
   * @return la valeur entière trouvée pour le filtrage spécifié
   */
  @Override
  public int getIntValue(Search search) {
    return (Integer)getSingleValue(search);
  }




  /**
   * Supprime le contenu du cache JPA.
   */
  @Override
  public void clearCache() {
    em.getEntityManagerFactory().getCache().evictAll();
  }

  /**
   * Supprime tout objet encore managé par JPA.
   */
  @Override
  public void clear() {
    em.clear();
  }

  /**
   * Rafraîchit toutes les données d'un entity-bean (dont les listes 1..N liées).
   *
   * @param e l'objet à rafraîchir
   */
  @Override
  public <E> void refresh(E e) {
    try {
      em.refresh(e);
      tr.commit();
    } catch (Exception ex1) {
      rollbackAfterError(ex1, true);
    }
  }

  /**
   * Détache un objet de la persistence JPA.
   *
   * @param e un objet managé par JPA qu'il faut détacher
   */
  @Override
  public <E> void detach(E e) {
    try {
      em.detach(e);
    } catch (Exception ex1) {
      Logger.error(clazz, ex1.getMessage() + " exception");
    }
  }

  /**
   * Manage à nouveau par JPA un objet détaché.
   *
   * @param e un objet détaché qu'il faut à nouveau manager avec JPA
   */
  @Override
  public <E> void merge(E e) {
    try {
      em.merge(e);
    } catch (Exception ex1) {
      Logger.error(clazz, ex1.getMessage() + " exception");
    }
  }

  /**
   * Retourne TRUE si l'objet passé en paramètre est managé par JPA.
   *
   * @param e un objet d'une certaine classe-entité
   *
   * @return TRUE si l'objet est managé par JPA
   */
  @Override
  public <E> boolean isMerged(E e) {
    return em.contains(e);
  }

  /**
   * Retourne TRUE si le premier élément d'une liste d'objets est managé par JPA
   * (sous-entendu les autres objets aussi).
   *
   * @param list une liste d'objets d'une certaine classe-entité
   *
   * @return TRUE si le premier élément est managé
   */
  @Override
  public <E> boolean isMerged(List<E> list) {
    boolean merged = false;
    if (list != null && list.size() > 0) {
      merged = em.contains(list.get(0));
    }
    return merged;
  }




  /**
   * Pour la classe-entité spécifiée, retourne le nom de la PK.
   *
   * @param cl une classe-entité à introspecter
   *
   * @return le nom de l'attribut avec "pk" dans le nom
   */
  @Override
  public String getPkName(Class<?> cl) {
    EntityInfo ei = getEntityInfo(cl);
    return ei.getPkName();
  }

  /**
   * Pour la classe-entité spécifiée, retourne le type de la PK.
   *
   * @param cl une classe-entité à introspecter
   *
   * @return le type de la PK
   */
  @Override
  public Type getPkType(Class<?> cl) {
    EntityInfo ei = getEntityInfo(cl);
    return ei.getPkType();
  }

  /**
   * Méthode privée pour retrouver la PK max d'après une information d'entité.
   *
   * @param ei un objet avec les informations principales de la classe-entité
   *
   * @return la PK maximale dans la table sous-jacente
   */
  private Object getPkMax(EntityInfo ei) {
    Object pk = null;
    try {
      Query query = em.createQuery(ei.buildMaxClause(ei.getPkName()));
      pk = query.getSingleResult();
    } catch (Exception ex) {
      Logger.error(clazz, ex.getMessage());
    }
    return pk;
  }

  /**
   * Pour la classe-entité spécifiée, retourne la valeur maximale de la PK
   * actuellement utilisée dans la table sous-jacente.
   *
   * @param cl une classe entité managée par JPA
   *
   * @return la valeur maximale de la pk
   */
  @Override
  @SuppressWarnings("unchecked")
  public Object getPkMax(Class<?> cl) {
    EntityInfo ei = getEntityInfo(cl);
    return getPkMax(ei);
  }




  /**
   * Pour la classe-entité spécifiée, récupère des informations importantes :
   * classe, nom et type de la PK, table de séquence utilisée oui/non.
   *
   * @param cl une classe-entité à introspecter
   *
   * @return les informations recherchées dans un objet EntityInfo
   */
  @Override
  public synchronized EntityInfo getEntityInfo(Class<?> cl) {
//    System.out.println("entitiesMap: " + entitiesMap.size());
    return entitiesMap.get(cl);
  }

  /**
   * Pour la classe-entité spécifiée, retourne par introspection une liste des
   * attributs présents dans cette classe.
   *
   * @param cl une classe-entité à introspecter
   *
   * @return une liste des attributs de la classe
   */
  @Override
  public List<Field> getEntityFields(Class<?> cl) {
    EntityInfo ei = getEntityInfo(cl);
    return ei.getFields();
  }

  /**
   * Permet de retourner une map avec la liste des classes-entités
   * gérées par l'EntityManager.
   *
   * @return une map avec la liste des classes gérées par l'EntityManager
   */
  @Override
  public Map<Class<?>, EntityInfo> getEntitiesMap() {
    return entitiesMap;
  }
}
