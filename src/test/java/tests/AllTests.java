package tests;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
/**
 * Cette suite de tests utilise une base de données MySql nommé "parlement".
 * Son utilisation est définie par le fichier "persistence.xml".
 *
 * Les données pour tester la base MySql se trouvent dans le dossier "data"
 * de ce projet.
 *
 * @author Jean-Claude Stritt
 */
@RunWith(value=Suite.class)
@SuiteClasses(value={
  JpaConnectionTest.class,
  JpaDaoTest.class
})
public class AllTests {

  @BeforeClass
  public static void setUpClass() throws Exception {
//    System.out.println("Default CHARSET: " + Charset.defaultCharset().name() + "\n");
  }

}