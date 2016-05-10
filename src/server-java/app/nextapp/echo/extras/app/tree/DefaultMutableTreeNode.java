package nextapp.echo.extras.app.tree;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * A default implementation of the {@link TreeNode} interface.
 * The column values are stored as an immutable map cloned from the
 * Map that is supplied, so any changes to the column values can only be
 * applied by calling setColumnValues with the modified Map.
 *
 * @author Lloyd Colling
 */
public class DefaultMutableTreeNode extends AbstractTreeNode {
    
    /**
     * The parent node for this TreeNode. If null, this is either
     * a root node or a tree fragment.
     */
    TreeNode parent;
    /**
     * The list of child nodes for this node
     */
    Vector children = new Vector();
    /**
     * The list of column values for this node
     */
    Map columnValues = Collections.emptyMap();
    /**
     * Whether this node is a leaf node. If null, then indicates that
     * we are a leaf if we have no children.
     */
    Boolean isLeaf;
    
    /**
     * Default constructor
     */
    public DefaultMutableTreeNode() {
        this(null);
    }
    
    /**
     * Constructor that sets the column values for this node
     * @param columnValues
     */
    public DefaultMutableTreeNode(Map columnValues) {
        this(columnValues, null);
    }
    
    /**
     * Constructor that sets the column values for this node and it's leaf status
     * @param columnValues
     * @param isLeaf
     */
    public DefaultMutableTreeNode(Map columnValues, Boolean isLeaf) {
        super();
        if (columnValues != null)
            this.columnValues = columnValues;
        this.isLeaf = isLeaf;
    }

    /**
     * Adds a child to this node at the end of the child nodes list
     */
    public void addChild(TreeNode node) {
        children.add(node);
        node.setParent(this);
        int childIndex = children.indexOf(node);
        fireTreeNodesInserted(new int[] {childIndex});
    }

    /**
     * Returns the node at the specified index.
     * @throws ArrayIndexOutOfBoundsException if the index is out of range for the child list
     */
    public TreeNode getChild(int index) {
        if (index < 0 || index >= children.size()) {
            throw new ArrayIndexOutOfBoundsException("Index " 
                    + index 
                    + " is not in range 0 <= index < " 
                    + children.size());
        }
        return (TreeNode)children.get(index);
    }

    /**
     * Returns the number of children this node holds
     */
    public int getChildCount() {
        return children.size();
    }

    /**
     * Returns an unmodifiable map of the nodes column values
     */
    public Map getColumnValues() {
        return Collections.unmodifiableMap(columnValues);
    }

    /**
     * Retrieves the node at the end of the given path. 
     * The path must start with this node or an IllegalStateException will be thrown.
     */
    public TreeNode getNodeForPath(TreePath path) {
        Object[] pathObj = path.getPath();
        if (pathObj.length == 0) {
            throw new IllegalStateException("Empty path");
        } else if (pathObj[0] != this) {
            throw new IllegalStateException("Path does not start at this node");
        } else if (pathObj.length == 1) {
            return this;
        } else {
            TreeNode child = null;
            for (int i = 0; i < children.size() && child == null; i++) {
                if (children.get(i) == pathObj[1])
                    child = (TreeNode)children.get(i);
            }
            
            if (child == null)
                throw new IllegalArgumentException("Path is not valid due to child not found: " 
                        + path);
            Object[] subPath = new Object[pathObj.length - 1];
            System.arraycopy(pathObj, 1, subPath, 0, subPath.length);
            return child.getNodeForPath(new TreePath(subPath));
        }
    }

    /**
     * Returns the current parent of this node
     */
    public TreeNode getParent() {
        return this.parent;
    }
    
    /**
     * Sets the current parent of this node.
     */
    public void setParent(TreeNode node) {
        if (parent != null)
            parent.removeChild(this);
        this.parent = node;
    }

    /**
     * Whether this node is a leaf node
     */
    public boolean isLeaf() {
        if (isLeaf != null)
            return isLeaf.booleanValue();
        
        return getChildCount() == 0;
    }
    
    /**
     * Sets whether this node is a leaf node. If set to null, then the node will be a leaf node if
     * it has no children.
     * @param isLeaf
     */
    public void setLeaf(Boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    /**
     * Clears the list of children for this node
     */
    public void removeAllChildren() {
        int[] children = new int[this.children.size()];
        for (int i = 0; i < children.length; i++) {
            children[i] = i;
        }
        TreeNode[] childNodes = (TreeNode[])this.children.toArray(new TreeNode[this.children.size()]); 
        this.children.removeAllElements();
        for (int i = 0; i < childNodes.length; i++) {
            ((TreeNode)childNodes[i]).setParent(null);
        }
        fireTreeNodesRemoved(children, childNodes);
    }

    /**
     * Removes the specified node from the list of children. If the node is not in the list of 
     * children, this does nothing.
     */
    public void removeChild(TreeNode node) {
        int index = children.indexOf(node);
        if (index == -1)
            return;
        children.remove(node);
        node.setParent(null);
        fireTreeNodesRemoved(new int[] {index}, new TreeNode[] {node});
    }

    /**
     * Sets the column values to be a shallow copy of the map of values passed.
     */
    public void setColumnValues(Map values) {
        this.columnValues = new HashMap();
        this.columnValues.putAll(values);
        if (parent != null)
            fireTreeNodesChanged(parent, new int[] {parent.getIndexOf(this)});
    }

    /**
     * Returns the index of the specified node in this node's child list, or -1 if the supplied node
     * is not a child of this node.
     */
    public int getIndexOf(TreeNode node) {
        return children.indexOf(node);
    }

}