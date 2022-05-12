package com.injectorsuite.cablemanagement.services

import com.intellij.openapi.project.Project
import com.injectorsuite.cablemanagement.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
