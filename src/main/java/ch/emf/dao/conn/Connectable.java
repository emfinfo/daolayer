package ch.emf.dao.conn;

import ch.emf.dao.transactions.Transaction;
import javax.persistence.EntityManager;

/**
 * Interface définissant les actions possibles sur une connexion JPA.
 *
 * @author jcstritt
 */
//@ImplementedBy(ConnectWithPU.class)
public interface Connectable {

  /**
   * Retourne l'objet EntityManager mémorisé dans un objet de connexion.
   *
   * @return l'objet en question
   */
  EntityManager getEm();


  /**
   * Retourne l'objet Transaction mémorisé dans un objet de connexion.
   *
   * @return l'objet en question
   */
  Transaction getTr();


  /**
   * Détermine si l'objet EntityManager mémorisé est différent de null
   * et actuellement ouvert sur une base de données.
   *
   * @return true si une connexion existe vers une BD via l'entity-manager
   */
  boolean isConnected();


  /**
   * Méthode de déconnexion à implémenter. Dans un système client-serveur, il n'y pas de
   * déconnexion à effectuer, car cela est géré normalement par un pool de connexions.
   * Mais dans une application standalone où un seul objet EntityManager est créé,
   * il faut fermer toutes les resources (voir une implémentation dans "ConnectWithPU").
   */
  void disconnect();

}
