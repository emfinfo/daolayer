package ch.emf.dao.exceptions;

/**
 * Exception JPA.
 * 
 * @author jcstritt
 */
public class JpaException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   * 
   * @param className the class name
   * @param methodName the methodName
   * @param msg the error message
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
