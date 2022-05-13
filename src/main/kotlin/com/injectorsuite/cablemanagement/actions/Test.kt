package com.injectorsuite.cablemanagement.actions

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
import com.intellij.psi.PsiElement
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.roots.ContentIterator
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.idea.caches.resolve.KtFileClassProviderImpl
import org.jetbrains.kotlin.idea.debugger.sequence.psi.callName
import org.jetbrains.kotlin.idea.refactoring.move.moveFilesOrDirectories.KotlinMoveDirectoryWithClassesHelper
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
//import org.jetbrains.kotlin.psi.*
import java.io.FileInputStream
import javax.swing.JComponent




class Test: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val psiManager=PsiManager.getInstance(e.project!!)
        val ktp= KTProcessor(psiManager)
        ktp.model=Json.decodeFromString(FilenameIndex.getFilesByName(e.project!!,"CableManagementConfig.json", GlobalSearchScope.allScope(e.project!!))[0].text)
        ProjectFileIndex.SERVICE.getInstance(e.project).iterateContent(ktp)

        val graph=mutableListOf<Node>()
        ktp.nodes.forEach{
            val newNode= Node(it.name,it.layer,it.dependents.filter{ dep->
                dep in ktp.nodes.map{node->node.name}
            })
            graph.add(newNode)
        }

        val text=graph.fold("",{acc,next->
            acc+next.toString()+"\n"
        })

        ActionWrapper(text).show()
    }
}

class Node(val name:String,val layer:String,val dependents:List<String>){
    override fun toString():String{
        return name+" ** "+layer + " ** " +dependents.toString()
    }
}



class KTProcessor(val psiManager:PsiManager): ContentIterator {
    val nodes=mutableListOf<Node>()
    var model= Model(listOf())
    @OptIn(ExperimentalSerializationApi::class)
    override fun processFile(fileOrDir: VirtualFile): Boolean {
        if (fileOrDir.extension!="kt"){
            return true
        }
        val file= psiManager.findFile(fileOrDir)
        file!!.getChildrenOfType<KtFunction>().forEach{it->
            val annotations=it.annotationEntries.map{ann->ann.shortName.toString()}.toSet().intersect(model.layers.map{ layer->layer.name}
                .toSet())
            if (annotations.size==1){
                val dependencies=it.collectDescendantsOfType<KtDotQualifiedExpression>().map{dqe->dqe.receiverExpression.text}+
                        it.collectDescendantsOfType<KtCallExpression>().map{ ce->ce.callName()}+it.collectDescendantsOfType<KtTypeProjection>().map{tp->tp.typeReference!!.text}
                nodes.add(Node(it.name!!,annotations.first(),dependencies))            }
            }
        file!!.getChildrenOfType<KtClass>().forEach{it->
            val annotations=it.annotationEntries.map{ann->ann.shortName.toString()}.toSet().intersect(model.layers.map{ layer->layer.name}
                .toSet())
            if (annotations.size==1){
                val dependencies=it.collectDescendantsOfType<KtDotQualifiedExpression>().map{dqe->dqe.receiverExpression.text}+
                        it.collectDescendantsOfType<KtCallExpression>().map{ ce->ce.callName()}+it.collectDescendantsOfType<KtTypeProjection>().map{tp->tp.typeReference!!.text}
                nodes.add(Node(it.name!!,annotations.first(),dependencies))
            }
        }
        return true
    }
}

@kotlinx.serialization.Serializable
data class Layer(val name:String,val level:Int)

@kotlinx.serialization.Serializable
data class Model(val layers:List<Layer>)

class NaiveGraphBuilder(){

    fun buildGraph(text:String,model: Model):String{
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



