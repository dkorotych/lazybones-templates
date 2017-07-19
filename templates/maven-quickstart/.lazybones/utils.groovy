class Utils {
    File fileInProject(name) {
        return new File(projectDir, name)
    }

    def askBoolean(String message, String defaultValue, String property) {
        def positiveAnswer = ['yes', 'true', 'ok', 'y']
        String answer = ask("${message} ${positiveAnswer}: ", defaultValue, property).toLowerCase()
        return positiveAnswer.find {
            return it == answer
        }
    }

    def askPredefined(String message, String defaultValue, List<String> answers, String property,
                      boolean showAnswers = true) {
        if (showAnswers) {
            message = "${message} ${answers}: "
        }
        answers = answers.each {
            it.toLowerCase()
        }
        def answer = ''
        while (!answers.contains(answer)) {
            answer = ask(message, defaultValue, property).toLowerCase()
        }
        return answer
    }

    /**
     * Read indent from editor settings file
     */
    def readIndent() {
        def indent = 2
        fileInProject('.editorconfig').readLines(fileEncoding).each {
            it.find(~/indent_size\s+=\s+(\d+)/) {
                indent = it[1] as Integer
            }
        }
        return indent
    }

    /**
     * Read indent from editor settings file
     */
    def readIndentAsString() {
        return ' ' * readIndent()
    }
}
