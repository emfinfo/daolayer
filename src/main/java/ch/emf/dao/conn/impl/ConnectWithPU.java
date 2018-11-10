package ch.emf.dao.conn.impl;

import ch.emf.dao.conn.Connectable;
import ch.emf.dao.exceptions.JpaException;
import ch.emf.dao.helpers.Logger;
import ch.emf.dao.transactions.Transaction;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Connecte avec une "Persistence Unit" de JPA (persistence.xml).
 *
 * @author jcstritt
 */
public class ConnectWithPU implements Connectable {
  private EntityManagerFactory emf;
  private EntityManager em;
  private Transaction tr;
  private Class<?> clazz;

  /**
   * Constructeur.
   *
   * @param pu un nom d'unité de persistence
   * @throws JpaException une exception à traiter en cas d'erreur
   */
  public ConnectWithPU(String pu) throws JpaException {
    emf = null;
    em = null;
    tr = null;
    clazz = ConnectWithPU.class;
    try {
      emf = Persistence.createEntityManagerFactory(pu);
      em = emf.createEntityManager();
      tr = new Transaction(em.getTransaction());
    } catch (Exception ex) {
      throw new JpaException(clazz.getSimpleName(), "ConnectWithPU", ex.getMessage());
    }
  }

  /**
   * Retourne l'objet EntityManager mémorisé ici.
   *
   * @return l'objet en question
   */
  @Override
  public EntityManager getEm() {
    return this.em;
  }

  /**
   * Retourne un objet Transaction mémorisé ici.
   *
   * @return l'objet en question
   */
  @Override
  public Transaction getTr() {
    return this.tr;
  }

  /**
   * Détermine si l'objet EntityManager mémorisé est différent de null
   * et actuellement ouvert sur une base de données.
   *
   * @return true si une connexion existe vers une BD
   */
  @Override
  public boolean isConnected() {
    return (em != null) && em.isOpen();
  }

  /**
   * Méthode de déconnexion (ferme toutes les resources ouvertes dans le constructeur).
   */
  @Override
  public void disconnect() {
    if (isConnected()) {
      try {
        em.close();
        if (emf != null) {
          emf.close();
        }
      } catch (Exception ex) {
        Logger.error(ConnectWithPU.class, ex.getMessage(), clazz.getSimpleName());
      }
    }
  }

}
