package com.github.dkorotych.lazybones.templates.maven

import groovy.util.slurpersupport.GPathResult

/**
 * @author Dmitry Korotych (dkorotych at gmail dot com)
 */
class RuleSetProcessor extends XmlProcessor {
    RuleSetProcessor(Script script) {
        super(script, 'config/dependency-rules.xml')
    }

    @Override
    protected void process() {
        GPathResult changes = changes()
        changes.rules.'*'.each {
            result.rules << indent
            result.rules << it
            result.rules << "\n${indent}"
        }
    }
}
