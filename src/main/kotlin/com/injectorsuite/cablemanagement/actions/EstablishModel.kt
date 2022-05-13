package com.injectorsuite.cablemanagement.actions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.swing.JComponent

class EstablishModel(): AnAction(){
    override fun actionPerformed(e: AnActionEvent) {
        val modelReceiver= ModelReceiver()
            if (modelReceiver.showAndGet()){
                modelReceiver.generateAnnotations(e)
                modelReceiver.generateJSON(e)
            }
    }

}



class ModelReceiver(): DialogWrapper(true) {

    var model= Model(listOf())

    init{
        init()
        title="Establish Model"
        setSize(500,500)
    }


    override fun createCenterPanel(): JComponent? {
        return ComposePanel().apply {
            setContent {
                var level by remember{mutableStateOf(1)}
                var compartmentName by remember{mutableStateOf("")}
                var layers by remember{mutableStateOf(listOf<Layer>())}
                Column(Modifier.size(500.dp)) {
                    Text("Add new compartment for level "+level.toString())
                    OutlinedTextField(
                        value = compartmentName,
                        onValueChange = {
                            compartmentName = it
                        },
                        label = { Text("Add Compartment") },
                        singleLine = true,
                    )
                    Row{
                        Text("Add "+compartmentName+" compartment to level "+level.toString(),Modifier.clickable(onClick={
                            layers=layers+listOf(Layer(compartmentName,level))
                            model= Model(layers)
                            compartmentName=""
                        }))
                        Spacer(Modifier.weight(1f))
                        Text("Next Level",Modifier.clickable(onClick={
                            level=level+1
                        }))
                        Spacer(Modifier.weight(1f))
                    }

                    Text("Model:")
                    Spacer(Modifier.height(12.dp))
                    Text(layers.map{it->it.level}.toSortedSet().fold(""){acc,next->
                        acc+"Level "+next.toString()+": "+layers.filter{it.level==next}.fold(""){line,layer->
                            line+layer.name+", "
                        }.removeSuffix(", ")+"\n"
                    })

                }
            }
        }
    }

    fun generateAnnotations(e: AnActionEvent){
        val tm= FileTemplateManager.getInstance(e.project!!)
        val template=tm.addTemplate("annotations","kt")
        val annotationCode=model.layers.fold(""){ acc, next->
            acc+"annotation class "+next.name+"\n\n"
        }
        var directory=e.getData(CommonDataKeys.PSI_FILE)!!.containingDirectory
        while (directory.name!="app" && directory.parentDirectory!=null){
            directory=directory.parentDirectory
        }
        directory=directory.findSubdirectory("src")?.findSubdirectory("main")?.findSubdirectory("res")?:directory
        if (directory.name!="res"){
            Messages.showInfoMessage("For some reason, annotation file was not put in resources but was placed in "+directory.name+ ". I don't know why that happened, but you'll have to move it manually.","Error")
        }
        template.text = annotationCode
        FileTemplateUtil.createFromTemplate(template,"CableManagementAnnotations.kt",null,directory)
    }
    fun generateJSON(e: AnActionEvent){
        val tm= FileTemplateManager.getInstance(e.project!!)
        val template=tm.addTemplate("json","json")
        val jsonCode= Json.encodeToString(model)
        var directory=e.getData(CommonDataKeys.PSI_FILE)!!.containingDirectory
        while (directory.name!="app" && directory.parentDirectory!=null){
            directory=directory.parentDirectory
        }
        directory=directory.findSubdirectory("src")?.findSubdirectory("main")?.findSubdirectory("res")?:directory
        if (directory.name!="resources"){
            Messages.showInfoMessage("For some reason, annotation file was not put in resources but was placed in "+directory.name+ ". I don't know why that happened, but you'll have to move it manually.","Error")
        }
        template.text = jsonCode
        FileTemplateUtil.createFromTemplate(template,"CableManagementConfig",null,directory)
    }
}