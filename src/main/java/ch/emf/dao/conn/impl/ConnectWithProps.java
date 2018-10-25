package ch.emf.dao.conn.impl;

import ch.emf.dao.transactions.Transaction;
import ch.emf.dao.conn.Connectable;
import ch.emf.dao.exceptions.JpaException;
import ch.emf.dao.helpers.Logger;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Connecte avec une "Persistence Unit" et des propriétés spécifiées.
 * Elles proviennent généralement d'un fichier de configuration ou
 * de propriétés système mémorisées.
 * 
 * @author jcstritt
 */
public class ConnectWithProps implements Connectable {
  private String pu;
  private EntityManagerFactory emf; 
  private Properties props;
  private EntityManager em;
  private Transaction tr;

  public ConnectWithProps(String pu, Properties props) throws JpaException {
    this.pu = pu;
    em = null;
    tr = null;
    try {
      emf = Persistence.createEntityManagerFactory(pu, props);
      em = emf.createEntityManager();
      tr = new Transaction(em.getTransaction());
    } catch (Exception ex) {
      Logger.error(this.getClass(), ex.getMessage());       
      throw new JpaException(this.getClass().getSimpleName(), "ConnectWithProps", ex.getMessage());
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
