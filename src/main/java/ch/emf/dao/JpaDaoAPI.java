package ch.emf.dao;

import ch.emf.dao.filtering.Search;
import ch.emf.dao.filtering.Search2;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.ejb.EJBContext;
import javax.persistence.EntityManager;

/**
 * Interface définissant une série de services proposées lors d'une gestion
 * de données basée sur le patron de conception "DAO" avec l'ORM JPA.
 *
 * @author Jean-Claude Stritt et Pierre-Alain Mettraux
 *
 * @opt nodefillcolor palegreen
 * @opt all
 * @note V5.36
 * @note 04.08.2016
 */
public interface JpaDaoAPI {

  /**
   * Ouvre la persistance en spécifiant un nom d'unité de persistance.
   * Ce nom est une identification contenue dans le fichier "persistence.xml".
   *
   * @param pu un nom d'unité de persistance JPA (ex: "MySqlPU")
   */
  void open(String pu); // standard

  /**
   * Ouvre la persistance en spécifiant par la variable "props" quelques
   * propriétés qui supplantent les informations contenues dans le fichier
   * persistence.xml.<br>
   * Ces propriétés doivent être initialisées avant l'appel à cette méthode:<br>
   * <pre>
   *   String prefixKey = "eclipselink.jdbc.";
   *   Properties props = new Properties();
   *   props.put(prefixKey + "driver", driver);
   *   props.put(prefixKey + "url", url);
   *   props.put(prefixKey + "user", user);
   *   props.put(prefixKey + "password", psw);
   * </pre>
   *
   * @param pu identifiant de l'unité de persistance
   * @param props une liste de propriétés pour la connexion
   */
  void open(String pu, Properties props); // standard avec properties

  /**
   * Ouvre la persistance en spécifiant les paramètres normalement fournis
   * par un "serveur d'application" tel que GlassFish et son support
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
   * @param em un objet "entity manager" de JPA
   * @param ctx le contexte EJB
   */
  void open(EntityManager em, EJBContext ctx); // Glassfish

  /**
   * Ouvre la persistance en spécifiant l'entity manager fourni
   * par une couche d'un serveur d'application tel que "Play".
   *
   * @param em l'entity-manager déjà lié à une base de données
   */
  void open(EntityManager em); // Play

  /**
   * Retourne true (vrai) si l'on est toujours connecté à la
   * couche de persistance et sa base de données.
   *
   * @return true si la connexion est déjà établie
   */
  boolean isOpen();

  /**
   * Retourne la dernière erreur de connexion rencontrée.
   *
   * @return une chaîne de caractère avec la dernière erreur rencontrée
   */
  String getOpenError();

  /**
   * Ferme la persistance avec la base connectée.
   */
  void close();




  /**
   * Retourne la version courante de cette couche d'intégration DAO-JPA.
   *
   * @return la version de l'implémentation JpaDao
   */
  String getVersion();

  /**
   * Retourne le chemin absolu où se trouve la base de données.
   * Cela permet d'y stocker des photos ou autres informations
   * que l'on peut charger ensuite en récupérant ce chemin.
   *
   * @param appPath le chemin vers l'application appelante
   * @return le chemin absolu vers la base de données
   */
  String getDataBasePath(String appPath);

  /**
   * Retourne une référence sur l'objet "entity manager" de JPA. Normalement,
   * cet objet est encapsulé dans cette couche et n'a pas à être appelé.
   *
   * @return une référence sur l'objet "entity manager" de JPA
   */
  EntityManager getEntityManager();

  /**
   * Retourne une instance sur le gestionnaire de transactions.
   * Normalement, cet objet est encapsulé dans cette couche et
   * n'a pas à être appelé directement.
   *
   * @return une référence sur l'objet qui gère les transactions
   */
  Transaction getTransaction();




  /**
   * Ajoute un objet dans la persistance.
   *
   * @param <E> un type générique pour une classe-entité
   * @param e l'objet à ajouter
   * @return =1 si l'objet a été créé dans la BD, =0 autrement
   */
  public <E> int create(E e);

  /**
   * Pour la classe-entité spécifiée, lit un objet d'après sa PK.
   * On peut aussi lui indiquer de rafraichir l'objet, ce qui est
   * nécessaire par exemple après une requête native SQL.
   *
   * @param <E> un type générique pour une classe-entité
   * @param cl une classe entité managée par JPA
   * @param pk une pk pour identifier l'objet à lire
   * @param refreshFlag TRUE pour rafraichir l'objet après la lecture
   * @return un objet lu et rafraichi éventuellement
   */
  <E> E read(Class<?> cl, Object pk, boolean refreshFlag);

  /**
   * Pour la classe-entité spécifiée, lit un objet d'après sa PK.
   *
   * @param <E> un type générique pour une classe-entité
   * @param cl une classe entité managée par JPA
   * @param pk une pk pour identifier l'objet à lire
   * @return un objet lu de la classe-entité spécifiée
   */
  <E> E read(Class<?> cl, Object pk);

  /**
   * Modifie un objet dans la persistance.
   *
   * @param <E> un type générique pour une classe-entité
   * @param e l'objet à modifier
   * @return =1 si l'objet a pu être modifié, =0 autrement
   */
  <E> int update(E e);

  /**
   * Pour la classe-entité spécifiée, supprime un objet de la persistance
   * d'après sa PK.
   *
   * @param cl une classe entité managée par JPA
   * @param pk une pk pour identifier l'objet à supprimer
   *
   * @return =1 si l'objet a pu être supprimé, =0 autrement
   */
  int delete(Class<?> cl, Object pk);

  /**
   * Pour la classe-entité spécifiée, retourne VRAI si un objet existe
   * dans la persistance.
   *
   * @param cl une classe entité managée par JPA
   * @param pk une pk pour identifier l'objet
   *
   * @return true si l'objet existe dans la persistance
   */
  boolean exists(Class<?> cl, Object pk);




  /**
   * Méthode de plus bas niveau pour retrouver un objet unique
   * d'après une requête jqpl et un tableau de valeurs paramètres
   *
   * @param <E> un type générique pour une classe-entité
   * @param jpql  une requête jpql déjà préparée, manque juste les paramètres
   * @param params un tableau de valeurs pour les paramètres de la requête
   * @return l'objet recherché
   */
  <E> E getSingleResult(String jpql, Object[] params);

  /**
   * Retrouve un objet unique d'une classe-entité donnée avec un critère
   * de recherche basée sur une égalité d'un attribut de cette classe avec
   * une valeur spécifiée.
   *
   * @param <E> un type générique pour une classe-entité
   * @param cl une classe entité managée par JPA
   * @param attr un nom d'attribut de la classe comme critère de recherche
   * @param value une valeur pour le critère de recherche
   * @return l'objet recherché
   */
  <E> E getSingleResult(Class<?> cl, String attr, Object value);

  /**
   * Retrouve un objet unique d'après un objet Search (spécification de critères
   * de recherche multiples).
   *
   * @param <E> un type générique pour une classe-entité
   * @param search un objet pour spécifier les critères de la recherche
   * @return l'objet recherché
   */
  <E> E getSingleResult(Search search);

  /**
   * Pour la classe-entité spécifiée, récupère une liste d'objets triés.
   *
   * @param <E> un type générique pour une classe-entité
   * @param cl une classe entité managée par JPA
   * @param sortFields les noms des propriétés de tri (séparés par des virgules)
   * @return une liste d'objets de la classe-entité spécifiée
   */
  <E> List<E> getList(Class<?> cl, String sortFields);

  /**
   * Pour la classe-entité spécifiée, récupère une liste d'objets filtrés et
   * triés, ceci d'après un seul critère basé sur une propriété et sa valeur.
   *
   * @param <E> un type générique pour une classe-entité
   * @param cl une classe entité managée par JPA
   * @param attr un nom d'attribut comme critère de filtrage
   * @param value une valeur pour le critère de filtrage
   * @param sortFields une liste des attributs de tri
   * @return une liste d'objets filtrée d'après les paramètres spécifiés
   */
  <E> List<E> getList(Class<?> cl, String attr, Object value, String sortFields);

  /**
   * Pour la classe-entité spécifiée, récupère une liste d'objets filtrés
   * et non triés, ceci d'après un seul critère basé sur un attribut de la
   * classe et une valeur pour cet attribut.
   *
   * @param <E> un type générique pour une classe-entité
   * @param cl une classe entité managée par JPA
   * @param attr un nom d'attribut comme critère de filtrage
   * @param value une valeur pour le critère de filtrage
   * @return une liste d'objets filtrés et non triés
   */
  <E> List<E> getList(Class<?> cl, String attr, Object value);

  /**
   * Récupère une liste d'objets en fournissant un objet de type "Search"
   * qui permet de stocker tous les paramètres nécessaires pour une recherche ciblée :<br>
   * - classe-entité; <br>
   * - choix des propriétés à récupérer (fields); <br>
   * - conditions de recherche (filters); <br>
   * - critères de tri (sorts); <br>
   * - limitation du nombre d'objets (maxResults)<br>
   *
   * @param <E> un type générique pour une classe-entité
   * @param search un objet pour spécifier les critères de la recherche
   * @return une liste d'objets filtrée et triée d'après l'objet "search"
   */
  <E> List<E> getList(Search search);


  /**
   * Récupère une liste d'objets en fournissant un objet de type Search2.
   * Cette objet contient directement une requête JPQL et la liste des
   * paramètres de cette requête.
   *
   * @param <E> un type générique pour une classe-entité
   * @param search un objet pour spécifier les critères de la recherche
   * @return une liste d'objets filtrée et triée d'après l'objet "search"
   */
  <E> List<E> getList(Search2 search);

  /**
   * Récupère une liste d'objets en effectuant une requête SQL native.
   *
   * @param <E> un type générique pour une classe-entité
   * @param sql une requête SQL native
   * @param params un tableau de paramètres pour satisfaire la requête
   * @param rsMapping un mapping pour le résultat
   * @return une liste d'objets filtrée d'après la requête
   */
  <E> List<E> getList(String sql, Object[] params, String rsMapping);

  /**
   * Récupère une liste d'objets en effectuant une requête SQL native.
   * Attention, il n'y a pas de mapping pour le résultat et on ne peut donc
   * pas caster avec une classe-entité.
   *
   * @param <E> un type générique pour une classe-entité
   * @param sql une requête SQL native
   * @param params un tableau de paramètres pour satisfaire la requête
   * @return une liste d'objets filtrée d'après la requête
   */
  <E> List<E> getList(String sql, Object[] params);




  /**
   * Permet de récupérer une liste d'agrégats de données
   * composés de colonnes préchoisies de type Field.
   *
   * @param <E> un type générique pour une classe-entité
   * @param search un objet de recherche.
   * @return une liste d'éléments de tableau
   */
  <E> List<E> getAggregateList(Search search);

  /**
   * Permet de récupérer une liste d'agrégats de données
   * composés de colonnes préchoisies.
   *
   * @param <E> un type générique pour une classe-entité
   * @param search un objet Search2 pour spécifier les critères de la recherche
   * @return une liste d'éléments de tableau
   */
  <E> List<E> getAggregateList(Search2 search);




  /**
   * Exécute une commande SQL native, principalement pour une
   * mise à jour (insert/update) ou un effacement (delete).
   *
   * @param sql la requête SQL native
   * @return le nombre d'enregistrements touchés
   */
  int executeCommand(String sql);

  /**
   * Exécute un script SQL contenu dans un fichier. Celui-ci peut être dans les
   * ressources de l'application ou totalement ailleurs si on spécifie le nom
   * du script avec son chemin complet. Le tableau facultatif d'objets permet de
   * spécifier un ancien nom de DB et un nouveau. Cela est utile lorsqu'on
   * désire créer une nouvelle BD basée sur un modèle.
   *
   * @param sqlScriptFileName un nom de fichier script avec des commandes sql
   * @param objects tableau facultatif avec 2 objets (nom des Dbs)
   * @return le nombre total d'enregistrements affectés par le script
   */
  int executeScript(String sqlScriptFileName, Object... objects);

  /**
   * Pour la classe-entité spécifiée, efface tous les objets managés.
   *
   * @param cl une classe entité managée par JPA
   * @return le nombre d'objets supprimés
   */
  int deleteAll(Class<?> cl);

  /**
   * Permet d'effacer le contenu global d'une liste de tables d'après
   * un tenant spécifié avec son nom et sa valeur.
   *
   * @param tenantName le nom d'un tenant
   * @param tenantId l'id de ce tenant (une pk)
   * @param tables une liste des tables-entités à effacer
   * @return le nombre d'enregistrements effacés
   */
  int deleteAll(String tenantName, int tenantId, String... tables);

  /**
   * Pour la classe-entité spécifiée, insert une liste globale d'objets.
   *
   * @param <E> un type générique pour une classe-entité
   * @param cl une classe entité managée par JPA
   * @param list une liste d'objets à insérer dans la persistance
   * @param resetPk TRUE s'il faut reconstruire les PK
   * @return le nombre d'objets insérés, =0 autrement
   */
  <E> int insertList(Class<?> cl, List<E> list, boolean resetPk);

  /**
   * Pour la classe-entité spécifiée, met à jour une liste globale d'objets.
   * Si un objet n'existe pas, il est rajouté.
   *
   * @param <E> un type générique pour une classe-entité
   * @param cl une classe entité managée par JPA
   * @param list une liste d'objets à modifier (ou à ajouter) dans la persistance
   * @return un tableau avec [0]= nb d'objets modifiés, [1]= nb d'objets ajoutés
   */
  <E> int[] updateList(Class<?> cl, List<E> list);

  /**
   * Détache tous les objets managés par JPA (liste en entrée-sortie).
   *
   * @param <E> un type générique pour une classe-entité
   * @param list une liste d'objets managés par JPA, puis non managés
   */
  <E> void detachList(List<E> list);

  /**
   * Rafraichit tous les objets d'une liste d'objets managés par JPA.
   *
   * @param <E> un type générique pour une classe-entité
   * @param list une liste d'objets managés par JPA
   */
  <E> void refreshList(List<E> list);




  /**
   * Pour la classe-entité spécifiée, retourne le nombre total d'objets.
   *
   * @param cl une classe entité managée par JPA
   * @return le nombre total d'objets
   */
  long count(Class<?> cl);

  /**
   * Pour la classe-entité spécifiée, retourne le nombre d'objets filtrés
   * d'après un seul critère fourni (propriété et valeur).
   *
   * @param cl une classe entité managée par JPA
   * @param attr un nom d'attribut comme critère de filtrage
   * @param value une valeur de filtrage pour cet attribut
   * @return le nombre d'objets pour le critère spécifié
   */
  long count(Class<?> cl, String attr, Object value);

/**
   * D'après la requête définie par un objet search, trouve le nombre
   * d'éléments.
   *
   * @param search un objet permettant le filtrage et le tri des données
   * @return le nombre d'éléments retournés basé sur l'objet "search"
   */
  long count(Search search);

  /**
   * Pour la classe-entité spécifiée et un champ donné,
   * retourne la valeur entière minimale de ce champ.
   *
   * @param cl une classe entité managée par JPA
   * @param fieldName le nom du champ où récupérer la valeur
   * @return la valeur minimale entière pour le champ spécifié
   */
  int getMinIntValue(Class<?> cl, String fieldName);

  /**
   * Pour la classe-entité spécifiée et un champ donné,
   * retourne la valeur entière maximale de ce champ.
   *
   * @param cl une classe entité managée par JPA
   * @param fieldName le nom du champ où récupérer la valeur
   * @return la valeur maximale entière pour le champ spécifié
   */
  int getMaxIntValue(Class<?> cl, String fieldName);

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
   * @return la valeur entière trouvée pour le filtrage spécifié
   */
  int getIntValue(Search search);




  /**
   * Supprime le contenu du cache JPA (normalement ne doit pas être appelé).
   */
  void clearCache();

  /**
   * Supprime tout objet encore managé par JPA.
   */
  void clear();

  /**
   * Rafraîchit toute donnée (dont les listes 1..N stockées) dans un
   * entity-bean.
   *
   * @param <E> un type générique pour une classe-entité
   * @param e l'objet à rafraîchir
   */
  <E> void refresh(E e);

  /**
   * Détache un objet de la persistence JPA.
   *
   * @param <E> un type générique pour une classe-entité
   * @param e un objet managé par JPA qu'il faut détacher
   */
  <E> void detach(E e);

  /**
   * Manage à nouveau par JPA un objet détaché.
   *
   * @param <E> un type générique pour une classe-entité
   * @param e un objet détaché qu'il faut à nouveau manager avec JPA
   */
  <E> void merge(E e);

  /**
   * Retourne TRUE si l'objet passé en paramètre est managé par JPA.
   *
   * @param <E> un type générique pour une classe-entité
   * @param e un objet d'une certaine classe-entité
   * @return TRUE si l'objet est managé par JPA
   */
  <E> boolean isMerged(E e);

  /**
   * Retourne TRUE si le premier élément d'une liste d'objets est managé par JPA
   * (sous-entendu les autres objets aussi).
   *
   * @param <E> un type générique pour une classe-entité
   * @param list une liste d'objets d'une certaine classe-entité
   * @return TRUE si le premier élément est managé
   */
  <E> boolean isMerged(List<E> list);




  /**
   * Pour la classe-entité spécifiée, retourne le nom de la PK.
   *
   * @param cl une classe-entité à introspecter
   * @return le nom de l'attribut avec "pk" dans le nom
   */
  String getPkName(Class<?> cl);

  /**
   * Pour la classe-entité spécifiée, retourne le type de la PK.
   *
   * @param cl une classe-entité à introspecter
   * @return le type de la PK
   */
  Type getPkType(Class<?> cl);

  /**
   * Pour la classe-entité spécifiée, retourne la valeur maximale de la PK
   * actuellement utilisée dans la table sous-jacente.
   *
   * @param cl une classe entité managée par JPA
   * @return la valeur maximale de la pk
   */
  Object getPkMax(Class<?> cl);




  /**
   * Pour la classe-entité spécifiée, récupère les informations suivantes :
   * classe, nom et type de la PK, table de séquence utilisée oui/non.
   *
   * @param cl une classe-entité à introspecter
   * @return les informations recherchées dans un objet EntityInfo
   */
  EntityInfo getEntityInfo(Class<?> cl);

  /**
   * Pour la classe-entité spécifiée, retourne par introspection une liste des
   * attributs présents dans cette classe.
   *
   * @param cl une classe-entité à introspecter
   * @return une liste des attributs de la classe
   */
  List<Field> getEntityFields(Class<?> cl);

  /**
   * Permet de retourner une map avec la liste des classes-entités
   * gérées par l'EntityManager.
   *
   * @return une map avec la liste des classes gérées par l'EntityManager
   */
  Map<Class<?>, EntityInfo> getEntitiesMap();


}
