##Lazybones Templates

[![Build Status](https://travis-ci.org/dkorotych/lazybones-templates.svg?branch=master)](https://travis-ci.org/dkorotych/lazybones-templates)

This project holds a set of templates to be used with [Lazybones][].
The current list contains the following templates

 * [maven-quickstart][]: Quick start a base [Maven][] application
 
[Lazybones]: http://github.com/pledbrook/lazybones
[maven-quickstart]: https://github.com/dkorotych/lazybones-templates/templates/maven-quickstart
[Maven]: http://maven.apache.org

##How To

You have just created a simple project for managing your own Lazybones project
templates. You get a build file (`build.gradle`) and a directory for putting
your templates in (`templates`).

To get started, simply create new directories under the `templates` directory
and put the source of the different project templates into them. You can then
package and install the templates locally with the command:

    ./gradlew installAllTemplates

You'll then be able to use Lazybones to create new projects from these templates.
If you then want to distribute them, you will need to set up a Bintray account,
populate the `repositoryUrl`, `repositoryUsername` and `repositoryApiKey` settings
in `build.gradle`, add new Bintray packages in the repository via the Bintray
UI, and finally publish the templates with

    ./gradlew publishAllTemplates

You can find out more about creating templates on [the GitHub wiki][1].

[1]: https://github.com/pledbrook/lazybones/wiki/Template-developers-guide
