package ch.emf.dao;

import org.slf4j.LoggerFactory;

/**
 * Assure les messages de debug, d'erreur ou d'info de la couche "dao" en utilisant
 * l'interface SLF4J et une implémentation LOG4J.
 * On peut aussi l'utiliser en dehors de la couche, puisque cette classe
 * ne contient que des méthodes statiques.
 * 
 * @author jcstritt
 *
 * @opt nodefillcolor LemonChiffon
 */
public class Logger {

  private static final int LEVEL = -2;
  private static final String SIMPLE_FORMAT = "{} [{}]";

  /**
   * Méthode privée pour récupérer le nom d'une méthode parente.
   *
   * @param level 0 = la méthode courante, -1 la méthode parente immédiate
   */
  private static String getParentMethod( int level ) {
    StackTraceElement e[] = Thread.currentThread().getStackTrace();
    StackTraceElement trace = e[2 - level];
    return trace.getMethodName();
  }

  /**
   * Méthode privée pour récupérer le nom de la méthode courante de l'appelant.
   */
  private static String getCurrentMethod() {
    return getParentMethod(LEVEL);
  }

  /**
   * Méthode privée générique pour afficher un message.
   *
   * @param which 1=message d'info, 2=message de debug, 3=message d'erreur
   * @param cl la classe
   * @param msg le message à afficher
   * @param values les valeurs à afficher
   */
  private static void display( int which, Class<?> cl, Object msg, Object... values ) {
    String format = SIMPLE_FORMAT + " - ";
    Object[] tab = new Object[2 + values.length];
    tab[0] = Logger.getParentMethod(LEVEL);
    tab[1] = msg;
    int i = 0;
    for (Object value : values) {
      format += "{}";
      tab[i + 2] = value;
      i++;
      if (i < values.length) {
        format += ", ";
      }
    }
    switch (which) {
      case 1:
        LoggerFactory.getLogger(cl).info(format, tab);
        break;
      case 2:
        LoggerFactory.getLogger(cl).debug(format, tab);
        break;
      case 3:
        LoggerFactory.getLogger(cl).error(format, tab);
        break;
    }
  }

  /**
   * Méthode pour afficher un message d'information.
   *
   * @param cl la classe de l'appelant
   * @param msg le message à afficher
   */
  public static void info( Class<?> cl, Object msg ) {
    LoggerFactory.getLogger(cl).info(SIMPLE_FORMAT, getCurrentMethod(), msg);
  }

  /**
   * Méthode pour afficher un message d'information.
   *
   * @param cl la classe de l'appelant
   * @param msg le message principal à afficher
   * @param values des valeurs supplémentaires à afficher
   */
  public static void info( Class<?> cl, Object msg, Object... values ) {
    display(1, cl, msg, values);
  }

  /**
   * Méthode pour afficher un message de deboguage.
   *
   * @param cl la classe de l'appelant
   * @param msg le message à afficher
   */
  public static void debug( Class<?> cl, Object msg ) {
    LoggerFactory.getLogger(cl).debug(SIMPLE_FORMAT, getCurrentMethod(), msg);
  }

  /**
   * Méthode pour afficher un message de deboguage.
   *
   * @param cl la classe de l'appelant
   * @param msg le message principal à afficher
   * @param values des valeurs supplémentaires à afficher
   */
  public static void debug( Class<?> cl, Object msg, Object... values ) {
    display(2, cl, msg, values);
  }

  /**
   * Méthode pour afficher un message d'erreur.
   *
   * @param cl la classe de l'appelant
   * @param msg le message à afficher
   */
  public static void error( Class<?> cl, Object msg ) {
    LoggerFactory.getLogger(cl).error(SIMPLE_FORMAT, getCurrentMethod(), msg);
  }

  /**
   * Méthode pour afficher un message d'erreur.
   *
   * @param cl la classe de l'appelant
   * @param msg le message principal à afficher
   * @param values des valeurs supplémentaires à afficher
   */
  public static void error( Class<?> cl, Object msg, Object... values ) {
    display(3, cl, msg, values);
  }

}
