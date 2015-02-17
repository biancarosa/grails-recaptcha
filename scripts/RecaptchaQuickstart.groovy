includeTargets << grailsScript("_GrailsBootstrap")

USAGE = """
Usage: grails recaptcha-quickstart <standalone|integrated>

Creates the required configuration elements for using the ReCaptcha plugin.
If 'integrated' is specified, entries are created in Config.groovy.
If 'standalone' is specified, then RecaptchaConfig.groovy is created.
"""

appDir = "$basedir/grails-app"
templateDir = "$recaptchaPluginDir/src/templates"

target(main: "Create default ReCaptcha configuration") {
    def argValue = parseArgs()
    if (!argValue) {
        return 1
    }
    if ("standalone" == argValue) {
        copyConfig()
    } else if ("integrated" == argValue) {
        updateConfig()
    }

    printMessage """
*************************************************
ReCaptcha configuration created successfully.
Please be sure to enter your API keys before use.
*************************************************
"""
}

private parseArgs() {
    def args = argsMap.params

    if (1 == args.size()) {
        if ("standalone" == args[0]) {
            printMessage "Creating standalone ReCaptcha configuration in RecaptchaConfig.groovy"
        } else if ("integrated" == args[0]) {
            printMessage "Creating ReCaptcha configuration in Config.groovy"
        } else {
            errorMessage USAGE
            return null
        }
        return args[0]
    }

    errorMessage USAGE
    null
}

private void updateConfig() {

    def configFile = new File(appDir, 'conf/Config.groovy')
    if (configFile.exists()) {
        configFile.withWriterAppend {
            it.write """
\n// Added by the Recaptcha plugin:
recaptcha {
    // These keys are generated by the ReCaptcha service
    publicKey = ""
    privateKey = ""

    // Include the noscript tags in the generated captcha
    includeNoScript = true

    //Include the script tags in the generated captcha
    includeScript = true

    // Set to false to disable the display of captcha
    enabled = true
}

mailhide {
    // Generated by the Mailhide service
    publicKey = ""
    privateKey = ""
}
"""
        }
    }
}

okToWrite = { String dest ->

    def file = new File(dest)
    if (!file.exists()) {
        return true
    }

    String propertyName = "file.overwrite.$file.name"
    ant.input(addProperty: propertyName, message: "$dest exists, ok to overwrite?",
            validargs: 'y,n,a', defaultvalue: 'y')

    if (ant.antProject.properties."$propertyName" == 'n') {
        return false
    }

    if (ant.antProject.properties."$propertyName" == 'a') {
        overwriteAll = true
    }

    true
}

copyFile = { String from, String to ->
    if (!okToWrite(to)) {
        return
    }

    ant.copy file: from, tofile: to, overwrite: true
}

private void copyConfig() {
    copyFile "$templateDir/RecaptchaConfig.groovy", "$appDir/conf/RecaptchaConfig.groovy"
}

printMessage = { String message -> event('StatusUpdate', [message]) }
errorMessage = { String message -> event('StatusError', [message]) }

setDefaultTarget(main)
