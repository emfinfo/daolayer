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
  
  EntityManager getEm();
  Transaction getTr();
  boolean isConnected();
  void disconnect();
  
}
