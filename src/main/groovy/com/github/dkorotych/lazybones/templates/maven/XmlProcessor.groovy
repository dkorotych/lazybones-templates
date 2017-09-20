package com.github.dkorotych.lazybones.templates.maven

import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder
import groovy.xml.XmlUtil

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
abstract class XmlProcessor {
    final GPathResult result
    final String indent
    XmlSlurper slurper
    protected File file
    protected Writer writer
    protected Script script

    XmlProcessor(Script script, String fileName) {
        this.script = script
        slurper = new XmlSlurper(false, false)
        slurper.setKeepIgnorableWhitespace(true)
        file = script.fileInProject(fileName)
        result = slurper.parse(file)
        // Read indent from editor settings file
        indent = script.readIndentAsString()
    }

    MarkupBuilder createMarkupBuilder() {
        writer = new StringWriter()
        return new MarkupBuilder(new IndentPrinter(writer, indent))
    }

    void save() {
        XmlUtil.serialize(result, file.newWriter(script.fileEncoding))
    }

    protected abstract void process()

    protected GPathResult changes() {
        return slurper.parseText(writer.toString())
    }
}
