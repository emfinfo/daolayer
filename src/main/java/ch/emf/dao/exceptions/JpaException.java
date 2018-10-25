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
   * @param methodName class and method name
   * @param msg error or other information
   */
  public JpaException(String className,  String methodName, String msg) {
    super("JPA error detected in: " + methodName + ", class: " + className + "\n" + msg);
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
