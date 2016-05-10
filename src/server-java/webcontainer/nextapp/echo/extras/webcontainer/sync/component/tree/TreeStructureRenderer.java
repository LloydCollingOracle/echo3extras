package nextapp.echo.extras.webcontainer.sync.component.tree;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import nextapp.echo.app.Component;
import nextapp.echo.app.util.Context;
import nextapp.echo.extras.app.Tree;
import nextapp.echo.extras.app.tree.TreeModel;
import nextapp.echo.extras.app.tree.TreePath;
import nextapp.echo.webcontainer.UserInstance;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Sends down the tree structure to the client. If a render state exists and a full render
 * is not required, an update will be sent.
 */
public class TreeStructureRenderer {
    private Tree tree;
    private TreeModel model;
    private int columnCount;
    private Element propertyElement;
    private Document document;
    private Set renderedPaths = new HashSet();
    private TreeRenderState renderState;
    
    public TreeStructureRenderer(Element propertyElement, Tree tree) {
        this.propertyElement = propertyElement;
        document = propertyElement.getOwnerDocument();
        this.tree = tree;
        columnCount = getColumnCount(tree);
        model = tree.getModel();
    }
    
    public void render(Context context, TreeRenderState renderState) {
        this.renderState = renderState;
        if (renderState.isFullRender()) {
            if (tree.isHeaderVisible()) {
                // header
                renderNode(context, null, null, false);
            }
            Object value = model.getRoot();
            renderNode(context, value, new TreePath(value), true);
            renderState.setFullRender(false);
            propertyElement.setAttribute("fr", "1");
        } else if (renderState.hasChangedPaths()) {
            for (Iterator iterator = renderState.changedPaths(); iterator.hasNext();) {
                TreePath path = (TreePath) iterator.next();
                renderNode(context, path.getLastPathComponent(), path, true);
                renderedPaths.add(path);
            }
        }
    }
    
    protected void renderNode(Context context, Object value, TreePath path, boolean root) {
        if (renderedPaths.contains(path)) {
            return;
        }
        if (renderState.isSent(path) && !renderState.isPathChanged(path)) {
            return;
        }
        
        renderedPaths.add(path);
        Component component = tree.getComponent(path, 0);

        boolean expanded = tree.isExpanded(path);
        boolean leaf = value != null && model.isLeaf(value);
        Element eElement = doRenderNode(path, component, context, value, root);
        
        propertyElement.appendChild(eElement);
        
        if (value == null) {
            return;
        }
        if (expanded) {
            int childCount = model.getChildCount(value);
            for (int i = 0; i < childCount; ++i) {
                Object childValue = model.getChild(value, i);
                renderNode(context, childValue, path.pathByAddingChild(childValue), false);
            }
        }
        if (expanded || leaf) {
            renderState.addSentPath(path);
        }
    }
    
    protected Element doRenderNode(TreePath path, Component component, Context context, Object value, boolean root) {
        
        UserInstance userInstance = (UserInstance) context.get(UserInstance.class);
        
        boolean expanded = tree.isExpanded(path);
        String id = userInstance.getClientRenderId(component);
        Element eElement = document.createElement("e");
        eElement.setAttribute("i", id);
        if (path != null) {
            TreePath parentPath = path.getParentPath();
            if (parentPath != null) {
                eElement.setAttribute("p", userInstance.getClientRenderId(tree.getComponent(parentPath, 0)));
            }
        }
        boolean leaf = value != null && model.isLeaf(value);
        if (path == null) {
            eElement.setAttribute("h", "1");
        } else {
            if (expanded) {
                eElement.setAttribute("ex", "1");
            } else {
                if (leaf) {
                    eElement.setAttribute("l", "1");
                }
            }
            if (root) {
                eElement.setAttribute("r", "1");
            }
        }
        
        if (!renderState.isSent(path)) {
            for (int i = 1; i < columnCount; ++i) {
                Component columnComponent = tree.getComponent(path, i);
                Element columnElement = document.createElement("c");
                columnElement.setAttribute("i", userInstance.getClientRenderId(columnComponent));
                eElement.appendChild(columnElement);
            }
        }
        return eElement;
    }
    
    private static int getColumnCount(Tree tree) {
        return tree.getColumnModel().getColumnCount();
    }

}
