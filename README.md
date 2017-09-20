## Lazybones Templates

[![Build Status](https://travis-ci.org/dkorotych/lazybones-templates.svg?branch=master)](https://travis-ci.org/dkorotych/lazybones-templates)
[![Download](https://api.bintray.com/packages/dkorotych/lazybones-templates/maven-quickstart-template/images/download.svg)](https://bintray.com/dkorotych/lazybones-templates/maven-quickstart-template/_latestVersion)
[![license](https://img.shields.io/github/license/dkorotych/lazybones-templates.svg)](https://github.com/dkorotych/lazybones-templates.git)

This project holds a set of templates to be used with [Lazybones].
The current list contains the following templates

* [maven-quickstart]: Quick start a base [Maven] application
  * Additional support for [logback]
  * Additional support for [vert.x]
  
## Installation

```bash
lazybones config add bintrayRepositories dkorotych/lazybones-templates
```

## How To
### Create [Maven] project
```bash
lazybones create maven-quickstart .
```
#### Generate supported feature inside project
* [logback]
```bash
lazybones generate logback-support
```
* [vert.x]
```bash
lazybones generate vertx-support
```

[1]: https://github.com/pledbrook/lazybones/wiki/Template-developers-guide
[Lazybones]: http://github.com/pledbrook/lazybones
[maven-quickstart]: https://bintray.com/dkorotych/lazybones-templates/maven-quickstart-template/_latestVersion
[Bintray]: https://bintray.com/
[Maven]: http://maven.apache.org
[logback]: http://logback.qos.ch
[vert.x]: http://vertx.io