# daolayer 6.0.0 - october 2018
A dao layer over JPA. With this library, you work with objects and lists of objects. No sql. Select queries can be made with a Search or Search2 object.

You can download and open this project in NetBeans 8.2. It's a Java 8 maven project. So, dependencies are loaded automaticly from maven central. There are some test classes where you can learn how to use this library.

In MacOS terminal or Windows console, you can start the "test" suite with a Maven command :

mvn test

Project documentation here :
http://jcstritt.emf-informatique.ch/doc/daolayer<br>

New in release 6.0.0 (24.10.2018) :
* Use of Google Guice for dependency injection
* See tests initialisation to see an injection by Guice
* The JpaDaoAPI interface if far the same, but use a new "setConnection" to set a connection.
* To use with a framework that give you the entitymanager, you must create your own connection class and create a DaoRepository class to make the connection. @see the "conseillers" application on https://github.com/emfinfo.

New in release 5.2.0 (19.03.2018) :
* JpaConnectionAPI have a new method "isOnServer()"
* This method is used in getList methods to optimize traitement for local and server application
* Correction of a big bug in method "setEntityManager" in JpaDao (crash with multiple requests in PlayFramework)

New in release 5.1.5 (3.10.2017) :
* Search2 class has been redesigned : you can now define the logical operator by default between the various filters.
Or even delete it. Javadoc has been added.

New in release 5.1.4 (12.1.2017) :
* New methods for filtering in Search2 class.

New in release 5.1.3 (6.1.2017) :
* Correct a bug in "inserList" and "updateList" (a flush is added to have a sorted list correct inserted).
* The tests are now adapted to new "parlement" database structure (2017)

New in release 5.1.2 (31.12.2016) :
* Correct a bug in initialisation of Transaction class : if a transaction is currently active, set autocommit flag to true immediatly. This preserve some tenancy properties stored in the current entity-manager transaction.
* Add NoTransactionJpa annotation in ch.emf.dao. It's for DbWorkerAPI classes to mark non JPA methods.

New in release 5.1.1 (29.12.2016) :
* Two "open" methods in JpaDaoAPI are now renamed "setEntityManager" when they accept an entity manager as parameter.

New in release 5.1.0 (11.10.2016) :
* New read method in API that can detach an object immediatly. This is better for standalone Java applications.
* Old read methods are deleted from API for better understanding of the new one.

New in release 5.0.37 (01.10.2016) :
* The "update" and "delete" methods returns now -1 if a concurrent access occurs. In JPA, you must add a @Version annotation on an "version" integer field in each class-entity where update or delete is possible.
* Change release numbering from MAJOR.PATCH to MAJOR.MINOR.PATCH.

New in release 5.36 (17.08.2016) :
* "getMaxStringValue" is now deleted from JpaDaoAPI.
* "getMinIntValue" is now added to JpaDaoAPI.
* "getMaxIntValue(Search)" is now renamed "getIntValue(Search)".
* Small bug correction in "Search2" (no more where key when no filter is added).

New in release 5.35 (23.04.2016) :
* "updateList(...)" in JpaDaoAPI now returns an integer array : [0]=number of updated objects, [1]=number of added objects.
