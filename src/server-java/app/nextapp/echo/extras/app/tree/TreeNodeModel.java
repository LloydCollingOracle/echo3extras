package nextapp.echo.extras.app.tree;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import nextapp.echo.extras.app.event.TreeModelEvent;
import nextapp.echo.extras.app.event.TreeModelListener;

/**
 * A {@link TreeModel} implementation that compiles a {@link TreeNode} root node and it's children
 * into a model as used by {@link nextapp.echo.extras.app.Tree}.
 * 
 * If constructed with only the root node, then the columns will be the set of keys that are 
 * contained in any of the tree nodes, with no guaranteed ordering.
 *
 * @author Lloyd Colling
 */
public class TreeNodeModel extends AbstractTreeModel implements TreeModelListener {
    
    /**
     * The root node of the model
     */
    TreeNode root;
    /**
     * The keys used for retrieving the column values
     */
    String[] columnKeys;
    
    /**
     * The set of nodes that we are listening to
     */
    Set nodesWeAreListeningTo = new HashSet();
    
    /**
     * Default constructor that creates a model from a node and compiles the set of column keys from
     * all the nodes in the tree.
     * @param root
     */
    public TreeNodeModel(TreeNode root) {
        this(root, null);
    }
    
    /**
     * Constructor that creates a model from the given node using the supplied keys to retrieve the
     * column values for display.
     * @param root
     * @param columnKeys
     */
    public TreeNodeModel(TreeNode root, String[] columnKeys) {
        super();
        this.root = root;
        if (columnKeys != null)
            this.columnKeys = columnKeys;
        else
            compileColumns();
        addListenersToTree(root);
    }
    
    /**
     * Adds this model as a tree model listener to the given node and it's children.
     * @param treefragment
     */
    protected void addListenersToTree(TreeNode treefragment) {
        if (!nodesWeAreListeningTo.contains(treefragment)) {
            treefragment.addTreeModelListener(this);
            nodesWeAreListeningTo.add(treefragment);
        }
        for(int i = 0; i < treefragment.getChildCount(); i++) {
            addListenersToTree(treefragment.getChild(i));
        }
    }

    protected void removeListenersFromTree(TreeNode treeNode) {
        if (nodesWeAreListeningTo.contains(treeNode)) {
            treeNode.removeTreeModelListener(this);
            nodesWeAreListeningTo.remove(treeNode);
        }
        for(int i = 0; i < treeNode.getChildCount(); i++) {
            removeListenersFromTree(treeNode.getChild(i));
        }
    }
    
    /**
     * Compiles the list of column headings from the current tree
     */
    protected void compileColumns() {
        List columnHeadings = new LinkedList();
        addColumnHeadings(root, columnHeadings);
        columnKeys = new String[columnHeadings.size()];
        for (int i = 0; i < columnHeadings.size(); i++) {
            columnKeys[i] = (String)columnHeadings.get(i);
        }
    }
    
    /**
     * Appends any unknown column keys from the given node and it's children to the current list of
     * column headings.
     * @param node
     * @param columnHeadings
     */
    private void addColumnHeadings(TreeNode node, List columnHeadings) {
        Set keys =  node.getColumnValues().keySet();
        for (Iterator i = keys.iterator(); i.hasNext();) {
            String thisKey = (String)i.next();
            if (!columnHeadings.contains(thisKey))
                columnHeadings.add(thisKey);
        }
    }

    /**
     * @see nextapp.echo.extras.app.tree.TreeModel.getChild(Object, int)
     */
    public Serializable getChild(Serializable parent, int index) {
        return ((TreeNode)parent).getChild(index);
    }

    /**
     * @see nextapp.echo.extras.app.tree.TreeModel.getChildCount(Object)
     */
    public int getChildCount(Serializable parent) {
        return ((TreeNode)parent).getChildCount();
    }

    /**
     * @see nextapp.echo.extras.app.tree.TreeModel.getColumnCount()
     */
    public int getColumnCount() {
        return columnKeys.length;
    }

    /**
     * @see nextapp.echo.extras.app.tree.TreeModel.getIndexOfChild(Object, Object)
     */
    public int getIndexOfChild(Serializable parent, Serializable child) {
        return ((TreeNode)parent).getIndexOf((TreeNode)child);
    }

    /**
     * @see nextapp.echo.extras.app.tree.TreeModel.getRoot()
     */
    public Serializable getRoot() {
        return root;
    }

    /**
     * @see nextapp.echo.extras.app.tree.TreeModel.getValueAt(Object, int)
     */
    public Serializable getValueAt(Serializable node, int column) {
        return (Serializable)((TreeNode)node).getColumnValues().get(columnKeys[column]);
    }

    /**
     * @see nextapp.echo.extras.app.tree.TreeModel.isLeaf(Object)
     */
    public boolean isLeaf(Serializable object) {
        return ((TreeNode)object).isLeaf();
    }

    /**
     * handles notifications from the tree nodes that the tree structure has changed
     */
    public void treeNodesAdded(TreeModelEvent e) {
        for (int i = 0; i < e.getChildren().length; i++) {
            addListenersToTree((TreeNode)e.getChildren()[i]);
        }
        fireTreeNodesInserted(this, e.getPath(), e.getChildIndices(), e.getChildren());
    }

    /**
     * handles notifications from the tree nodes that the tree structure has changed
     */
    public void treeNodesChanged(TreeModelEvent e) {
        fireTreeNodesChanged(this, e.getPath(), e.getChildIndices(), e.getChildren());
    }

    /**
     * handles notifications from the tree nodes that the tree structure has changed
     */
    public void treeNodesRemoved(TreeModelEvent e) {
        for (int i = 0; i < e.getChildren().length; i++) {
            removeListenersFromTree((TreeNode)e.getChildren()[i]);
        }
        fireTreeNodesRemoved(this, e.getPath(), e.getChildIndices(), e.getChildren());
    }

    /**
     * handles notifications from the tree nodes that the tree structure has changed
     */
    public void treeStructureChanged(TreeModelEvent e) {
        removeAllNodeListeners();
        addListenersToTree(root);
        fireTreeStructureChanged(this, e.getPath(), e.getChildIndices(), e.getChildren());
    }

    /**
     * Removes this TreeModel as a model listener from all the nodes it is currently registered to
     */
    private void removeAllNodeListeners() {
        for(Iterator i = nodesWeAreListeningTo.iterator(); i.hasNext();) {
            ((TreeNode)i.next()).removeTreeModelListener(this);
        }
        nodesWeAreListeningTo.clear();
    }

}