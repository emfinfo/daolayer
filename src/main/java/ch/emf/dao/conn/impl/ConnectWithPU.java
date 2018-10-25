package ch.emf.dao.conn.impl;

import ch.emf.dao.transactions.Transaction;
import ch.emf.dao.conn.Connectable;
import ch.emf.dao.exceptions.JpaException;
import ch.emf.dao.helpers.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Connecte avec une "Persistence Unit" de JPA (fichier XML).
 * 
 * @author jcstritt
 */
public class ConnectWithPU implements Connectable {
  private EntityManagerFactory emf; 
  private EntityManager em;
  private Transaction tr;  
  
  public ConnectWithPU(String pu) throws JpaException {
    emf = null;
    em = null;
    tr = null;
    try {
      emf = Persistence.createEntityManagerFactory(pu);
      em = emf.createEntityManager();
      tr = new Transaction(em.getTransaction());
    } catch (Exception ex) {
      throw new JpaException(this.getClass().getSimpleName(), "ConnectWithPU", ex.getMessage());
    }  
  }
  
  @Override
  public boolean isConnected() {
    return (em != null) && em.isOpen();
  }  
  
  @Override
  public EntityManager getEm() {
    return this.em;
  }

  @Override
  public Transaction getTr() {
    return this.tr;
  }
  
  @Override
  public void disconnect() {
    if (isConnected()) {
      try {
        em.close();
        if (emf != null) {
          emf.close();
        }
      } catch (Exception ex) {
        Logger.error(this.getClass(), ex.getMessage());       
      }
    }
  }

}
