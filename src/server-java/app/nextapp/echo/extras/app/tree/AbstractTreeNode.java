package nextapp.echo.extras.app.tree;

import java.util.Map;
import java.util.Vector;

import nextapp.echo.app.event.EventListenerList;
import nextapp.echo.extras.app.event.TreeModelEvent;
import nextapp.echo.extras.app.event.TreeModelListener;

public abstract class AbstractTreeNode implements TreeNode {

    protected EventListenerList listenerList = new EventListenerList();

    public void addTreeModelListener(TreeModelListener listener) {
        listenerList.addListener(TreeModelListener.class, listener);
    }
    
    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.removeListener(TreeModelListener.class, l);
    }
    
    private Object[] getPathToRoot(TreeNode node) {
        Vector path = new Vector();
        TreeNode currNode = node;
        do {
            path.add(0, currNode);
            currNode = currNode.getParent();
        } while (currNode != null);
        return path.toArray();
    }
    
    private TreeNode[] getChildren(int[] childIndices) {
        TreeNode[] children = new TreeNode[childIndices.length];
        for (int i = 0; i < children.length; i++) {
            children[i] = getChild(childIndices[i]);
        }
        return children;
    }
    
    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     * 
     * @param source the node being changed
     * @param path the path to the root node
     * @param childIndices the indices of the changed elements
     * @param children the changed elements
     * @see EventListenerList
     */
    protected void fireTreeNodesChanged(TreeNode parent, int[] childIndices) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListeners(TreeModelListener.class);
        
        TreeModelEvent e = null;
        for (int i = 0; i < listeners.length; ++i) {
            // Lazily create the event:
            if (e == null) {
                e = new TreeModelEvent(parent, getPathToRoot(parent), childIndices, getChildren(childIndices));
            }
            ((TreeModelListener) listeners[i]).treeNodesChanged(e);
        }
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     * 
     * @param source the node where new elements are being inserted
     * @param path the path to the root node
     * @param childIndices the indices of the new elements
     * @param children the new elements
     * @see EventListenerList
     */
    protected void fireTreeNodesInserted(int[] childIndices) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListeners(TreeModelListener.class);
        TreeModelEvent e = null;
        for (int i = 0; i < listeners.length; i++) {
            // Lazily create the event:
            if (e == null) {
                e = new TreeModelEvent(this, getPathToRoot(this), childIndices, getChildren(childIndices));
            }
            ((TreeModelListener) listeners[i]).treeNodesAdded(e);
        }
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     * 
     * @param source the node where elements are being removed
     * @param path the path to the root node
     * @param childIndices the indices of the removed elements
     * @param children the removed elements
     * @see EventListenerList
     */
    protected void fireTreeNodesRemoved(int[] childIndices) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListeners(TreeModelListener.class);
        TreeModelEvent e = null;
        for (int i = 0; i < listeners.length; i++) {
            // Lazily create the event:
            if (e == null) {
                e = new TreeModelEvent(this, getPathToRoot(this), childIndices, getChildren(childIndices));
            }
            ((TreeModelListener) listeners[i]).treeNodesRemoved(e);
        }
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     * 
     * @param source the node where the tree model has changed
     * @param path the path to the root node
     * @param childIndices the indices of the affected elements
     * @param children the affected elements
     * @see EventListenerList
     */
    protected void fireTreeStructureChanged(int[] childIndices) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListeners(TreeModelListener.class);
        TreeModelEvent e = null;
        for (int i = 0; i < listeners.length; i++) {
            // Lazily create the event:
            if (e == null)
                e = new TreeModelEvent(this, getPathToRoot(this), childIndices, getChildren(childIndices));
            ((TreeModelListener) listeners[i]).treeStructureChanged(e);
        }
    }

}