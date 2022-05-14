package com.injectorsuite.cablemanagement.actions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.roots.ContentIterator
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.idea.debugger.sequence.psi.callName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
//import org.jetbrains.kotlin.psi.*
import javax.swing.JComponent




class ShowModelAdherence: AnAction() {
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

        GraphDialogWrapper(graph,ktp.model).show()
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


class GraphDialogWrapper(val graph:MutableList<Node>,val model:Model): DialogWrapper(true) {
    val positionMap=mutableMapOf<Node,Offset>()
    init{
        init()
        title="Model Adherence"
        setSize(500,500)
    }
    override fun createCenterPanel(): JComponent? {
        return ComposePanel().apply {
            setContent {
                Column(Modifier.size(500.dp)) {
                    Canvas(Modifier){
                        model.layers.sortedBy{it.level}.forEachIndexed{outerIndex,layer->
                            val rectSize=graph.count{node->node.layer==layer.name}
                            drawRect(Color.Black, Offset(10f,150f*outerIndex+10f), Size(rectSize*200f,50f),1f,Stroke(3f))
                            drawContext.canvas.nativeCanvas.apply{
                                drawTextLine(TextLine.make(layer.name+" Layer",Font()),rectSize*100f,150f*outerIndex+75f,Paint())
                            }
                            graph.filter{node->node.layer==layer.name}.forEachIndexed{innerIndex,node->
                                val center=Offset(30f+200f*innerIndex,150f*outerIndex+35f)
                                drawCircle(Color.Blue,10f,center,1f,Fill)
                                drawContext.canvas.nativeCanvas.apply{
                                    drawTextLine(TextLine.make(node.name, Font()),center.x+15f,center.y,Paint())
                                }
                                positionMap.put(node,center)
                            }
                        }
                        graph.forEach{node->
                            graph.filter{other->other.dependents.contains(node.name)}.forEach{other->
                                drawLine(Color.Green,positionMap[other]!!,positionMap[node]!!)
                            }
                        }
                    }

                }
            }
        }
    }
}



