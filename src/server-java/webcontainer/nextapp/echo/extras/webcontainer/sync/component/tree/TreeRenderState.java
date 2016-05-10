package nextapp.echo.extras.webcontainer.sync.component.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import nextapp.echo.extras.app.Tree;
import nextapp.echo.extras.app.event.TreeExpansionEvent;
import nextapp.echo.extras.app.event.TreeExpansionListener;
import nextapp.echo.extras.app.tree.TreePath;
import nextapp.echo.webcontainer.RenderState;

/**
 * Holds the state of the client side tree.
 */
public class TreeRenderState implements RenderState {
    private static final long serialVersionUID = 1L;
    /**
     * Holds all paths that are sent down to the client. When a path has children it
     * is only added to this set if all it's children are sent down too.
     */
    private Set sentPaths = new HashSet();
    /**
     * Holds paths that are changed on the server since the last synchronization
     */
    private Set changedPaths = new HashSet();
    /**
     * Holds all selection paths that have not been sent to the client
     */
    private Set unsentSelections = new HashSet();
    /**
     * The path that changed the expansion state of as a result
     * of a client update.
     */
    private TreePath clientPath;
    /**
     * Indicates whether a full render is necessary
     */
    private boolean fullRender = true;
    private final Tree tree;
    
    /**
     * Listens for changes in the expansion state. All changed paths are added
     * to the <code>changedPaths</code> list.
     */
    private TreeExpansionListener expansionListener = new TreeExpansionListener() {
        public void treeCollapsed(TreeExpansionEvent event) {
            if (!event.getPath().equals(clientPath)) {
                changedPaths.add(event.getPath());
            }
        }
        
        public void treeExpanded(TreeExpansionEvent event) {
            if (!event.getPath().equals(clientPath)) {
                changedPaths.add(event.getPath());
            }
        }
    };
    
    public TreeRenderState(Tree tree) {
        this.tree = tree;
        tree.addTreeExpansionListener(expansionListener);
    }
    
    public void setClientPath(TreePath path) {
        this.clientPath = null;
        if (isSent(path)) {
            this.clientPath = path;
        }
    }
    
    public boolean isFullRender() {
        return fullRender;
    }
    
    public void setFullRender(boolean newValue) {
        fullRender = newValue;
    }
    
    public void addSentPath(TreePath path) {
        sentPaths.add(path);
    }
    
    public void removeSentPath(TreePath path) {
        sentPaths.remove(path);
    }
    
    public boolean isSent(TreePath path) {
        return sentPaths.contains(path);
    }
    
    /**
     * Returns all paths of which the expansion state has been changed since
     * the last synchronization. The paths are sorted by row index.
     * 
     * @return all paths that are changed since last synchronization
     */
    public Iterator changedPaths() {
        ArrayList list = new ArrayList(changedPaths);
        Collections.sort(list, new Comparator() {
            public int compare(Object obj1, Object obj2) {
                TreePath path1 = (TreePath) obj1;
                TreePath path2 = (TreePath) obj2;
                if (path1 == path2 || path1.equals(path2)) {
                    return 0;
                }
                int path1Count = path1.getPathCount();
                int path2Count = path2.getPathCount();
                if (path1Count == 1) { // path1 has only root element
                    return -1;
                } else if (path2Count == 1) { // path2 has only root element
                    return 1;
                }
                int end = Math.min(path1Count, path2Count);
                int i = 1;
                for (; i < end; i++) {
                    Object comp1 = path1.getPathComponent(i);
                    Object comp2 = path2.getPathComponent(i);
                    if (comp1 != comp2) {
                        return compareNodes(i, path1, path2);
                    }
                }
                if (path1Count == i) { // path1 has only root element
                    return -1;
                } else if (path2Count == i) { // path2 has only root element
                    return 1;
                }
                return compareNodes(i, path1, path2);
            }
            
            private int compareNodes(int index, TreePath path1, TreePath path2) {
            	Serializable commonParent = path1.getPathComponent(index - 1);
                int index1 = tree.getModel().getIndexOfChild(commonParent, path1.getPathComponent(index));
                int index2 = tree.getModel().getIndexOfChild(commonParent, path2.getPathComponent(index));
                if (index1 < index2) {
                    return -1;
                } else if (index1 > index2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        return list.iterator();
    }
    
    public void clearChangedPaths() {
        clientPath = null;
        changedPaths.clear();
    }
    
    public boolean hasChangedPaths() {
        return clientPath != null || !changedPaths.isEmpty();
    }
    
    public boolean isPathChanged(TreePath path) {
        return changedPaths.contains(path);
    }
    
    public boolean hasUnsentSelections() {
        return !unsentSelections.isEmpty();
    }
    
    public void addUnsentSelection(TreePath path) {
        unsentSelections.add(path);
    }
    
    public void removeUnsentSelection(TreePath path) {
        unsentSelections.remove(path);
    }
}
