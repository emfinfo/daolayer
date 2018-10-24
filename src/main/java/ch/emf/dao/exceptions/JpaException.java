package ch.emf.dao.exceptions;

/**
 * Exception JPA.
 * 
 * @author jcstritt
 */
public class JpaException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Constructeur.
   * 
   * @param method class and method name
   * @param msg error or other information
   */
  public JpaException(String method, String msg) {
    super("ERROR JPA in: " + method + "\n" + msg);
  }

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public String getMessage() {
    return super.getMessage();
  }

}
