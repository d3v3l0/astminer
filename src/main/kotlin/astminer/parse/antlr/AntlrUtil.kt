package astminer.parse.antlr

import astminer.common.Node
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Vocabulary
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.TerminalNode

fun convertAntlrTree(tree: ParserRuleContext, ruleNames: Array<String>, vocabulary: Vocabulary): SimpleNode {
    return simplifyTree(convertRuleContext(tree, ruleNames, null, vocabulary))
}

private fun convertRuleContext(ruleContext: ParserRuleContext, ruleNames: Array<String>, parent: Node?, vocabulary: Vocabulary): SimpleNode {
    val typeLabel = ruleNames[ruleContext.ruleIndex]
    val currentNode = SimpleNode(typeLabel, parent, null)
    val children: MutableList<Node> = ArrayList()

    ruleContext.children.forEach {
        if (it is TerminalNode) {
            children.add(convertTerminal(it, currentNode, vocabulary))
            return@forEach
        }
        if (it is ErrorNode) {
            children.add(convertErrorNode(it, currentNode))
            return@forEach
        }
        children.add(convertRuleContext(it as ParserRuleContext, ruleNames, currentNode, vocabulary))
    }
    currentNode.setChildren(children)

    return currentNode
}

private fun convertTerminal(terminalNode: TerminalNode, parent: Node?, vocabulary: Vocabulary): SimpleNode {
    return SimpleNode(vocabulary.getSymbolicName(terminalNode.symbol.type), parent, terminalNode.symbol.text)
}

private fun convertErrorNode(errorNode: ErrorNode, parent: Node?): SimpleNode {
    return SimpleNode("Error", parent, errorNode.text)
}

private fun simplifyTree(tree: SimpleNode): SimpleNode {
    return if (tree.getChildren().size == 1) {
        simplifyTree(tree.getChildren().first() as SimpleNode)
    } else {
        tree.setChildren(tree.getChildren().map { simplifyTree(it as SimpleNode) })
        tree
    }
}

