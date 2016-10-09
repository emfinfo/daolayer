# daolayer 5.1.0 - october 2016
A dao layer over JPA. With this library, you work with objects and lists of objects. No sql. Select queries can be made with a Search or Search2 object.

You can download and open this project in NetBeans 8.1. It's a Java 8 maven project. So, dependencies are loaded automaticly from maven central. There are some test classes where you can learn how to use this library.

In MacOS terminal or Windows console, you can start the "test" suite with a Maven command :

mvn test

Project documentation here :
http://jcstritt.emf-informatique.ch/doc/daolayer<br>

New in release 5.1.0 :
* new read method in API that can detach an object immediatly. This is better for standalone Java applications.

New in release 5.0.37 :
* The "update" and "delete" methods returns now -1 if a concurrent access occurs. In JPA, you must add a @Version annotation on an "version" integer field in each class-entity where update or delete is possible.
* Change release numbering from MAJOR.MINOR to MAJOR.MINOR.PATCH

New in release 5.36 :
* "getMaxStringValue" is now deleted from JpaDaoAPI
* "getMinIntValue" is now added to JpaDaoAPI
* "getMaxIntValue(Search)" is now renamed "getIntValue(Search)"
* Small bug correction in "Search2" (no more where key when no filter is added)

New in release 5.35 :
* "updateList(...)" in JpaDaoAPI now returns an integer array : [0]=number of updated objects, [1]=number of added objects
