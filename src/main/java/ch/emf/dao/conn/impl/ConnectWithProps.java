package ch.emf.dao.conn.impl;

import ch.emf.dao.conn.Connectable;
import ch.emf.dao.exceptions.JpaException;
import ch.emf.dao.helpers.Logger;
import ch.emf.dao.transactions.Transaction;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Connecte avec une "Persistence Unit" de JPA et des propriétés spécifiées.
 * Elles proviennent généralement d'un fichier de configuration ou
 * de propriétés système mémorisées.
 *
 * @author jcstritt
 */
public class ConnectWithProps implements Connectable {
  private EntityManagerFactory emf;
  private EntityManager em;
  private Transaction tr;
  private Class<?> clazz;

  public ConnectWithProps(String pu, Properties props) throws JpaException {
    emf = null;
    em = null;
    tr = null;
    clazz = ConnectWithProps.class;
    try {
      emf = Persistence.createEntityManagerFactory(pu, props);
      em = emf.createEntityManager();
      tr = new Transaction(em.getTransaction());
    } catch (Exception ex) {
      throw new JpaException(clazz.getSimpleName(), "ConnectWithProps", ex.getMessage());
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
        Logger.error(clazz, ex.getMessage(), clazz.getSimpleName());
      }
    }
  }

}
