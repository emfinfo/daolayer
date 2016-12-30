package ch.emf.dao;

import javax.persistence.EntityTransaction;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

/**
 * Gestion des transactions. Cette classe est instanciée automatiquement au moment
 * de la connexion JPA.<br>
 * <br>
 * Le mode par défaut est le mode "autocommit" où chaque modification de type
 * CRUD  sera validée automatiquement et immédiatement dans la BD. <br>
 * Exemple d'utilisation interne pour un "create" :<br>
 * <pre>
 *   try {
 *     em.persist(p);
 *     tr.commit();
 *   } catch (Exception ex1) {
 *     try {
 *       tr.rollback();
 *     } catch (Exception ex2) {
 *       ...
 *     }
 *   }
 * </pre>
 * <br>
 * Il est possible de lancer des transactions manuelles comme suit :<br>
 * <pre>
 *   Transaction tr = JpaConnection.getInstance().getTransaction();
 *   ...
 *   ...
 *   try {
 *      tr.beginManualTransaction();
 *      for ... {
 *        // operation CRUD
 *      }
 *      tr.commitManualTransaction();
 *   } catch (Exception ex1) {
 *       try {
 *         tr.rollbackManualTransaction();
 *       } catch (Exception ex2) {
 *         Logger.error(clazz, ex2.getMessage());
 *       }
 *   } finally {
 *     tr.finishManualTransaction();
 *   }
 * </pre>
 *
 * @author P.-A. mettraux / J.-C. Stritt
 *
 * @opt nodefillcolor LemonChiffon
 * @opt all
 */
public class Transaction {

  private EntityTransaction et;
  private UserTransaction ut;
  private boolean autoCommit;

  public Transaction(EntityTransaction et) {
    this.et = et;
    this.ut = null;
    init(true);
  }

  public Transaction(UserTransaction ut) {
    this.ut = ut;
    this.et = null;
    init(false);
  }

  private void init(boolean auto) {
    autoCommit = isActive(); // false; // change JCS 30.12.2016
    if (auto) {
      setAutoCommitOn();
    } else {
      setAutoCommitOff();
    }
  }

  private boolean isUserTransactionActive() {
    try {
      return ut.getStatus() == Status.STATUS_ACTIVE;
    } catch (Exception e) {
      return false;
    }
  }

  private void commitNow() throws Exception {
    if (isActive()) {
      if (et != null) {
        et.commit();
      } else if (ut != null) {
        ut.commit();
      }
    }
    if (autoCommit) begin();
  }

  private void rollbackNow() throws Exception {
    if (isActive()) {
      if (et != null) {
        et.rollback();
      } else if (ut != null) {
        ut.rollback();
      }
    }
    if (autoCommit) begin();
  }

  private void setAutoCommitOn() {
    if (!autoCommit) {
      autoCommit = true;
      try {
        commitNow();
      } catch (Exception e) {
      }
    }
  }

  private void setAutoCommitOff() {
    if (autoCommit) {
      try {
        rollbackNow(); // essayé commitNow(); mais erreur dans les tests unitaires (31.12.2016)
      } catch (Exception e) {
      }
      autoCommit = false;
    }
  }



  /**
   * Permet de connaitre l'état d'une transaction.
   *
   * @return true si une transaction est engagée
   */
  public boolean isActive() {
    boolean act = false;
    if (et != null) {
      act = et.isActive();
    } else if (ut != null) {
      act = isUserTransactionActive();
    }
    return act;
  }

  /**
   * Activation ou désactivation de la validation automatique de chaque
   * modification de donnée.
   *
   * @param on : indique par une valeur true ou false si l'on désire l'autocommit
   */
  public void setAutoCommit(boolean on) {
    if (on) {
      setAutoCommitOn();
    } else {
      setAutoCommitOff();
    }
  }

  /**
   * Retourne vrai si on est en mode "autocommit".
   *
   * @return TRUE si autocommit = VRAI
   */
  public boolean isAutoCommit() {
    return autoCommit;
  }

  /**
   * Débute une transaction. Utile seulement en mode autocommmit=false ce qui est
   * le cas pour un serveur d'applications tel que GlassFish qui ne supporte pas
   * qu'une transaction soit commencée sans être terminée (ce qui exclue le mode
   * autocommit=true).
   *
   * @throws java.lang.Exception l'exception remontée au niveau supérieur
   */
  public void begin() throws Exception {
    if (et != null) {
      et.begin();
    } else if (ut != null) {
      ut.begin();
    }
  }

  /**
   * Validation automatique d'une transaction dans JpaDao si l'autocommit est à true.
   *
   * @throws java.lang.Exception l'exception remontée au niveau supérieur
   */
  public void commit() throws Exception {
    if (autoCommit) {
      commitNow();
    }
  }

  /**
   * Annulation automatique d'une transaction dans JpaDao si l'autocommit est à true.
   *
   * @throws java.lang.Exception l'exception remontée au niveau supérieur
   */
  public void rollback() throws Exception {
    if (autoCommit) {
      rollbackNow();
    }
  }

  /**
   * Débute une transaction manuelle en mettant l'auto-commit à false.
   */
  public void beginManualTransaction() {
    setAutoCommit(false);
  }

  /**
   * Termine une transaction manuelle en remettant l'auto-commit à true.
   */
  public void finishManualTransaction() {
    setAutoCommit(true);
  }

  /**
   * Validation manuelle d'une transaction si l'autocommit est à false.
   *
   * @throws java.lang.Exception l'exception remontée au niveau supérieur
   */
  public void commitManualTransaction() throws Exception {
    if (!autoCommit) {
      commitNow();
    }
  }

  /**
   * Annulation manuelle d'une transaction si l'autocommit est à false.
   *
   * @throws java.lang.Exception l'exception remontée au niveau supérieur
   */
  public void rollbackManualTransaction() throws Exception {
    if (!autoCommit) {
      rollbackNow();
    }
  }

}
