package ch.emf.dao;

import java.io.File;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;

/**
 * Permet la connexion JPA à une unité de persistance. Cette classe est appelée
 * directement par la classe JpaDao (point d'entrée de la couche dao).
 * Il n'y a donc pas lieu de l'appeler spécifiquement pour la connexion.
 * Par contre, on peut appeler directement "check" ou "deconnect" pour
 * des opérations ciblées.
 *
 * @author Jean-Claude Stritt et Pierre-Alain Mettraux
 *
 * @opt nodefillcolor LemonChiffon
 * @has 1 - 1 Transaction
 */
public class JpaConnection implements JpaConnectionAPI {
  private final String DAOLAYER_VERSION = "DaoLayer V5.1.3 / 5.1.2017";
  private final String JPA2_PREFIX_KEY = "javax.persistence.jdbc";
  private String pu;
  private Properties props;
  private EntityManagerFactory emf;
  private EntityManager em;
  private Transaction tr;
  private String lastError;

  /**
   * Constructeur.
   */
  public JpaConnection() {
    pu = "";
    props = null;
    emf = null;
    em = null;
    tr = null;
    lastError = "";
  }

  /**
   * Retourne la version courante de cette couche d'intégration JPA.
   */
  @Override
  public String getVersion() {
    return DAOLAYER_VERSION;
  }

  /**
   * Pour une base de données locale, retourne le chemin absolu
   * où se trouve la base de données. Cela permet d'y stocker des photos
   * ou autres informations que l'on peut charger ensuite
   * en récupérant ce chemin.
   *
   * @param appPath le chemin absolu de l'application principale
   * @return le chemin absolu vers la base de données
   */
  @Override
  public String getDataBasePath(String appPath) {

    // retrouve le chemin relatif depuis le fichier de persistance JPA
    String relPath = (String)em.getProperties().get(JPA2_PREFIX_KEY + ".url");
    int i = relPath.lastIndexOf(File.separatorChar);
    relPath = relPath.substring(0, i);
    i = relPath.lastIndexOf(":");
    relPath = relPath.substring(i+1);

    // retrouve le chemin de l'application
    String dbPath = appPath;

    // traitement différencié si développement ou distribution
    i = dbPath.lastIndexOf(File.separatorChar);
    dbPath = dbPath.substring(0, i);
    if (dbPath.endsWith("build")) {
      i = dbPath.lastIndexOf(File.separatorChar);
      dbPath = dbPath.substring(0, i);
    }
    dbPath = dbPath + File.separatorChar + relPath + File.separatorChar;
    return dbPath;
  }

  /**
   * Retourne VRAI si la connexion à la base de donnnées est effective.
   */
  @Override
  public boolean isConnected() {
    return (em != null) && em.isOpen();
  }

  /**
   * Crée un objet avec les propriétés de la connexion. <br>
   * Attention, dans l'état actuel (V2.0 de la persistance JPA),
   * cela crée un objet pour JPA avec le préfixe "javax.persistence.jdbc.".
   *
   * @param dbDriver pilote JDBC
   * @param dbUrl URL vers la base de données
   * @param dbUser nom d'utlisateur de la base de données
   * @param dbPsw mot de passe pour accéder à la base
   * @return un objet de type "Properties"
   */
  @Override
  public Properties getConnectionProperties( String dbDriver, String dbUrl, String dbUser, String dbPsw ) {
    props = new Properties();
    props.put(JPA2_PREFIX_KEY + ".driver", dbDriver);
    props.put(JPA2_PREFIX_KEY + ".url", dbUrl);
    props.put(JPA2_PREFIX_KEY + ".user", dbUser);
    props.put(JPA2_PREFIX_KEY + ".password", dbPsw);
    return props;
  }

  /**
   * Connexion à la base de données en spécifiant des propriétés de connexion.
   * La variable "props" peut être créée avec "getProps".
   *
   * @param pu ID string de l'unité de persistance
   * @param props les propriétés pour la connexion.
   * @return une référence sur l'EntityManager si OK, autrement null
   */
  @Override
  public EntityManager connect( String pu, Properties props ) {
    this.pu = pu;
    this.props = props;
    lastError = "";
    if (!isConnected()) {
      try {
        if (props != null) {
          emf = Persistence.createEntityManagerFactory(pu, props);
        } else {
          emf = Persistence.createEntityManagerFactory(pu);
        }
        if (emf != null) {
          em = emf.createEntityManager();
          tr = new Transaction(em.getTransaction());
          Logger.debug(getClass(), em != null);
        }
      } catch (Exception e) {
        lastError = e.getMessage();
        Logger.error(getClass(), "*** "+lastError);
        deconnect();
      }
    }
    return em;
  }

  /**
   * Connexion à la base de données.
   *
   * @param pu ID string de l'unité de persistance
   * @return une référence sur l'EntityManager si OK, autrement null
   */
  @Override
  public EntityManager connect( String pu ) {
    return connect(pu, null);
  }

  /**
   * Connexion à la base de données en spécifiant les propriétés principales.
   *
   * @param pu nom de l'unité de persistance
   * @param dbDriver nom du driver JDBC
   * @param dbUrl URL permettant d'accéder à la BD
   * @param dbUser nom d'utilisateur de la BD
   * @param dbPsw mot de passe pour accéder à la BD
   *
   * @return une référence sur l'EntityManager si OK, autrement null
   */
  @Override
  public EntityManager connect(String pu, String dbDriver, String dbUrl, String dbUser, String dbPsw ) {
    props = getConnectionProperties(dbDriver, dbUrl, dbUser, dbPsw);
    return connect(pu, props);
  }

  /**
   * Permet de spécifier l'entity manager lorsque la connexion
   * est effectuée par une couche supérieure, par exemple par un serveur
   * d'application tel que GlassFish.
   *
   * @param em une référence sur l'EntityManager
   * @param ut une référence sur le gestionnaire de transactions
   */
  @Override
  public EntityManager connect( EntityManager em, UserTransaction ut ) {
    if (em != this.em) {
      this.em = em;
      this.tr = new Transaction(ut);
      Logger.debug(getClass(), true);
    }
    return this.em;
  }

  /**
   * Permet de spécifier l'entity manager lorsque la connexion
   * est effectuée par une couche supérieure telle que le framework
   * Play par exemple.
   *
   * @param em une référence sur l'EntityManager
   */
  @Override
  public EntityManager connect( EntityManager em ) {
    if (em != this.em) {
      this.em = em;
      this.tr = new Transaction(em.getTransaction());
      Logger.debug(getClass(), true);
    }
    return this.em;
  }

  /**
   * Déconnexion à la base de données.
   */
  @Override
  public void deconnect() {
    boolean ok = true;
    if (isConnected()) {
      if (tr.isActive() && !tr.isAutoCommit()) {
        Logger.error(getClass(), "Transaction active lors de la déconnexion !");
      }
      try {
        em.close();
        emf.close();
        em = null;
        emf = null;
        tr = null;
      } catch (Exception ex) {
        ok = false;
      }
      Logger.debug(getClass(), ok);
    }
  }

  /**
   * Permet une déconnexion-reconnexion instantannée.
   * Intéressant pour flusher les données pour Access par exemple.
   */
  @Override
  public EntityManager reconnect() {
    deconnect();
    return connect(pu, props);
  }

  /**
   * Retourne l'entity manager sous-jacent à la connexion.
   */
  @Override
  public EntityManager getEntityManager() {
    return em;
  }

  /**
   * Retourne une référence sur l'objet transaction.
   */
  @Override
  public Transaction getTransaction() {
    return tr;
  }

  /**
   * Teste si une base de donnée est atteignable avec les propriétés spécifiées.
   *
   * @param pu nom de l'unité de persistance
   * @param props propriétés qui supplantent les données de la "persistence unit"
   * @return TRUE si la base de données a pu être connectée.
   */
  @Override
  public boolean check( String pu, Properties props ) {
    em = connect(pu, props);
    boolean ok = isConnected();
    deconnect();
    return ok;
  }

  /**
   * Teste si une base de donnée est atteignable avec les propriétés spécifiées.
   *
   * @param pu nom de l'unité de persisance
   * @param dbDriver nom du driver JDBC
   * @param dbUrl URL permettant d'accéder à la BD
   * @param dbUser nom d'utilisateur de la BD
   * @param dbPsw mot de passe pour accéder à la BD
   *
   * @return TRUE si la base de donnée a pu être connectée.
   */
  @Override
  public boolean check(
          String pu, String dbDriver, String dbUrl, String dbUser, String dbPsw ) {
    em = connect(pu, dbDriver, dbUrl, dbUser, dbPsw);
    boolean ok = isConnected();
    deconnect();
    return ok;
  }

  /**
   * Retourne une chaîne de caractères avec la dernière erreur rencontrée.
   */
  @Override
  public String getLastError() {
    return lastError;
  }
}
