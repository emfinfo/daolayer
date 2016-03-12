package ch.emf.dao;

import java.util.Properties;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

/**
 * Interface définissant les services de connexion et de déconnexion
 * à une base de données via JPA et son fichier de persistance.
 *
 * @author Jean-Claude Stritt et Pierre-Alain Mettraux
 *
 * @opt nodefillcolor palegreen
 */
public interface JpaConnectionAPI {

  /**
   * Retourne la version courante de cette couche d'accès aux données.
   * @return la version de JpaConnectionAPI
   */
  public String getVersion();

  /**
   * Pour une base de données locale, retourne le chemin absolu
   * où se trouve la base de données. Cela permet d'y stocker des photos
   * ou autres informations que l'on peut charger ensuite
   * en récupérant ce chemin.
   *
   * @param appPath le chemin absolu de l'application principale
   * @return le chemin absolu vers la base de données
   */
  public String getDataBasePath(String appPath);

  /**
   * Retourne VRAI si une connexion est actuellement engagée correctement.
   * @return true si la connexion a été faite
   */
  public boolean isConnected();

  /**
   * Crée un objet avec les propriétés de la connexion. <br>
   * Attention, dans l'état actuel crée un objet pour JPA "EclipseLink".
   *
   * @param dbDriver pilote JDBC
   * @param dbUrl URL vers la base de données
   * @param dbUser nom d'utlisateur de la base de données
   * @param dbPsw mot de passe pour accéder à la base
   *
   * @return un objet de type "Properties"
   */
  public Properties getConnectionProperties(
          String dbDriver, String dbUrl, String dbUser, String dbPsw );

  /**
   * Connexion à la base de données.
   *
   * @param pu ID string de l'unité de persistance
   * @return une référence sur l'entity-manager si OK, autrement null
   */
  public EntityManager connect( String pu );

  /**
   * Connexion à la base de données en spécifiant des propriétés de connexion.<br>
   * Les propriétés peuvent être créées avec "getConnectionProperties(...)".
   *
   * @param pu ID string de l'unité de persistance
   * @param props les propriétés pour la connexion.
   *
   * @return une référence sur l'entity-manager si OK, autrement null
   */
  public EntityManager connect( String pu, Properties props );

  /**
   * Connexion à la base de données en spécifiant les propriétés principales.
   *
   * @param pu nom de la "persistence unit"
   * @param dbDriver nom du driver JDBC
   * @param dbUrl URL permettant d'accéder à la BD
   * @param dbUser nom d'utilisateur de la BD
   * @param dbPsw mot de passe pour accéder à la BD
   *
   * @return une référence sur l'EntityManager si OK, autrement null
   */
  public EntityManager connect(
          String pu, String dbDriver, String dbUrl, String dbUser, String dbPsw );

  /**
   * Permet de spécifier l'entity-manager à cette couche lorsque la connexion   * est effectuée par une couche supérieure, par exemple par un serveur
   * d'application tel que GlassFish.
   *
   * @param em une référence sur l'entity-manager
   * @param ut une référence sur le gestionnaire de transactions
   * @return une référence sur l'entity-manager
   */
  public EntityManager connect( EntityManager em, UserTransaction ut );

  /**
   * Permet de spécifier l'entity-manager lorsque la connexion   * est effectuée par une couche supérieure telle que le framework
   * Play par exemple.
   *
   * @param em une référence sur l'entity-manager provenant de Play
   * @return la référence sur l'entity-manager connecté
   */
  public EntityManager connect( EntityManager em );

  /**
   * Déconnexion à la base de données.
   */
  public void deconnect();

  /**
   * Permet une déconnexion-reconnexion instantannée. Intéressant pour flusher les données
   * pour Access par exemple.
   *
   * @return une référence sur l'entity-manager reconnectée
   */
  public EntityManager reconnect();

  /**
   * Retourne l'entity-manager sous-jacent à la connexion.
   *
   * @return l'entity-manager en cours d'utilisation
   */
  public EntityManager getEntityManager();

  /**
   * Retourne une référence sur l'objet transaction en cours.
   *
   * @return une référence sur la transaction en cours
   */
  public Transaction getTransaction();

  /**
   * Teste si une base de donnée est atteignable avec les propriétés spécifiées.
   *
   * @param pu nom de l'unité de persistance
   * @param props propriétés qui supplantent les données de la "persistence unit"
   *
   * @return TRUE si la base de données est atteignable
   */
  public boolean check( String pu, Properties props );

  /**
   * Teste si une base de donnée est atteignable avec les propriétés spécifiées.
   *
   * @param pu nom de l'unité de persistance
   * @param dbDriver nom du driver JDBC
   * @param dbUrl URL permettant d'accéder à la BD
   * @param dbUser nom d'utilisateur de la BD
   * @param dbPsw mot de passe pour accéder à la BD
   *
   * @return TRUE si la base de donnée est atteignable
   */
  public boolean check(
          String pu, String dbDriver, String dbUrl, String dbUser, String dbPsw );

  /**
   * Retourne une chaîne de caractères avec la dernière erreur rencontrée.
   *
   * @return une chaîne avec la dernière erreur
   */
  public String getLastError();
}
