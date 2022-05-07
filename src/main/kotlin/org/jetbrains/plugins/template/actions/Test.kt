package org.jetbrains.plugins.template.actions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.unit.dp
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.lang.ASTNode
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.roots.ContentIterator
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.idea.caches.resolve.KtFileClassProviderImpl
import org.jetbrains.kotlin.idea.refactoring.move.moveFilesOrDirectories.KotlinMoveDirectoryWithClassesHelper
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFileAnnotationList
import org.jetbrains.kotlin.psi.KtFileClassProvider
import java.io.FileInputStream
import javax.swing.JComponent



class Test: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val psiManager=PsiManager.getInstance(e.project!!)
        val ktp=KTProcessor(psiManager)
        ProjectFileIndex.SERVICE.getInstance(e.project).iterateContent(ktp)
        val tm=FileTemplateManager.getInstance(e.project!!)
        val template=tm.addTemplate("annotations","kt")
        val annotationCode=ktp.model.layers.fold(""){ acc, next->
            acc+"annotation class "+next.name+"\n\n"
        }

        var directory=e.getData(CommonDataKeys.PSI_FILE)!!.containingDirectory
        while (directory.name!=e.project!!.name){
            directory=directory.parentDirectory
        }
        directory=directory.findSubdirectory("src")?.findSubdirectory("main")?.findSubdirectory("resources")?:directory
        if (directory.name!="resources"){
            Messages.showInfoMessage("For some reason, annotation file was not put in resources but was placed in "+directory.name+ ". I don't know why that happened, but you'll have to move it manually.","Error")
        }

        println(directory.subdirectories.map{it->it.name})
        template.text = annotationCode
        FileTemplateUtil.createFromTemplate(template,"CableManagementAnnotations.kt",null,directory)

        ActionWrapper(NaiveGraphBuilder().buildGraph(ktp.allText,ktp.model)).show()
    }
}

class KTProcessor(val psiManager:PsiManager): ContentIterator {

    var allText=""
    var model=Model(listOf())
    @OptIn(ExperimentalSerializationApi::class)
    override fun processFile(fileOrDir: VirtualFile): Boolean {
        if (fileOrDir.extension!="kt" && fileOrDir.name!="CableManagement.json"){
            println(fileOrDir.name)
            return true}
        if (fileOrDir.name=="CableManagement.json"){
            model=Json.decodeFromString<Model>(psiManager.findFile(fileOrDir)!!.text)
            println("###### layers")
            println(model.layers.size)
            return true
        }
        val file= psiManager.findFile(fileOrDir)
        val rawText=file?.text?:""
        allText=allText+rawText
        return true
    }
}

@kotlinx.serialization.Serializable
data class Layer(val name:String,val level:Int)

@kotlinx.serialization.Serializable
data class Model(val layers:List<Layer>)

class NaiveGraphBuilder(){

    fun buildGraph(text:String,model:Model):String{
        return model.layers.map{it->it.level}.toSortedSet().fold(""){acc,next->
            acc+"Level "+next.toString()+": "+model.layers.filter{it.level==next}.fold(""){line,layer->
                line+layer.name+", "
            }.removeSuffix(", ")+"\n"
        }
    }

}


class ActionWrapper(val text:String): DialogWrapper(true) {
    init{
        init()
        title="a demo"
        setSize(500,500)
    }
    override fun createCenterPanel(): JComponent? {
        return ComposePanel().apply {
            setContent {
                Column(Modifier.size(500.dp)) {
                    Text(text)

                }
            }
        }
    }
}

