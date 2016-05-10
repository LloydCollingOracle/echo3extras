package nextapp.echo.extras.app.tree;

import java.io.Serializable;
import java.util.Map;

import nextapp.echo.extras.app.event.TreeModelListener;

/**
 * Represents a single node in a tree that contains a Map of values that may be displayed
 * as the columns for that node.
 *
 * @author Lloyd Colling
 */
public interface TreeNode extends Serializable {

    /**
     * How many children the node has
     * @return
     */
    public int getChildCount();
    
    /**
     * Retrieves the node at the specified index
     * @param index
     * @return
     */
    public TreeNode getChild(int index);
    
    /**
     * Whether this node is a leaf node
     * @return
     */
    public boolean isLeaf();
    
    /**
     * Returns this node's current parent
     * @return
     */
    public TreeNode getParent();
    
    /**
     * Sets the parent of the current node
     * @param parent
     */
    public void setParent(TreeNode parent);
    
    /**
     * Adds the specified node as a child of this node
     * @param node
     */
    public void addChild(TreeNode node);
    
    /**
     * Removes the specified node from this node's children
     * @param node
     */
    public void removeChild(TreeNode node);
    
    /**
     * Removes all the children from this node
     */
    public void removeAllChildren();
    
    /**
     * Adds a listener to receive events generated by this node
     * @param listener
     */
    public void addTreeModelListener(TreeModelListener listener);
    
    /**
     * Stops the listener from receiving events generated by this node
     * @param listener
     */
    public void removeTreeModelListener(TreeModelListener listener);
    
    /**
     * The Map of column name to column value for this node
     * @return
     */
    public Map getColumnValues();
    
    /**
     * Sets the column values for this node
     * @param values
     */
    public void setColumnValues(Map values);
    
    /**
     * Returns the node corresponding with the given path based on this node.
     * The path must start with this node.
     * @param path
     * @return
     */
    public TreeNode getNodeForPath(TreePath path);
    
    /**
     * Returns the index of the specified node in this node's children
     * @param node
     * @return
     */
    public int getIndexOf(TreeNode node);
}