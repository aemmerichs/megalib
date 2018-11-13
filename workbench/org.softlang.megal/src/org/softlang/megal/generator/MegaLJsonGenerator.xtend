/*
 * generated by Xtext 2.12.0
 */
package org.softlang.megal.generator

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.softlang.megal.megaL.impl.ModuleImpl
import org.softlang.megal.megaL.impl.TypeImpl
import org.softlang.megal.megaL.impl.InstanceImpl
import org.softlang.megal.megaL.impl.FunDeclImpl
import org.softlang.megal.megaL.impl.FunAppImpl

/**
 * Generates code from your model files on save.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#code-generation
 */
class MegaLJsonGenerator extends AbstractGenerator {

	override void doGenerate(Resource input, IFileSystemAccess2 fsa, IGeneratorContext context) {
		fsa.generateFile(input.fileName, (input.contents.head as ModuleImpl).toJson)
	}

	private def toJson(ModuleImpl it) {
		// TODO: Write JSON
		'''Model: �it.name�
			Nodes:
			�FOR statement : it.statements�
				�IF statement instanceof TypeImpl�
					�statement.printNode�
				�ELSEIF statement instanceof InstanceImpl�
					�statement.printNode�
				�ELSEIF statement instanceof FunDeclImpl�
					�statement.printNode�
				�ELSEIF statement instanceof FunAppImpl�
					�statement.printNode�
				�ENDIF�	 
			�ENDFOR�
			Edges:
			�FOR statement : it.statements�
				�IF statement instanceof TypeImpl�
					�statement.printEdge�
				�ELSEIF statement instanceof InstanceImpl�
					�statement.printEdge�
				�ELSEIF statement instanceof FunDeclImpl�
					�statement.printEdge�
				�ELSEIF statement instanceof FunAppImpl�
					�statement.printEdge�
				�ENDIF�	 
			�ENDFOR�
		'''
	}

	private def printNode(FunAppImpl t) {
		'''
			{
			 "name": Application:�t.f.name�,
			}
		'''
	}

	private def printNode(FunDeclImpl t) {
		'''
			{
			 "name": Declaration:�t.name�,
			}
		'''
	}

	private def printNode(InstanceImpl t) {
		'''
			{
			 "name": �t.name�,
			 "links": �t.links.printUrlArray�,
			 "bindings": �t.binds.printUrlArray�	 	 
			}
		'''
	}

	private def printNode(TypeImpl t) {
		'''
			{
			 "name":  �t.name�,
			 "links": �t.links.printUrlArray�	 
			}
		'''
	}

	private def printEdge(FunAppImpl t) {
		'''
			�FOR in : t.in�
				{
				 "label: in", 
				 "source": �in.name�,
				 "target": �t.f.name�	
				}
			�ENDFOR�
			�FOR out : t.out�
				{
				 "label: out", 
				 "source": �out.name�,
				 "target": �t.f.name�	
				}
			�ENDFOR�
		'''
	}

	private def printEdge(FunDeclImpl t) {
		'''
			�FOR domain : t.domains�
				{
				 "label: inputOf", 
				 "source": �domain.name�,
				 "target": �t.name�	
				}
			�ENDFOR�
			�FOR range : t.ranges�
				{
				 "label: outputOf", 
				 "source": �range.name�,
				 "target": �t.name�	
				}
			�ENDFOR�
		'''
	}

	private def printEdge(InstanceImpl t) {
		'''
			{
			 "label: elementOf", 
			 "source": �t.name�,
			 "target": �t.type.name�	
			},
			
			�FOR relation : t.relEdges�
				{
				 "label": �relation.rel.name�,
				 "source":�t.name�,
				 "target":�relation.right.name�
				}
			�ENDFOR�
		'''
	}

	private def printEdge(TypeImpl t) {
		'''
			�IF t.supertype !== null�
			{
				"label: subtypeOf", 
				"source":  �t.name�,
				"target":  �t.supertype.name�
			}
			�ENDIF�
		'''
	}

	private def printUrlArray(String[] urls) {
		'''
			[
			�FOR url : urls�
				�url�
				�ENDFOR�
			]
		'''
	}

	private def fileName(Resource res) {
		// generate the json file into the "src-gen" folder using the package structure of the model file
		res.URI.segments.drop(3).join("/").replace(".", "_") + ".json"
	}

}
